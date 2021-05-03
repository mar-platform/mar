CREATE TABLE IF NOT EXISTS data (
  model_id text PRIMARY KEY,
  filename text,
  name text,
  download_url text,
  size integer,
  license text,
  repo_id VARCHAR(255),
  creation_date integer, -- unix time
  last_update integer, -- unix time
  description text,
  topics text,
  popularity int,
  authors text
);

CREATE TABLE IF NOT EXISTS repo_info (
  id VARCHAR(255) PRIMARY KEY,
  name text,
  full_name text,
  html_url,
  git_url text,
  stargazers_count integer,
  forks_count integer,
  topics text,
  creation_date integer,
  last_update integer,
  description text
)
