import fnmatch
import os
import sys
import argparse
import yaml
from yaml.loader import SafeLoader
import glob
import logging
import cause
import git as gitpython

class Configuration:
    def __init__(self, data):
        self.data = data
        if 'content_filters' in data:
            filters = data['content_filters']
            self.content_filters = {"." + filter['extension']: filter['contains'] for filter in filters}
        else:
            self.content_filters = []

        if 'must_contain_filters' in data:
            filters = data['must_contain_filters']
            self.must_contain_filters = {"." + filter['extension']: filter['contains'] for filter in filters}
        else:
            self.must_contain_filters = []

        if 'ignore' in data:
            self.ignored_filters = [i['pattern'] for i in data['ignore']]
        else:
            self.ignored_filters = []

    def is_accepted(self, fullpath, filepath, ext):
        if ext in self.must_contain_filters:
            filter_ = self.must_contain_filters[ext]
            #print("Checking filter", filter_)
            try:
                with open(fullpath, 'r', encoding='utf-8', errors='ignore') as fp:
                    for l_no, line in enumerate(fp):
                        if filter_ in line:
                            print("It's accepted: ", filepath)
                            return True
                    # If the filter doesn't appear, return false
                    #print("Not accepted: ", filepath)
                    return False
            except:
                print("Can't process ", filepath)
                return False
        else:
            return True

    def is_filtered_out(self, fullpath, filepath, ext):
        if not self.is_accepted(fullpath, filepath, ext):
            return True

        if ext in self.content_filters:
            filter_ = self.content_filters[ext]
            try:
                with open(fullpath, 'r') as fp:
                    for l_no, line in enumerate(fp):
                        if filter_ in line:
                            print("Filtered by content: ", filepath)
                            return True
            except:
                print("Can't process ", filepath)
                return True

        for p in self.ignored_filters:
            # To force match everything after the pattern when this is not a glob pattern
            if '*' not in p:
                p = p + '*'

            if fnmatch.fnmatch(filepath, p):
            #if filepath.startswith(p):
                print("Filtered by pattern: ", filepath)
                return True

        return False
        
def load_config(configuration_file):
    f = open(configuration_file)
    data = yaml.load(f, Loader=SafeLoader)
    
    f.close()
    return Configuration(data)

def get_git_info(repo_path, file_rel_path):
    """Return (created_at, created_commit, created_author, updated_at, updated_commit, updated_author).
    All values are None if git info cannot be retrieved."""
    try:
        repo = gitpython.Repo(repo_path)
        commits = list(repo.iter_commits(paths=file_rel_path))
        if not commits:
            return {}

        latest = commits[0]
        oldest = commits[-1]

        return {
            'created_at':     oldest.committed_datetime.isoformat(),
            'created_commit': oldest.hexsha,
            'created_author': str(oldest.author),
            'updated_at':     latest.committed_datetime.isoformat(),
            'updated_commit': latest.hexsha,
            'updated_author': str(latest.author),
        }
    except Exception as e:
        logging.warning("Git error for '%s' in '%s': %s", file_rel_path, repo_path, e)
        return {}


def insert_project(dir, cursor):
    project_name = dir.split(os.path.sep)[1]
    cursor.execute('INSERT INTO projects(project_path, name) VALUES (?, ?)', [dir, project_name])


def insert_file(project_path, path, fname, ext, filetype, cursor,
                created_at=None, created_commit=None, created_author=None,
                updated_at=None, updated_commit=None, updated_author=None):
    cursor.execute(
        'INSERT INTO files(project_path, file_path, filename, extension, type, '
        'created_at, created_commit, created_author, updated_at, updated_commit, updated_author) '
        'VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [project_path, path, fname, ext, filetype,
         created_at, created_commit, created_author,
         updated_at, updated_commit, updated_author]
    )


def insert_dependency(filepath, depending_file, cursor):
    extension = os.path.splitext(depending_file)[1]
    cursor.execute('INSERT INTO dependencies(file_path, using_file, using_extension) VALUES (?, ?, ?)',
                   [filepath, depending_file, extension])


