import os

def get_repos(repo_db):
    repos = []
    for (id, name, description) in repo_db.execute('SELECT id, name, description FROM repo_info'):
        repos.append((id, description))
    return repos


def get_repo(repo_db, repo_id):
    cursor = repo_db.execute('SELECT id, name, description FROM repo_info where id = ?', (repo_id,))
    id, name, description = cursor.fetchone()
    return (id, description)


def find_readme_file(repo_path: str):
    variants = ['README.md', 'ReadMe.md', "readme.md", "readme.MD", "Readme.md"]
    return find_file_from_variants(repo_path, variants)

def find_file_from_variants(repo_path: str, variants: list[str]):
    for v in variants:
        file = os.path.join(repo_path, v)
        if os.path.exists(file):
            return file

    return None