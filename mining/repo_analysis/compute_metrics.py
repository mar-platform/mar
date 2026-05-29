import pandas as pd
import argparse

def normalize_95(df, column_name, percentile=0.95):
    p95 = df[column_name].quantile(percentile)
    print(f"95th percentile of {column_name}:", p95)
    df[column_name + '_norm'] = df[column_name].apply(
        lambda x: x / p95 if x <= p95 else 1.0
    )

def normalize_1_0(df, column_name):
    df[column_name + '_norm'] = df[column_name].apply(
        lambda x: 0 if x is None else 1
    )


def plot_chat(df):
    import matplotlib.pyplot as plt

    df_sorted = df.sort_values(by='score', ascending=False)
    # Plot bar chart
    plt.figure(figsize=(12, 6))
    plt.bar(df_sorted['id'].astype(str), df_sorted['score'], color='skyblue')
    plt.xticks(rotation=90)
    plt.xlabel('Repository ID')
    plt.ylabel('Engineering Score')
    plt.title('Engineering Score per Repository')
    plt.tight_layout()

    #plt.show()
    plt.savefig('/tmp/results.png')
    plt.close()


def process(database: str):
    import sqlite3

    conn = sqlite3.connect(database)
    # Load all_data table into pandas
    # Load the table into a pandas DataFrame
    df = pd.read_sql_query("SELECT * FROM all_data", conn)

    # Close the connection (optional but recommended)
    conn.close()

    # Display the DataFrame
    print(df.head())

    normalize_95(df, 'commits_per_month')
    normalize_95(df, 'contributors_count')
    normalize_95(df, 'total_prs')
    normalize_95(df, 'stars')
    normalize_95(df, 'total_issues')
    normalize_1_0(df, 'ci')
    normalize_1_0(df, 'license')
    normalize_95(df, 'readme_size')

    weights = {
        'commits_per_month_norm': 1.0,
        'contributors_count_norm': 1.0,
        'total_prs_norm': 1.0,
        'stars_norm': 1.0,
        #'total_issues_norm': 1.0,
        'ci_norm': 1.0,
        'license_norm': 1.0,
        'readme_size_norm': 1.0
    }

    # Compute weighted sum
    df['score'] = sum(df[col] * w for col, w in weights.items()) / sum(weights.values())

    #plot_chat(df)
    #cluster(df)

    #cluster_1 = df[df['cluster'] == 0].sort_values(by='score', ascending=False)
    #print(cluster_1.head(20))

    print(df.sort_values(by='score', ascending=False).head(20))

def cluster(df):
    from sklearn.cluster import KMeans

    # List of normalized columns
    norm_cols = [
        'commits_per_month_norm',
        'contributors_count_norm',
        'total_prs_norm',
        #'total_issues_norm',
        'stars_norm',
        'ci_norm',
        'license_norm',
        'readme_size_norm'
    ]

    X = df[norm_cols]
    kmeans = KMeans(n_clusters=2, random_state=12)
    df['cluster'] = kmeans.fit_predict(X)
    cluster_summary = df.groupby('cluster')[norm_cols + ['score']].mean()
    print(cluster_summary)

    plot_pca(df, X)
    plot_pca3d(df, X)

def plot_pca(df, X):
    from sklearn.decomposition import PCA
    import matplotlib.pyplot as plt

    # Reduce to 2D
    pca = PCA(n_components=2)
    components = pca.fit_transform(X)

    # Plot
    plt.figure(figsize=(8, 6))
    plt.scatter(components[:, 0], components[:, 1], c=df['cluster'], cmap='Accent', s=50)
    plt.xlabel('PCA 1')
    plt.ylabel('PCA 2')
    plt.title('K-Means Clustering of Repositories')
    plt.tight_layout()
    plt.savefig('/tmp/clusters_pca_plot.png')
    plt.close()

def plot_pca3d(df, X):
    from sklearn.decomposition import PCA
    import matplotlib.pyplot as plt
    from mpl_toolkits.mplot3d import Axes3D  # Required for 3D plotting

    # Step 1: Fit PCA with 3 components
    pca = PCA(n_components=3)
    components = pca.fit_transform(X)

    # Step 2: Add PCA components to the DataFrame (optional)
    df['pca1'] = components[:, 0]
    df['pca2'] = components[:, 1]
    df['pca3'] = components[:, 2]

    # Step 3: Plot 3D scatter
    fig = plt.figure(figsize=(10, 8))
    ax = fig.add_subplot(111, projection='3d')

    scatter = ax.scatter(df['pca1'], df['pca2'], df['pca3'],
                         c=df['cluster'], cmap='Accent', s=50)

    ax.set_xlabel('PCA 1')
    ax.set_ylabel('PCA 2')
    ax.set_zlabel('PCA 3')
    ax.set_title('3D PCA of Repositories by Cluster')
    plt.tight_layout()
    plt.savefig('/tmp/clusters_pca_3d.png')
    plt.close()


def parse_args():
    parser = argparse.ArgumentParser(description='Merge databases with repository information')
    parser.add_argument('database', metavar='DATABASE', type=str,
                   help='the database')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    process(args.database)