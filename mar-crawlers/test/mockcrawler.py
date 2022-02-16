import os
import sys
import argparse

sys.path.append(os.path.join(os.path.dirname(os.path.abspath(__file__)), "../common"))
import crawler_common as common

def insert_repo_info(c, id, name):
    c.execute('INSERT INTO repo_info(id, name, full_name, html_url, git_url, stargazers_count, forks_count, topics, description, creation_date, last_update) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
              (id, name, name, 'http://' + name, 'git://' + name, 10, 5, 'test,model', 'a model', 1000, 2000))
    c.connection.commit()
    
def insert_file(c, full_file, filename):
    print("Inserting ", filename);
    stats = os.stat(full_file)

    c.execute('INSERT INTO data(model_id, filename, name, download_url, size, license, repo_id, creation_date, last_update, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
              (filename, filename, filename, 'file:/' + filename,
               stats.st_size, 'mine', 1, 1000, 2000, 'a description'))
    c.connection.commit()

def get_crawled_files(input_folder, extension):
    crawled_files = []
    for (dirpath, dirnames, filenames) in os.walk(input_folder):
        rel = ''
        if dirpath != input_folder:
            rel = os.path.relpath(dirpath, input_folder)
        
        for filename in filenames:
            if filename.endswith(extension):
                crawled_files.append([os.path.join(dirpath, filename), os.path.join(rel, filename)])

    return crawled_files

def process(crawled_files, output_folder):
    conn = common.open_db(output_folder, 'crawler.db', smash=True)
    cursor = conn.cursor()
            
    insert_repo_info(cursor, 1, 'test-repo')
    for full, f in crawled_files:
        insert_file(cursor, full, f)
            
def parse_args():
    parser = argparse.ArgumentParser(description='Simulate downloading test files.')
    parser.add_argument("-d", "--dir", dest='input', metavar='INPUT_FOLDER', type=str,
                    help='input folder for the test files')
    parser.add_argument('-f', '--filelist', dest='filelist', metavar='FILE_LIST', type=str,
                    help='List of files to be considered. Extension is not needed')     
    parser.add_argument('-e', '--extension', dest='extension', metavar='EXTENSION', type=str,
                    help='File extension to crawl')
    parser.add_argument('-o', '--output', dest='output', metavar='OUTPUT_FOLDER', type=str,
                    help='output folder to store the downloaded files')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    input_folder = args.input
    filelist = args.filelist
    output_folder = args.output
    extension = args.extension

    if extension is not None:
        all_files = get_crawled_files(input_folder, extension)
    else:
        with open(filelist) as file:
            lines = [line.rstrip() for line in file]        
        all_files = [[os.path.join(input_folder, f), f] for f in lines]        
    
    process(all_files, output_folder)
