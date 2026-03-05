import argparse
import re
import sys

import sqlite3
import os.path

from collections import defaultdict
from git import Repo
import git.exc

def extract_from_crawler(root, type='emf'):
    from common import get_repos_by_type
    repos = get_repos_by_type(type)

    repo_artifact = defaultdict(list)

    result = set()
    for r in repos:
        db_file = os.path.join(root, r, 'crawler.db')
        if not os.path.exists(db_file):
            print("No crawler ", db_file)
            continue

        print("Opening ", db_file)
        conn = sqlite3.connect(db_file)

        for (model_id, url) in conn.execute(
                'SELECT model_id, git_url FROM data, repo_info where data.repo_id = repo_info.id'):
            result.add(url)

    return result

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

def download_all_repos(all_repos: str, target: str):
    # Disable interactive git prompts (important!)
    os.environ["GIT_TERMINAL_PROMPT"] = "0"

    for url in all_repos:
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
        # Repo.clone_from(url, target_folder, depth=1)
        except git.exc.GitCommandError as err:
            print(err)

def parse_args():
    parser = argparse.ArgumentParser(description='Collect repository stats from GitHub.')
    parser.add_argument('sources', metavar='SOURCES', type=str,
                   help='colon separated list of sources of repositories')
    parser.add_argument('target', metavar='TARGET', type=str,
                   help='folder to store the repos')
    parser.add_argument('--repo_type', metavar='repository_type', type=str, default='emf', required=False,
                   help='Repository type (emf, mps, spoofax)')
    parser.add_argument('--dry', action='store_true',
                   help='Download or not')
    return parser.parse_args()

mar_folders = [
    'repo-github-ecore',
    'repo-github-xtext',
#    'repo-github-archimate',
#    'repo-github-pnml',
#    'repo-github-uml',
#    'repo-simulink-emf'
]

def extract_from_mar_dataset(root):
    all_repos = set()
    for mar_folder in mar_folders:
        repos = extract_from_mar_folder(os.path.join(root, mar_folder, "data"))
        all_repos.update(repos)
    return all_repos

def extract_from_mar_folder(root):
    from pathlib import Path
    root_path = Path(root)
    repositories = []

    if not root_path.exists() or not root_path.is_dir():
        return repositories

    # Iterate over top-level user directories
    for user_dir in root_path.iterdir():
        if user_dir.is_dir():
            # Iterate over repositories inside each user directory
            for repo_dir in user_dir.iterdir():
                if repo_dir.is_dir():
                    repositories.append(f"git://github.com/{user_dir.name}/{repo_dir.name}.git")

    return repositories

if __name__ == "__main__":
    args = parse_args()
    repo_sources = args.sources
    target = args.target

    all_repo_sources = parts = re.split(r':(?=(crawler|mar)://)', repo_sources)
    print("Repository sources: ", all_repo_sources)
    all_repos = set()
    for source in all_repo_sources:
        if source.startswith("crawler://"):
            repos = extract_from_crawler(source.replace("crawler://", ""))
            all_repos.update(repos)
        if source.startswith("mar://"):
            repos = extract_from_mar_dataset(source.replace("mar://", ""))
            all_repos.update(repos)

    print("\n".join(all_repos))
    print("Total repos: ", len(all_repos))

    if not args.dry:
        download_all_repos(all_repos, target)


#main(sys.argv[1], sys.argv[2], sys.argv[3])

