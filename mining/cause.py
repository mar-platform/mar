import argparse
import json
import os
import traceback
import subprocess


def parse_args():
    parser = argparse.ArgumentParser(description='Analyse the cause of missing files.')
    parser.add_argument("-r", "--root", dest='root', metavar='ROOT_FOLDER', type=str, required=True,
                        help='root folder of the repository')
    parser.add_argument("-i", "--input", dest='input', metavar='INPUT_FOLDER', type=str, required=True,
                        help='input file with the errors')
    parser.add_argument("-o", "--output", dest='output', metavar='OUTPUT_FOLDER', type=str, required=True,
                        help='output file with the causes')
    args = parser.parse_args()
    return args


RELEVANT_EXTENSIONS = {
    'epsilon': {'.launch', '.etl', '.evl', '.egl', '.egx', '.eol', '.ewl', '.eml', '.mig'},
}

def safe_decode(string):
    try:
        return string.decode('utf-8')
    except UnicodeDecodeError as e:
        return ""
              
def get_using_files(file: str, type: str, root: str):
    parts = file.split("/")
    name = os.path.basename(file)
    project = "/".join(parts[0:2])
    full_project = os.path.join(root, project)

    #result = subprocess.run(['git', 'grep', name], stdout=subprocess.PIPE, cwd=full_project)

    # Use ripgrep for better performance
    # rg --color never --no-heading 'JsonFormatter.cs' 
    result = subprocess.run(['rg', '--color', 'never', '--no-heading', name], stdout=subprocess.PIPE, cwd=full_project)

    output = safe_decode(result.stdout)
    output = output.strip()

    result = []
    for line in output.split("\n"):
        parts = line.split(":")
        if len(parts) < 2:
            continue

        full_file = parts[0]
        if full_file != file:
            result.append(full_file)

    return list(set(result))


def check_file(file: str, type: str, root: str):
    print("Processing ", file)
    parts = file.split("/")
    if len(parts) < 3:
        return "InvalidPath"

    filenames = get_using_files(file, type, root)

    if len(filenames) == 0:
        return "TrueIsolated"

    is_defined_in_java = False
    for full_file in filenames:
        filename = os.path.basename(full_file)
        if filename.endswith(".java"):
            is_defined_in_java = True
            continue

        relevant_extensions = RELEVANT_EXTENSIONS.get(type, {})
        extension = os.path.splitext(filename)[1]
        if extension in relevant_extensions:
            return "FalseIsolated"

    if is_defined_in_java:
        return "JavaDefined"

    return "Unknown"


if __name__ == "__main__":
    args = parse_args()
    root = args.root
    with open(args.input) as f:
        data = json.load(f)

    isolated = data["isolated"]
    for artifact_type in isolated:
        type = artifact_type["type"]
        by_cause = {}
        for file in artifact_type["files"]:
            try:
                cause = check_file(file, type, root)
            except:
                traceback.print_exc()
                continue

            if cause not in by_cause:
                by_cause[cause] = []
            by_cause[cause].append(file)
        artifact_type["by_cause"] = by_cause

    with open(args.output, "w") as f:
        json.dump(data, f, indent=4)
