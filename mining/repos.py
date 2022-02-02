import sys

import sqlite3
import os.path

from collections import defaultdict
from git import Repo
import git.exc

def main(root, target):
    repos = ['repo-github-ecore',
             'repo-github-qvto',
             'repo-github-atl']

    repo_artifact = defaultdict(list)

    for r in repos:        
        db_file = os.path.join(root, r, 'crawler.db')
        conn = sqlite3.connect(db_file)

        for (model_id, url) in conn.execute('SELECT model_id, git_url FROM data, repo_info where data.repo_id = repo_info.id'):
            repo_artifact[url].append(model_id)
                
        #folder = os.path(root, r)
        print("Total repos found so far", len(repo_artifact))

    for url in repo_artifact:
        user = url.split('/')[-2]
        name = url.split('/')[-1]
        if name.endswith('.git'):
            name = name[:-len('.git')]
            
        target_folder = os.path.join(target, user, name)
        if os.path.exists(target_folder):
            print("Skipping existing", target_folder, "for", url)
            continue
            
        print("Cloning", url, "to", target_folder)
        try:
            Repo.clone_from(url, target_folder)
        except git.exc.GitCommandError as err:
            print(err)
        
main(sys.argv[1], sys.argv[2])

