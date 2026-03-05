
Results are stored in /data3/supergraph


## Crawling
There are several crawlers for GitHub, stored in `./mar-crawlers/github/`. 
Each crawler must be executed independently and deals with a specific type of artifact.
The crawlers follow this strategy:
- Search for files with a specific extension (e.g., `.atl`) and some "content hint" (e.g., `transformation`)
- To overcome GitHub's limit of 1024 search results, the crawler proceeds by moving a window of a certain file size
  (e.g., files in the range of 128KB and 256KB, and so on).
- For each artefact, individual information available from GitHub is gathered

The crawlers have the option to download the files or just to generate a database with information about the
available files. Here we only need the information.

The file `common.py` describes which crawlers correspond to which technologies (so far, EMF, Spoofax, MPS). 
This is needed to split the later processing per technology.

## Downloading
We have a set of files of different type but *we need the repositories* that likely contain MDE projects.
The script `download_repos.py` is in charge of computing the set of unique repositories and downloading them.

```
python3 -u download_repos.py crawler://../mar-crawlers/github/crawled-data/:crawler://../mar-crawlers/github/data2:mar:///data2/mde-datasets/download /data3/supergraph/repos2  | tee git_download.log
```

The repositories are stored using the convention: `user/repo-name`.

## Consolidating the information

We read the information from the downloaded repositories in order do a post-processing step
to rule out certain repositories or invalid files.

```bash
python3 organize.py \
  -d /data3/supergraph/repos:/data3/supergraph/repos-mps:/data3/supergraph/repos-spoofax  \
  -o /data3/supergraph/organize5.db \
  -c configuration.yaml
```

## Extracting repository information

This step generates a complete database with the information about the repositories.
(*For some reason it is split by technology*: actually the reason is to be able to add new technologies as we go.
We started with EMF only, and then we added MPS and Spoofax.*)


This is done in two substeps:

* `collect_github_stats`: Connects to GitHub to extract information about each repository
```bash
python3 collect_github_stats.py /data3/supergraph/organize3.db /data3/supergraph/repo_info3.db any
  
# This was the old way
#  python3 collect_github_stats.py ../mar-crawlers/github/crawled-data/ /data3/supergraph/repo_info_spoofax.db spoofax
```

  This generates an "`info`" database. **TODO: FIND A BETTER NAME.** 

* `collect_git_stats`: in a second step the database is completed with information extracted from the actual Git repository
stored locally.

```bash
python3 collect_git_stats.py /data3/supergraph/repos/ /data3/supergraph/repo_info3.db
#  python3 collect_git_stats.py /data3/supergraph/repos-spoofax/ /data3/supergraph/repo_info_spoofax.db
```

The structure of the database is: 

```
CREATE TABLE repo_info (
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
  parent_repo text
);
CREATE TABLE repo_git (
  id VARCHAR(255) PRIMARY KEY,
  ci text, -- Path to CI witness
  readme_size integer,
  commits_per_month real,
  license text -- Path to the LICENSE witness
);
```

## Classification pipeline

The script `type_classification.py` uses an LLM to classify the repository.

```bash
python3 type_classification.py    \ 
  /data3/supergraph/repos/        \
  /data3/supergraph/repo_info_emf.db \
  /data3/supergraph/repo_classification_emf_qwen3.db --model qwen3:30b
```

To classify the repository we take into account the contents of the README.md file
(We could also use the contents of MDE artefacts. This another line.)
The result of the classification can be:

- No readme file. This set `error = True` and `readme = False`
- Readme too short. This set `error = True` and `readme = False`
- Error with the LLM. This set `error = True` and `readme = True`
- Successful classification. This set `error = False` and `readme = True`

The extracted information is:

```bash
CREATE TABLE repo_classification (
      id VARCHAR(255) PRIMARY KEY,
      type text,
      application_domain text,
      suggestion text,
      rationale text,
      error integer,
      readme_ok integer
    );
```

## Consolidating data

The information is stored per technology: EMF, Spoofax and MPS.
Everything is consolidated with:

```bash
python3 merge_info.py  /data3/supergraph/ ../../mar-crawlers/github/crawled-data/
```

The data is consolidated into `merged_info.db`

## Metrics


```bash
python3 compute_metrics.py  /data3/supergraph/merged_info.db
```