import sys

import sqlite3
import os.path

from collections import defaultdict
from git import Repo
import git.exc

def main(root):            
    repos = ['repo-github-ecore',
             'repo-github-qvto',
             'repo-github-atl',
             'repo-github-epsilon-etl',
             'repo-github-epsilon-eol',
             'repo-github-epsilon-etl',
             'repo-github-epsilon-egl',             
             'repo-github-jet',
             'repo-github-xtext',
             'repo-github-odesign',
             'repo-github-emfatic',
             'repo-github-acceleo',
             'repo-github-henshin',
             'repo-github-ocl',
             'repo-github-emftext']

    repos = ['acceleo',
             'atl',
             'ecore',
             'emfatic',
             'emf-projects',
             'emftext',
             'epsilon',
             'henshin',
             'ocl',
             'qvto',
             'sirius',
             'uml',
             'xtend',
             'xtext']

    
    repo_artifact = defaultdict(list)

    for r in repos:
        if r == 'emf-projects':
            continue
        
        db_file = os.path.join(root, r, 'crawler.db')
        print("Opening ", db_file)
        conn = sqlite3.connect(db_file)

        for (model_id, url) in conn.execute('SELECT model_id, git_url FROM data, repo_info where data.repo_id = repo_info.id'):
            repo_artifact[url].append(model_id)
                
        #folder = os.path(root, r)
        print("Total repos found so far", len(repo_artifact))

    # Get emf projects
    db_file = os.path.join(root, 'emf-projects', 'crawler.db')
    conn = sqlite3.connect(db_file)
    count = 0
    repos_emf = set()
    for (model_id, url) in conn.execute('SELECT model_id, git_url FROM data, repo_info where data.repo_id = repo_info.id'):
        if url not in repo_artifact and url not in repos_emf:
            count = count + 1
            repos_emf.add(url)
            print(url)
    print("Total pure emf-projects: ", count)
            
main(sys.argv[1])

