import os
import sys
import argparse

def insert_project(dir, cursor):
    project_name = dir.split(os.path.sep)[1]
    cursor.execute('INSERT INTO projects(project_path, name) VALUES (?, ?)', [dir, project_name])


def insert_file(project_path, path, fname, ext, filetype, cursor):
    cursor.execute('INSERT INTO files(project_path, file_path, filename, extension, type) VALUES (?, ?, ?, ?, ?)',
                   [project_path, path, fname, ext, filetype])

def process_folder(input_folder, extension_map, file_map, cursor):
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
            continue
        else:
            project_path = os.path.sep.join(parts[0:2])
            
        for filename in filenames:
            try:
                filepath = os.path.join(dirpath, filename)
                ext = os.path.splitext(filename)[1]
                if ext in extension_map:
                    filetype = extension_map[ext]
                    print(filetype, filepath)
                    insert_file(project_path, filepath, filename, ext, filetype, cursor)
                elif filename in file_map:
                    filetype = file_map[filename]
                    print(filetype, filepath)
                    insert_file(project_path, filepath, filename, ext, filetype, cursor)
            except UnicodeEncodeError:
                print("Invalid file name")

                    
def open(output_file):
    import sqlite3
    conn   = sqlite3.connect(output_file)
    cursor = conn.cursor()

    cursor.execute('CREATE TABLE IF NOT EXISTS projects (project_path VARCHAR(255), name VARCHAR(255), PRIMARY KEY (project_path))')
    cursor.execute('CREATE TABLE IF NOT EXISTS files (project_path VARCHAR(255), file_path TEXT, filename VARCHAR(255), extension VARCHAR(32), type VARCHAR(32), PRIMARY KEY (file_path))')

    return cursor, conn


def parse_args():
    parser = argparse.ArgumentParser(description='Analyse the files in the repository and generates a databaes containing references to the interesting files.')
    parser.add_argument("-d", "--dir", dest='input', metavar='INPUT_FOLDER', type=str, required=True,
                    help='input folder for the test files')
    #parser.add_argument('-f', '--filelist', dest='filelist', metavar='FILE_LIST', type=str,
    #                help='List of files to be considered.')
    parser.add_argument('-o', '--output', dest='output', metavar='OUTPUT_FOLDER', type=str, required=True,
                    help='output database file')

    args = parser.parse_args()

    return args


if __name__ == "__main__":
    args = parse_args()
    input_folder = args.input
    output_db = args.output

    cursor, connection = open(output_db)

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
        
        '.atl': 'atl',

        '.qvto': 'qvto',

        '.ocl': 'ocl',
        
        '.cs': 'emftext',
        
        '.mtl': 'acceleo',
        
        '.xtext': 'xtext',

        '.ecore': 'ecore',
        '.emf': 'emfatic'
    }

    # TODO: A list of extensions which we need to inspect inside to know the content (e.g., xml files)
    
    process_folder(input_folder, extensions, filenames, cursor)

    connection.commit()
