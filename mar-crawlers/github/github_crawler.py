from github import Github
import github
import random
from pathlib import Path
import os.path
import time
import base64
import csv
import os
import argparse
import traceback
import sqlite3
from datetime import datetime, timezone

import sys

sys.path.append(os.path.join(os.path.dirname(os.path.abspath(__file__)), "../common"))
import crawler_common as common

# https://github.com/PyGithub/PyGithub

token=os.environ.get('GH_TOKEN')
if token is None:
    print("GH_TOKEN variable required")
    exit(-1)

g = Github(token, per_page=100)

def api_wait_search(git):
  limits = git.get_rate_limit()
  print("   Limits: ", limits)
  if limits.search.remaining <= 10:
    seconds = (limits.search.reset - datetime.now()).total_seconds()
    print("Waiting for %d seconds ..." % (seconds))
    time.sleep(seconds)
    print("Done waiting - resume!")

def api_wait_search2(github):
    limits = github.get_rate_limit()
    reset = limits.search.reset.replace(tzinfo=timezone.utc)
    now = datetime.now(timezone.utc)
    seconds = (reset - now).total_seconds()
    print(f"Rate limit exceeded")
    print(f"Reset is in {seconds:.3g} seconds.")
    if seconds > 0.0:
        print(f"Waiting for {seconds:.3g} seconds...")
        time.sleep(seconds)
        print("Done waiting - resume!")

def largest_file(c):
    c.execute('select max(size) from data')
    value = c.fetchone()
    if value is None:
        return None
    return value[0]
    
def insert_file_contents(model_id, fname, c, f):
    """ 
    c is a Sqlite3 Cursor
    f is a ContentsFile
    """    
    repo = f.repository
    c.execute('SELECT id FROM repo_info WHERE id = %d' % repo.id)
    value = c.fetchone()
    # print(v)
    if value is None:
        # topics = [l for l in repo.get_topics()]
        topics = repo.get_topics()
        topics_str = ', '.join(topics)

        c.execute('INSERT INTO repo_info(id, name, full_name, html_url, git_url, stargazers_count, forks_count, topics, description) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)',
                  (repo.id, repo.name, repo.full_name, repo.html_url,
                   repo.git_url, repo.stargazers_count, repo.forks_count,
                   topics_str, repo.description))
        
    c.execute('INSERT INTO data(model_id, filename, name, download_url, size, license, repo_id) VALUES (?, ?, ?, ?, ?, ?, ?)',
              (model_id, fname, f.name, f.download_url,
               f.size, f.license, repo.id))
    c.connection.commit()
        
def process(hint, extension, writer, output_folder, init = None, end = 1_000_000, step = 5):
    total = 0

    db = os.path.join(output_folder, 'crawler.db')
    
    conn = common.open_db(output_folder, 'crawler.db')
    c = conn.cursor()
    
    if init is None:
        init = largest_file(c)
        if init is None:
            init = 512
        else:
            print("Using largest size as init value = ", init)
                
    # step = 5 # very small steps to avoid loosing files

    initial_step = step
    iterations_without_downloading = 0
    last_size = init
    for i in range(init, end, step):
        iterations_without_downloading = iterations_without_downloading + 1
        step = initial_step * iterations_without_downloading
        finished_chunk = False        
        while not finished_chunk:
            try:            
                size = 'size:' + str(i) + '..' + str(i + step - 1)        
                print("Processing with " + size)
                files = g.search_code(query=hint + ' extension:' + extension + ' ' + size)
                print("   There are " , files.totalCount)
            
                for f in files:
                    # api_wait_search(g)
            
                    print("   Processing ", total, "... ", f.name)

                    # Wait a bit, as requested by GitHub best pratices
                    # https://docs.github.com/en/free-pro-team@latest/rest/guides/best-practices-for-integrators#dealing-with-abuse-rate-limits
                    seconds = random.randint(1, 2)
                    print("   Waiting ", seconds, " seconds")
                    time.sleep(seconds)

                    print(f)

                    name = f.repository.full_name                   
                    model_id = os.path.join(name, f.path)
                    fname = os.path.join("data", name, f.path)
                    if common.model_already_exists(model_id, c):
                        print("Model already exists ", model_id)
                        continue
                    
                    # print(f.repository.name)
                    # print(f.repository.owner.name)
                    # print(f.repository.organization)
                    path = Path(output_folder, "data", name, f.path);
                    path.parents[0].mkdir(parents=True, exist_ok=True)
                    text = base64.b64decode(f.content).decode('utf-8')
                    print("   Writing to ", total, "...", path)

                    try:
                        with path.open("w") as target:
                            target.write(text)

                            insert_file_contents(model_id, fname, c, f)                        
                            writer.writerow([model_id, f.name, f.download_url, f.size])
                    except Exception as e:
                        print("Error processing ", model_id, " with size = ", f.size)
                        traceback.print_exc()

                    iterations_without_downloading = 0
                    last_size = f.size
                    total = total + 1
                    
                finished_chunk = True
            #except github.GithubException.RateLimitExceededException:
            except github.GithubException as exception:
            #except Exception as exception:
                traceback.print_exc()
                api_wait_search2(g)                
                if i == last_size:
                    i = i + 1 # Sometimes we can't make progress with the current size because of too many files (perhaps consider changing the step dynamically)
                else:
                    i = last_size
                print("Trying again with new size: ", last_size)
                    
            # This might be worth capturing as well, for internet errors
            # requests.exceptions.ConnectionError
                
    conn.close()

def parse_args():
    parser = argparse.ArgumentParser(description='Download files from github.')
    parser.add_argument('output', metavar='OUTPUT_FOLDER', type=str,
                   help='output folder to store the downloaded files')
    parser.add_argument('--init', dest='init', action='store', type=int,
                   default=None,
                   help='initial value in bytes')
    parser.add_argument('--step', dest='step', action='store', type=int,
                   default=5,
                   help='step')

    args = parser.parse_args()

    return args
    

# uses dates for filtering: https://github.com/PyGithub/PyGithub/issues/824
