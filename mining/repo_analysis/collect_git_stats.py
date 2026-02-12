import argparse
import json
import sqlite3
from dataclasses import dataclass
from git import Repo
from datetime import datetime
from collections import Counter
import os

from typing import Dict, Any, Optional

from analysis_common import get_repos, get_repo, find_readme_file, find_file_from_variants


@dataclass
class GitInfo:
    id: str
    ci: Optional[str]
    commits_per_month: float
    readme_size: int
    license: Optional[str]

# % GitHub Actions => .github/workflows
# % CircleCI => .circleci/config.yml
# % Travis CI => .travis.yml
# % Jenkins => Jenkinsfile
def ci_info(path):
    if os.path.exists(os.path.join(path, ".github/workflows")):
        return ".github/workflows"
    elif os.path.exists(os.path.join(path, ".circleci")):
        return ".circlecli"
    elif os.path.exists(os.path.join(path, ".travis.yml")):
        return ".travis.yml"
    elif os.path.exists(os.path.join(path, "Jenkinsfile")):
        return "Jenkinsfile"
    return None

def compute_commit_frequency(repo_path: str) -> float:
    if not os.path.isdir(repo_path):
        raise ValueError("Provided path does not exist or is not a directory.")

    if not os.path.isdir(os.path.join(repo_path, ".git")):
        raise ValueError("Provided path is not a Git repository.")

    repo = Repo(repo_path)
    if repo.bare:
        raise ValueError("Repository is bare.")

    commits = list(repo.iter_commits())
    if not commits:
        return 0.0  # No commits in the repository

    # Extract year-month from each commit
    year_months = [datetime.fromtimestamp(commit.committed_date).strftime("%Y-%m") for commit in commits]
    count_by_month = Counter(year_months)

    total_commits = sum(count_by_month.values())
    total_months = len(count_by_month)

    return total_commits / total_months if total_months > 0 else 0.0

def measure_readme(repo_path: str):
    f = find_readme_file(repo_path)
    if f is not None:
        return os.path.getsize(f)
    return 0

def analyse_repo(root, repo_id) -> GitInfo:
    path = os.path.join(root, repo_id)
    ci = ci_info(path)
    commit_frequency = compute_commit_frequency(path)
    readme_size = measure_readme(path)
    license = find_file_from_variants(path, ['LICENSE', 'COPYING'])
    return GitInfo(
        id=repo_id,
        ci=ci,
        commits_per_month=commit_frequency,
        readme_size=readme_size,
        license=license,
    )

def insert_data(id, data : GitInfo, conn):
    c = conn.cursor()
    c.execute('INSERT INTO repo_git(id, ci, readme_size, commits_per_month, license)'
              'VALUES(?, ?, ?, ?, ?)',
              (id, data.ci, data.readme_size, data.commits_per_month, data.license))
    conn.commit()
    c.close()

def repo_already_inserted(id, conn):
    c = conn.cursor()
    c.execute('SELECT id FROM repo_git WHERE id = ?', (id,))
    r = c.fetchone() is not None
    c.close()
    return r

def process(root, repo_db_file):
    repo_db = sqlite3.connect(repo_db_file)
    create_table(repo_db)

    repos = get_repos(repo_db)
    for repo_id, description in repos:
        if repo_already_inserted(repo_id, repo_db):
            print("Already processed", repo_id)
            continue

        if not os.path.exists(os.path.join(root, repo_id)):
            print("Repository ", repo_id, " does not exist.")
            continue

        print("Processing ", repo_id)
        analysis = analyse_repo(root, repo_id)
        insert_data(repo_id, analysis, repo_db)

    repo_db.close()

def create_table(conn):
    sql = """
CREATE TABLE IF NOT EXISTS repo_git (
  id VARCHAR(255) PRIMARY KEY,
  ci text, -- Path to CI witness
  readme_size integer,
  commits_per_month real,
  license text -- Path to the LICENSE witness
)"""

    conn.cursor().execute(sql)
    conn.commit()

def parse_args():
    parser = argparse.ArgumentParser(description='Collect repository stats from Git.')
    parser.add_argument('root', metavar='REPO_ROOT', type=str,
                   help='folder with the repository databases')
    parser.add_argument('target_db', metavar='TARGET_DB', type=str,
                   help='db with the recovered data')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    processed_projects = set()
    for input_folder in args.root.split(":"):
        process(input_folder, args.target_db)
