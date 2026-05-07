import json
import sqlite3
from typing import Optional
from functools import lru_cache

from git import Repo
from ollama import chat
from ollama import ChatResponse
import argparse
import os
from dataclasses import dataclass

from termcolor import colored

from analysis_common import get_repos, get_repo, find_readme_file

# Module-level connection (initialized in process())
_db_conn = None

@dataclass
class RepoClasification:
    id: str
    type: Optional[str]
    rationale: str
    application_domain: Optional[str]
    suggestion: Optional[str]
    error: bool = False
    readme_ok: bool = True


#Common meta-tools organized by category:
#- EMF (Eclipse Modeling Framework): Meta-modeling framework
#- Xtext, EMFText, TCS: For creating textual DSLs
#- Sirius, GMF, GEF, Graphitti, Eugenia: For creating graphical DSLs
#- ATL, Epsilon, ETL, EOL, EVL, Henshin, Acceleo, Mofscript, Xtend: Model transformation languages

PROMPT = """
Your task is to help me classify a Model-Driven Engineering (MDE) project into a category.
I will give you a README.md file from a project. Tell me to which category the project belongs to:

* Meta-tool: A tool whose aim is the construction of MDE tools. A meta-tool can be used to build DSLs, editors, transformations, code generators, etc. This also includes tools and extensions to support other meta-tools (e.g., an static analyser, a testing tool, et.).
* Domain tool: A tool intended to automate or help the development in some application domain. Typically based on a DSL or a modeling language (ej., UML) and provides associated facilities like generators, validators, simulators, etc.
* Example repository: A repository of examples rather than a real tool.
* Research data repository: A repository containing data produced by some research, instead of a real tool.
* Other. When the tool doesn't fit in the previous categories. If the README doesn't provide enough information, use this category. Provide a suggestion for the category.

When analysing the text, take into account the following terms related to MDE:
- EMF (Eclipse Modeling Framework): Meta-modeling framework
- Xtext, EMFText, TCS: For creating textual DSLs
- Sirius, GMF, GEF, Graphitti, Eugenia: For creating graphical DSLs
- ATL, Epsilon, ETL, EOL, EVL, Henshin, Acceleo, Mofscript, Xtend: Model transformation languages.

Think about the rationale for your decision.

The answer MUST be in json. Mark it with ```json, using the following format:
```json
{
    "category": "<the given category: meta-tool, domain-tool, example-repository, research-data, other>",
    "rationale": "<the rationale for the decision>"
    "application-domain": "<if it is domain tool>",
    "suggestion": "<if it is other>",
}
```

Contents of the README.md file:

"""

#Common meta-tools organized by category:
#- Meta-modeling frameworks: EMF (Eclipse Modeling Framework)
#- For creating textual DSLs: Xtext, EMFText, TCS
#- For creating graphical DSLs: Sirius, GMF, GEF, Graphitti, Eugenia
#- Model transformation languages: ATL, Epsilon, ETL, EOL, EVL, Henshin, Acceleo, Mofscript, Xtend


class ShortReadme(Exception):
    pass


def count_tokens(contents):
    return len(contents) / 4

def get_prompt(readme_file, description, context_size = 32768):
    # read readme into a string
    with open(readme_file, errors='ignore') as f:
        contents = f.read()
        # At least roughly two lines
        if len(contents) < 80 * 2:
            raise ShortReadme()

        prompt = PROMPT + contents
        if description is not None:
            prompt = prompt + "\nAdditional project description: " + description

        #print("----------")
        #print(prompt)
        #print("----------")

        if count_tokens(prompt) > context_size:
            print("Triming prompt")
            prompt = prompt[:context_size - 1]

        return prompt


def is_openai(model: str):
    return model in ['gpt-5-mini', 'gpt-5-nano']

# To change Ollama context size: https://blog.driftingruby.com/ollama-context-window/
def invoke_llm(readme_file, description, model, attempts = 0):
    prompt = get_prompt(readme_file, description)
    #model = 'gemma3:4b'
    #model = 'llama3.2'
    #model = 'gemma3:27b'

    if is_openai(model):
        from openai import OpenAI

        client = OpenAI()
        resp = client.chat.completions.create(model=model,  # cheaper and faster than GPT-4
        messages=[ { 'role': 'user', 'content': prompt, } ],
        # max_tokens=200,  # adjust based on how long you want the answer
        #temperature=0.0
        )

        content = resp.choices[0].message.content
    else:
        # Assume Ollama
        try:
            response: ChatResponse = chat(model=model, messages=[{'role': 'user', 'content': prompt, }, ])
            content = response.message.content
        except:
            print("Error processing ", readme_file)
            if attempts < 2:
                return invoke_llm(readme_file, description, model, attempts + 1)

            # TODO: Perhaps indicate this error somehow
            return None

    return process_response(content)


