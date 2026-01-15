import argparse
import json
import sqlite3
import os

from github import Github
from typing import Dict, Any

from common import get_repos_by_type


def get_repo_stats(repo_id: int | str, *, token: str | None = None) -> Dict[str, Any]:
    """
    Return a dictionary with high-level statistics for the GitHub repository
    identified by `repo_id` (numeric ID or "owner/name" slug).

    Parameters
    ----------
    repo_id : int | str
        The GitHub repository ID (e.g. 28457823) **or** "owner/repo" string
        (e.g. "octocat/Hello-World").
    token : str | None, keyword-only
        A personal-access token.
        • If provided, you’ll avoid rate limits and can access private repos.
        • If omitted, the call is unauthenticated and limited.

    Returns
    -------
    dict
        {
            "name": str,
            "full_name": str,
            "private": bool,
            "description": str | None,
            "created_at": datetime,
            "updated_at": datetime,
            "pushed_at": datetime,
            "size_kb": int,
            "open_issues": int,
            "closed_issues": int,
            "open_prs": int,
            "closed_prs": int,
            "merged_prs": int,
            "stars": int,
            "watchers": int,
            "forks": int,
            "subscribers": int,
        }
    """
    gh = Github(token or None)           # Uses anonymous mode if token is None

    # PyGithub lets you fetch by numeric ID or "owner/name" slug
    repo = gh.get_repo(repo_id)

    # Issues (state='all' includes PRs, so filter them out)
    open_issues = repo.get_issues(state="open").totalCount
    closed_issues = repo.get_issues(state="closed").totalCount

    # Pull requests
    open_prs    = repo.get_pulls(state="open").totalCount
    closed_prs  = repo.get_pulls(state="closed").totalCount

    # Topics (requires media preview header, but PyGithub handles it)
    topics = repo.get_topics()

    # Labels (on the repository, not per issue)
    labels = [label.name for label in repo.get_labels()]

    contributors = [
        {"login": user.login, "contributions": user.contributions}
        for user in repo.get_contributors()
    ]

    parent_repo = None
    if repo.fork:
        parent_repo = repo.parent.full_name


    return {
        "name": repo.name,
        "full_name": repo.full_name,
        "private": repo.private,
        "description": repo.description,
        "created_at": repo.created_at,
        "updated_at": repo.updated_at,
        "pushed_at": repo.pushed_at,
        "size_kb": repo.size,
        "open_issues": open_issues,
        "closed_issues": closed_issues,
        "total_issues": open_issues + closed_issues,
        "open_prs": open_prs,
        "closed_prs": closed_prs,
        "total_prs": open_prs + closed_prs,
        "stars": repo.stargazers_count,
        "watchers": repo.watchers_count,
        "forks": repo.forks_count,
        "subscribers": repo.subscribers_count,
        "topics": topics,
        "labels": labels,
        "contributors": contributors,
        "parent_repo": parent_repo
    }

def insert_data(id, repo_type, data, conn):
    c = conn.cursor()
    topics_str = ', '.join(data["topics"])
    labels_str = ', '.join(data["labels"])
    contributor_count = len(data["contributors"])
    contributor_detail = json.dumps(data["contributors"], indent=2)

    c.execute('INSERT INTO repo_info(id, repo_type, name, full_name, description,'
              'total_issues, total_prs, stars, watchers, forks, subscribers,'
              'topics, labels, created_at, updated_at,'
              'contributors_count, contributors_detail, parent_repo)'
              'VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
              (id, repo_type, data["name"], data["full_name"], data["description"],
               data["total_issues"], data["total_prs"], data["stars"], data["watchers"],
               data["forks"], data["subscribers"], topics_str, labels_str,
               data["created_at"].isoformat(), data["updated_at"].isoformat(),
               contributor_count, contributor_detail, data["parent_repo"]))
    conn.commit()
    c.close()

def repo_already_inserted(id, conn):
    c = conn.cursor()
    c.execute('SELECT id FROM repo_info WHERE id = ?', (id,))
    r = c.fetchone() is not None
    c.close()
    return r

def process(root, db, repo_type):
    target_db_conn = sqlite3.connect(db)
    create_table(target_db_conn)

    repos = get_repos_by_type(repo_type)
    token = os.environ.get('GH_TOKEN')
    if token is None:
        print("GH_TOKEN variable required")
        exit(-1)

    repos_checked = set()
    for r in repos:
        db_file = os.path.join(root, r, 'crawler.db')
        print("Opening ", db_file)
        conn = sqlite3.connect(db_file)

        for (id, name) in conn.execute('SELECT full_name, name FROM repo_info group by full_name'):
            if id in repos_checked:
                print("Already checked ", id)
                continue
            
            if repo_already_inserted(id, target_db_conn):
                print("Already inserted ", id)
                continue

            print("Analysing ", name, " - ", id)
            try:
                data = get_repo_stats(id, token=token)
            except Exception as e:
                print("Error analysing ", id)

            repos_checked.add(id)
                
            if data["private"]:
                continue

            insert_data(id, repo_type, data, target_db_conn)

        conn.close()

    target_db_conn.close()

def create_table(conn):
    sql = """
CREATE TABLE IF NOT EXISTS repo_info (
  id VARCHAR(255) PRIMARY KEY,
  name text,
  full_name text,
  description text,
  total_issues integer,
  total_prs integer,
  stars integer,
  watchers integer,
  forks integer,
  subscribers integer,
  topics text,
  labels text,
  created_at text,
  updated_at text,
  contributors_count integer,
  contributors_detail text,
  parent_repo text,
  repo_type varchar(255)
)"""

    conn.cursor().execute(sql)
    conn.commit()

def parse_args():
    parser = argparse.ArgumentParser(description='Collect repository stats from GitHub.')
    parser.add_argument('root', metavar='REPO_ROOT', type=str,
                   help='folder with the repository databases')
    parser.add_argument('target_db', metavar='TARGET_DB', type=str,
                   help='db with the recovered data')
    parser.add_argument('repo_type', metavar='repository_type', type=str,
                   help='Repository type (emf, mps, spoofax)')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    process(args.root, args.target_db, args.repo_type)
