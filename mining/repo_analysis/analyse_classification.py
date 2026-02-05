import pandas as pd
import argparse

def process(database: str):
    import sqlite3

    conn = sqlite3.connect(database)
    # CREATE TABLE repo_classification (
    #       id VARCHAR(255),
    #       type text,
    #       application_domain text,
    #       suggestion text,
    #       rationale text,
    #       error integer,
    #       readme_ok integer,
    #       llm varchar(255),
    #       PRIMARY KEY(id, llm)
    #     );

    # List all id for which the type differs
    cursor = conn.cursor()

    query = """
    SELECT id, type, llm
    FROM repo_classification
    WHERE id IN (
        SELECT id
        FROM repo_classification
        GROUP BY id
        HAVING COUNT(DISTINCT type) > 1
    )
    ORDER BY id;
    """

    cursor.execute(query)
    rows = cursor.fetchall()

    #for repo_id, repo_type, llm in rows:
    #    print(repo_id, repo_type, llm)

    from collections import defaultdict

    conflicts = defaultdict(list)

    for repo_id, repo_type, llm in rows:
        conflicts[repo_id].append((repo_type, llm))

    # Example usage
    for repo_id, values in conflicts.items():
        print(f"\n{repo_id}")
        for repo_type, llm in values:
            print(f"  {repo_type} ({llm})")

    print("Total differences: ", len(conflicts))

    # Close the connection (optional but recommended)
    conn.close()



def parse_args():
    parser = argparse.ArgumentParser(description='Merge databases with repository information')
    parser.add_argument('database', metavar='DATABASE', type=str,
                   help='the database')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    process(args.database)