def process_response(content):
    print(content)
    # print(colored(content, 'orange'))
    try:
        if content.startswith('```json'):
            content = content.strip().replace('```json', '').replace('```', '')
        else:
            # This is for qwen3, which always provides an explanation before the result <think></think>
            idx = content.find("```json")
            if idx == -1:
                return None
            content = content[idx:].replace('```json', '').replace('```', '')

        result = json.loads(content)
        print(result)
        if not "suggestion" in result:
            result["suggestion"] = None
        if not "application-domain" in result:
            result["application-domain"] = None
        if not "rationale" in result:
            result["rationale"] = None
        if not "category" in result:
            if "classification" in result:
                result["category"] = result["classification"]
            else:
                return None

        return result
    except:
        return None


def create_target_db(target_db_file):
    target_db_conn = sqlite3.connect(target_db_file)
    sql = """
    CREATE TABLE IF NOT EXISTS repo_classification (
      id VARCHAR(255) PRIMARY KEY,
      type text,
      application_domain text,
      suggestion text,
      rationale text,
      error integer,
      readme_ok integer
    )"""

    target_db_conn.cursor().execute(sql)
    target_db_conn.commit()

    return target_db_conn

def classify_repo(root, repo_id, description, model):
    readme_file = find_readme_file(os.path.join(root, repo_id))
    if readme_file is None:
        return RepoClasification(repo_id, None, None, None, None, True, False)

    try:
        result = invoke_llm(readme_file, description, model)
    except ShortReadme:
        return RepoClasification(repo_id, None, None, None, None, True, False)

    if result is not None:
        print(repo_id, "\n", colored(json.dumps(result, indent=2), 'green'))
        return RepoClasification(repo_id, result['category'], result['rationale'], result['application-domain'], result['suggestion'], False)
    else:
        return RepoClasification(repo_id, None, None, None, None, True, True)



def insert_data(conn, repo):
    c = conn.cursor()

    c.execute('INSERT INTO repo_classification(id, type, rationale, application_domain, suggestion, error, readme_ok) VALUES (?, ?, ?, ?, ?, ?, ?)',
              (repo.id, repo.type, repo.rationale, repo.application_domain, repo.suggestion, repo.error, repo.readme_ok))
    conn.commit()
    c.close()

@lru_cache(maxsize=1_000_000)
def already_processed(repo_id):
    """Check if repo has already been processed using cached lookup"""
    c = _db_conn.cursor()
    c.execute('SELECT id FROM repo_classification WHERE id = ?', (repo_id,))
    r = c.fetchone() is not None
    c.close()
    return r


def process(root, repo_db_file, target_db_file, model):
    global _db_conn
    repo_db = sqlite3.connect(repo_db_file)
    target_db = create_target_db(target_db_file)
    _db_conn = target_db

    repos = get_repos(repo_db)
    for repo_id, description in repos:
        try:
            # Main program code here
            if already_processed(repo_id):
                print("Already processed", repo_id)
                continue

            print("Processing ", repo_id)
            classification = classify_repo(root, repo_id, description, model)
            insert_data(target_db, classification)
            print(" - Classified as ", classification.type, classification.application_domain, classification.suggestion, classification.error)
        except KeyboardInterrupt:
          print("Ctrl-C pressed!")
          import sys
          sys.exit(0)


    repo_db.close()
    target_db.close()


def parse_args():
    parser = argparse.ArgumentParser(description='Analyse project types using an LLM.')
    parser.add_argument('root', metavar='REPO_ROOT', type=str,
                   help='folder with the code repository')
    parser.add_argument('repo_db', metavar='REPO_DB', type=str,
                   help='db with the repository info')
    parser.add_argument('classification_db', metavar='CLASSIFICATION_DB', type=str,
                   help='db with the classified info (will be written)')
    parser.add_argument('--only', metavar='ONLY', type=str, required=False,
                   help='If you want to test just one repository')
    parser.add_argument('--model', metavar='MODEL', type=str, required=False, default='gemma3:27b',
                   help='The LLM to use')
    parser.add_argument('--fresh', action="store_true", default=False,
                   help='To continue or delete the database')

    args = parser.parse_args()

    return args

if __name__ == "__main__":
    args = parse_args()
    if args.only:
        repo = args.only
        readme_file = os.path.join(args.root, repo, 'README.md')
        repo_db = sqlite3.connect(args.repo_db)
        repo_id, description = get_repo(repo_db, repo)
        print(description)
        result = invoke_llm(readme_file, description, args.model)
        print(result)
        repo_db.close()
    else:
        if args.fresh:
            os.remove(args.classification_db)

        process(args.root, args.repo_db, args.classification_db, args.model)
