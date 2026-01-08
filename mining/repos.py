import sys

import sqlite3
import os.path

from collections import defaultdict
from git import Repo
import git.exc

def main(root, target, type='emf'):            
    from common import get_repos_by_type
    repos = get_repos_by_type(type)
    
    repo_artifact = defaultdict(list)

    for r in repos:        
        db_file = os.path.join(root, r, 'crawler.db')
        print("Opening ", db_file)
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

        url = url.replace('git://', 'https://')    
            
        target_folder = os.path.join(target, user, name)
        if os.path.exists(target_folder):
            print("Skipping existing", target_folder, "for", url)
            continue
            
        print("Cloning", url, "to", target_folder)
        try:
            Repo.clone_from(url, target_folder)
	#Repo.clone_from(url, target_folder, depth=1)	
        except git.exc.GitCommandError as err:
            print(err)
        
main(sys.argv[1], sys.argv[2], sys.argv[3])

