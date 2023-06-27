import argparse
import json
import os


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


def check_file(file: str, type: str, root: str):
    print("Processing ", file)

    parts = file.split("/")
    if len(parts) < 3:
        return "InvalidPath"

    name = os.path.basename(file)
    project = "/".join(parts[0:2])
    full_project = os.path.join(root, project)

    import subprocess
    result = subprocess.run(['git', 'grep', name], stdout=subprocess.PIPE, cwd=full_project)
    output = result.stdout.decode('utf-8')
    output = output.strip()

    if len(output) == 0:
        return "TrueIsolated"

    is_defined_in_java = False
    for line in output.split("\n"):
        parts = line.split(":")
        if len(parts) < 2:
            continue

        full_file = parts[0]
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
            cause = check_file(file, type, root)
            if cause not in by_cause:
                by_cause[cause] = []
            by_cause[cause].append(file)
        artifact_type["by_cause"] = by_cause

    with open(args.output, "w") as f:
        json.dump(data, f, indent=4)

