import argparse
import os
import sys
# add ".." to path
sys.path.append(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))

from collect_repository_stats import create_table as create_info_table
from collect_git_stats import create_table as create_git_table
from common import get_repos_by_type

def create_artefact_type_tables(conn):
    sql = """
CREATE TABLE IF NOT EXISTS repo_artefacts (
  rowid INTEGER PRIMARY KEY,
  repo_id VARCHAR(255),
  artefact_type VARCHAR(255)
)"""

    conn.cursor().execute(sql)
    conn.commit()

def create_classification_table(conn):
    sql = """
    CREATE TABLE repo_classification (
      id VARCHAR(255),
      type text,
      application_domain text,
      suggestion text,
      rationale text,
      error integer,
      readme_ok integer,
      llm varchar(255),
      PRIMARY KEY(id, llm) 
    );
    """
    conn.cursor().execute(sql)
    conn.commit()

def repo_exists(id, conn):
    c = conn.cursor()
    c.execute('SELECT full_name FROM repo_info WHERE full_name = ?', (id,))
    r = c.fetchone() is not None
    c.close()
    return r

def process(db_root: str, crawler_root: str):
    import sqlite3

    # Open destination database
    conn = sqlite3.connect(os.path.join(db_root, 'merged_info.db'))
    create_info_table(conn)
    create_git_table(conn)
    create_artefact_type_tables(conn)
    create_classification_table(conn)
    cursor = conn.cursor()

    # Attach other DBs
    cursor.execute(f"ATTACH DATABASE '{os.path.join(db_root, 'repo_info_emf.db')}' AS emf")
    cursor.execute(f"ATTACH DATABASE '{os.path.join(db_root, 'repo_info_mps.db')}' AS mps")
    cursor.execute(f"ATTACH DATABASE '{os.path.join(db_root, 'repo_info_spoofax.db')}' AS spoofax")

    cursor.execute(f"ATTACH DATABASE '{os.path.join(db_root, 'repo_classification_emf_qwen3.db')}' AS emfc")
    cursor.execute(f"ATTACH DATABASE '{os.path.join(db_root, 'repo_classification_mps_qwen3.db')}' AS mpsc")
    cursor.execute(f"ATTACH DATABASE '{os.path.join(db_root, 'repo_classification_spoofax_qwen3.db')}' AS spoofaxc")

    cursor.execute("INSERT OR IGNORE INTO main.repo_info SELECT *, 'emf' as repo_type FROM emf.repo_info")
    cursor.execute("INSERT OR IGNORE INTO main.repo_info SELECT *, 'mps' as repo_type FROM mps.repo_info")
    cursor.execute("INSERT OR IGNORE INTO main.repo_info SELECT *, 'spoofax' as repo_type FROM spoofax.repo_info")

    cursor.execute("INSERT OR IGNORE INTO main.repo_classification SELECT *, 'qwen3' as repo_type FROM emfc.repo_classification")
    cursor.execute("INSERT OR IGNORE INTO main.repo_classification SELECT *, 'qwen3' as repo_type FROM mpsc.repo_classification")
    cursor.execute("INSERT OR IGNORE INTO main.repo_classification SELECT *, 'qwen3' as repo_type FROM spoofaxc.repo_classification")

    cursor.execute("INSERT OR IGNORE INTO main.repo_git SELECT * FROM emf.repo_git")
    cursor.execute("INSERT OR IGNORE INTO main.repo_git SELECT * FROM mps.repo_git")
    cursor.execute("INSERT OR IGNORE INTO main.repo_git SELECT * FROM spoofax.repo_git")

    conn.commit()

    # Detach
    cursor.execute("DETACH DATABASE emf")
    cursor.execute("DETACH DATABASE mps")
    cursor.execute("DETACH DATABASE spoofax")

    repos = get_repos_by_type("emf")
    for r in repos:
        db_file = os.path.join(crawler_root, r, 'crawler.db')
        print("Opening ", db_file)
        conn_repo = sqlite3.connect(db_file)

        for (id,) in conn.execute('SELECT id FROM repo_info'):
            if repo_exists(id, conn_repo):
                print(id, " found")
                conn.execute("INSERT INTO repo_artefacts(repo_id, artefact_type) VALUES (?, ?)", (id, r))

        conn_repo.close()

    conn.commit()

    # TODO: Somehow convert this into a materialized view
    #conn.execute("""
    #CREATE VIEW regular_repo_info AS
    #select id, (select count(*) from repo_artefacts r2 where r2.repo_id = r1.id) as ac from repo_info r1 where ac > 1 or id not in (select repo_id from repo_artefacts where artefact_type = 'emf-projects')
    #""")
    #conn.commit()

    print("Filtering pure EMF projects")
    conn.execute("""
    CREATE TABLE regular_repo_info AS 
    select *, (select count(*) from repo_artefacts r2 where r2.repo_id = r1.id) as ac from repo_info r1 where ac > 1 or id not in (select repo_id from repo_artefacts where artefact_type = 'emf-projects') 
    """)
    conn.commit()

    conn.execute("""
    create view all_data as select * from regular_repo_info r1, repo_git r2 where r1.id = r2.id;
    """)
    conn.commit()


    # select repo_id, (select count(*) from repo_artefacts r2 where r2.repo_id = r1.repo_id) as ac from repo_artefacts r1;
    # select count(*) from (select id, (select count(*) from repo_artefacts r2 where r2.repo_id = r1.id) as ac from repo_info r1 where ac > 1 or id not in (select repo_id from repo_artefacts where artefact_type = 'emf-projects'));

    # All repos which are not not purely emf
    # (select id, (select count(*) from repo_artefacts r2 where r2.repo_id = r1.id) as ac from repo_info r1 where ac > 1 or id not in (select repo_id from repo_artefacts where artefact_type = 'emf-projects')

    conn.close()

def parse_args():
    parser = argparse.ArgumentParser(description='Merge databases with repository information')
    parser.add_argument('db_root', metavar='REPO_ROOT', type=str,
                   help='folder with the databases')
    parser.add_argument('crawler_root', metavar='CRAWLER_ROOT', type=str,
                   help='folder with the crawler databases')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    process(args.db_root, args.crawler_root)