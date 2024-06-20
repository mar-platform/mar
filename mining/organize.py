import fnmatch
import os
import sys
import argparse
import yaml
from yaml.loader import SafeLoader
import glob
import cause

class Configuration:
    def __init__(self, data):
        self.data = data
        if 'content_filters' in data:
            filters = data['content_filters']
            self.content_filters = {"." + filter['extension']: filter['contains'] for filter in filters}
        else:
            self.content_filters = []

        if 'ignore' in data:
            self.ignored_filters = [i['pattern'] for i in data['ignore']]
        else:
            self.ignored_filters = []

    def is_filtered_out(self, fullpath, filepath, ext):
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

def insert_project(dir, cursor):
    project_name = dir.split(os.path.sep)[1]
    cursor.execute('INSERT INTO projects(project_path, name) VALUES (?, ?)', [dir, project_name])


def insert_file(project_path, path, fname, ext, filetype, cursor):
    cursor.execute('INSERT INTO files(project_path, file_path, filename, extension, type) VALUES (?, ?, ?, ?, ?)',
                   [project_path, path, fname, ext, filetype])


def insert_dependency(filepath, depending_file, cursor):
    extension = os.path.splitext(depending_file)[1]
    cursor.execute('INSERT INTO dependencies(file_path, using_file, using_extension) VALUES (?, ?, ?)',
                   [filepath, depending_file, extension])


def process_folder(input_folder, extension_map, file_map, cursor, conf = None):
    for (dirpath, dirnames, filenames) in os.walk(input_folder, topdown=True, followlinks=False):
        # See: https://stackoverflow.com/questions/19859840/excluding-directories-in-os-walk
        dirnames[:] = [d for d in dirnames if d != '.git']

        dirpath = os.path.relpath(dirpath, input_folder)

        parts = dirpath.split(os.path.sep)
        length = len(parts)

        if length < 2:
            continue
        elif length == 2:
            insert_project(dirpath, cursor)
            project_path = dirpath
            #continue
        else:
            project_path = os.path.sep.join(parts[0:2])

        for filename in filenames:
            try:
                filepath = os.path.join(dirpath, filename)
                ext = os.path.splitext(filename)[1]

                if conf is not None:
                    if conf.is_filtered_out(os.path.join(input_folder, filepath), filepath, ext):
                        continue

                inserted = False
                if ext in extension_map:
                    filetype = extension_map[ext]
                    print(filetype, filepath)
                    insert_file(project_path, filepath, filename, ext, filetype, cursor)
                    inserted = True
                elif filename in file_map:
                    filetype = file_map[filename]
                    print(filetype, filepath)
                    insert_file(project_path, filepath, filename, ext, filetype, cursor)
                    inserted = True

                if inserted and is_artefact_file_type(filetype):
                    deps = cause.get_using_files(filepath, filetype, input_folder)
                    for d in deps:
                        insert_dependency(filepath, d, cursor)

            except UnicodeEncodeError:
                print("Invalid file name")

                    
def open_db(output_file):
    import sqlite3
    conn   = sqlite3.connect(output_file)
    cursor = conn.cursor()

    cursor.execute('CREATE TABLE IF NOT EXISTS projects (project_path VARCHAR(255), name VARCHAR(255), PRIMARY KEY (project_path))')
    cursor.execute('CREATE TABLE IF NOT EXISTS files (project_path VARCHAR(255), file_path TEXT, filename VARCHAR(255), extension VARCHAR(32), type VARCHAR(32), PRIMARY KEY (file_path))')
    # A file_path is used in used_file if its name appears in used_file
    cursor.execute('CREATE TABLE IF NOT EXISTS dependencies (file_path TEXT, using_file TEXT, using_extension VARCHAR(32), PRIMARY KEY (file_path, using_file))')

    return cursor, conn


def parse_args():
    parser = argparse.ArgumentParser(description='Analyse the files in the repository and generates a databaes containing references to the interesting files.')
    parser.add_argument("-d", "--dir", dest='input', metavar='INPUT_FOLDER', type=str, required=True,
                    help='input folder for the test files')
    #parser.add_argument('-f', '--filelist', dest='filelist', metavar='FILE_LIST', type=str,
    #                help='List of files to be considered.')
    parser.add_argument('-o', '--output', dest='output', metavar='OUTPUT_FOLDER', type=str, required=True,
                    help='output database file')
    parser.add_argument('-c', '--configuration', dest='conf', metavar='CONFIGURATION_FOLDER', type=str, required=False,
                    help='configuration file')

    args = parser.parse_args()

    return args

def is_artefact_file_type(file_type):
    return file_type not in ['ant', 'maven', 'eclipse-launcher']

if __name__ == "__main__":
    args = parse_args()
    input_folder = args.input
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

        '.launch': 'eclipse-launcher',        

        '.ecore': 'ecore',
        '.emf': 'emfatic',

        '.odesign': 'sirius',

        '.henshin': 'henshin',

        '.jet': 'jet',
        '.javajet': 'jet'
    }

    # TODO: A list of extensions which we need to inspect inside to know the content (e.g., xml files)

    print(args.conf)
    if args.conf is not None:
        conf = load_config(args.conf)
    else:
        conf = None
    
    process_folder(input_folder, extensions, filenames, cursor, conf)

    connection.commit()