def process_folder(input_folder, extension_map, file_map, cursor, processed_projects, check_cause = False, conf = None):
    repo_cache = {}  # project_path -> git.Repo or None

    for (dirpath, dirnames, filenames) in os.walk(input_folder, topdown=True, followlinks=False):
        # See: https://stackoverflow.com/questions/19859840/excluding-directories-in-os-walk
        dirnames[:] = [d for d in dirnames if d != '.git']

        dirpath = os.path.relpath(dirpath, input_folder)

        parts = dirpath.split(os.path.sep)
        length = len(parts)

        if length < 2:
            continue
        elif length == 2:
            project_path = dirpath
            # It may happen that when processing another repository folder, there is a clash
            # because the project has been downloaded twice. In this case, it has already been processed
            # so we skip it
            if project_path in processed_projects:
                continue

            insert_project(dirpath, cursor)
            processed_projects.add(project_path)

            #continue
        else:
            project_path = os.path.sep.join(parts[0:2])

        #print("Processing: ", dirpath)

        # Open (and cache) the git repo for this project
        if project_path not in repo_cache:
            repo_abs = os.path.join(input_folder, project_path)
            try:
                repo_cache[project_path] = gitpython.Repo(repo_abs)
            except Exception as e:
                logging.warning("Could not open git repo at '%s': %s", repo_abs, e)
                repo_cache[project_path] = None

        repo = repo_cache[project_path]

        for filename in filenames:

            try:
                filepath = os.path.join(dirpath, filename)

                if filepath in processed_projects:
                    continue
                processed_projects.add(filepath)

                ext = os.path.splitext(filename)[1]

                if conf is not None:
                    if conf.is_filtered_out(os.path.join(input_folder, filepath), filepath, ext):
                        continue

                inserted = False
                if ext in extension_map:
                    filetype = extension_map[ext]
                    print(filetype, filepath)
                elif filename in file_map:
                    filetype = file_map[filename]
                    print(filetype, filepath)
                else:
                    filetype = None

                if filetype is not None:
                    # Collect git metadata for the file
                    file_rel_to_repo = os.path.relpath(filepath, project_path)
                    if repo is not None:
                        git_info = get_git_info(
                            os.path.join(input_folder, project_path), file_rel_to_repo
                        )
                    else:
                        git_info = {}

                    insert_file(project_path, filepath, filename, ext, filetype, cursor,
                                git_info.get('created_at'),
                                git_info.get('created_commit'),
                                git_info.get('created_author'),
                                git_info.get('updated_at'),
                                git_info.get('updated_commit'),
                                git_info.get('updated_author'))
                    inserted = True

                if check_cause:
                    if inserted and is_artefact_file_type(filetype):
                        deps = cause.get_using_files(filepath, filetype, input_folder)
                        for d in deps:
                            insert_dependency(filepath, d, cursor)

            except UnicodeEncodeError:
                print("Invalid file name")

    # Create a view named project_with_files with the query:
    cursor.execute("DROP VIEW IF EXISTS mde_projects;")
    cursor.execute("""
    CREATE VIEW mde_projects AS
    SELECT project_path, name
    FROM projects
    WHERE project_path IN (
        SELECT project_path FROM files
    );
    """)



def open_db(output_file):
    import sqlite3
    conn   = sqlite3.connect(output_file)
    cursor = conn.cursor()

    cursor.execute('CREATE TABLE IF NOT EXISTS projects (project_path VARCHAR(255), name VARCHAR(255), PRIMARY KEY (project_path))')
    cursor.execute('''CREATE TABLE IF NOT EXISTS files (
        project_path  VARCHAR(255),
        file_path     TEXT,
        filename      VARCHAR(255),
        extension     VARCHAR(32),
        type          VARCHAR(32),
        created_at    TEXT,
        created_commit TEXT,
        created_author TEXT,
        updated_at      TEXT,
        updated_commit  TEXT,
        updated_author  TEXT,
        PRIMARY KEY (file_path)
    )''')
    # A file_path is used in used_file if its name appears in used_file
    cursor.execute('CREATE TABLE IF NOT EXISTS dependencies (file_path TEXT, using_file TEXT, using_extension VARCHAR(32), PRIMARY KEY (file_path, using_file))')
    return cursor, conn


def parse_args():
    parser = argparse.ArgumentParser(description='Analyse the files in the repository and generates a databaes containing references to the interesting files.')
    parser.add_argument("-d", "--dir", dest='input', metavar='INPUT_FOLDER', type=str, required=True,
                    help='input folder(s) for the repositories')
    #parser.add_argument('-f', '--filelist', dest='filelist', metavar='FILE_LIST', type=str,
    #                help='List of files to be considered.')
    parser.add_argument('-o', '--output', dest='output', metavar='OUTPUT_FOLDER', type=str, required=True,
                    help='output database file')
    parser.add_argument('-c', '--configuration', dest='conf', metavar='CONFIGURATION_FOLDER', type=str, required=False,
                    help='configuration file')
    parser.add_argument('-cause', '--cause', dest='cause', action='store_true')

    args = parser.parse_args()

    return args

def is_artefact_file_type(file_type):
    return file_type not in ['ant', 'maven', 'eclipse-launcher']

if __name__ == "__main__":
    args = parse_args()
    input_folders = args.input
    output_db = args.output

    cursor, connection = open_db(output_db)

    filenames = {
        'build.xml': 'ant',
        'pom.xml': 'maven'
    }
    extensions = {
        '.etl': 'epsilon',
        '.evl': 'epsilon',
        '.egl': 'epsilon',
        '.egx': 'epsilon',
        '.eol': 'epsilon',
        '.ewl': 'epsilon',
        '.eml': 'epsilon',
        '.epl': 'epsilon',
        '.mig': 'epsilon',
        '.emg': 'epsilon', 
        
        '.atl': 'atl',

        '.qvto': 'qvto',

        '.ocl': 'ocl',
        
        '.cs': 'emftext',
        
        '.mtl': 'acceleo',
        
        '.xtext': 'xtext',
        '.xtend': 'xtend',
        '.mwe2' : 'mwe2',

        '.launch': 'eclipse-launcher',        

        '.ecore': 'ecore',
        '.xcore': 'xcore',
        
        '.emf': 'emfatic',

        '.odesign': 'sirius',
        '.gmfmap': 'gmf', # Because it contains the references to the mm

        '.henshin': 'henshin',

        '.jet': 'jet',
        '.javajet': 'jet',

        '.sdf3': 'spoofax',
        '.mps': 'mps'
    }

    # TODO: A list of extensions which we need to inspect inside to know the content (e.g., xml files)

    print(args.conf)
    if args.conf is not None:
        conf = load_config(args.conf)
    else:
        conf = None

    # Split input_folders using ":"
    processed_projects = set()
    for input_folder in input_folders.split(":"):
        process_folder(input_folder, extensions, filenames, cursor, processed_projects, args.cause, conf)

    connection.commit()
