import sys
import csv
import os
import argparse

sys.path.append(os.path.join(os.path.dirname(os.path.abspath(__file__)), "../common"))
import crawler_common as common

def insert_repo_info(c, id, name):
    c.execute('INSERT INTO repo_info(id, name, full_name, html_url, git_url, stargazers_count, forks_count, topics, description, creation_date, last_update) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
              (id, name, name, None, None, None, None, None, None, None, None))
    c.connection.commit()
    
def insert_file(c, full_file, filename):
    print("Inserting ", filename);
    stats = os.stat(full_file)

    c.execute('INSERT INTO data(model_id, filename, name, download_url, size, license, repo_id, creation_date, last_update, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
              (filename, filename, filename, 'file:/' + filename,
               stats.st_size, None, 1, None, None, None))
    c.connection.commit()

def process(test_input, extension, output_folder, repo_name):
    conn = common.open_db(output_folder, 'crawler.db', smash=True)
    cursor = conn.cursor()
    
    crawled_files = []
    for (dirpath, dirnames, filenames) in os.walk(test_input):
        rel = ''
        if dirpath != test_input:
            rel = os.path.relpath(dirpath, test_input)
        
        for filename in filenames:
            if filename.endswith(extension):
                crawled_files.append([os.path.join(dirpath, filename), os.path.join(rel, filename)])

            
    insert_repo_info(cursor, 1, repo_name)
    for full, f in crawled_files:
        insert_file(cursor, full, f)

def parse_args():
    parser = argparse.ArgumentParser(description='Simulate downloading files from a file based repository.')
    parser.add_argument('input', metavar='INPUT_FOLDER', type=str,
                   help='input folder for the test files')    
    parser.add_argument('extension', metavar='EXTENSION', type=str,
                   help='File extension to crawl')
    parser.add_argument('output', metavar='OUTPUT_FOLDER', type=str,
                   help='output folder to store the downloaded files')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    input_folder  = args.input
    output_folder = args.output
    extension = args.extension

    process(input_folder, extension, output_folder, 'files')
