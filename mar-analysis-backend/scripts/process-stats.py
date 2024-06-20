
def main(args):
    import json

    # Load the json file
    data = json.load(args.file)

    rawFilesInDb = data['totalRawFiles']
    totalBuildFiles = data['totalBuildFiles']
    totalFilesWithError = data['totalFilesWithError']
    totalTypesNotConsidered = data['totalTypesNotConsidered']

    artefactsInDb = data['totalArtefacts']
    totalNotFoundInRawDB = data['totalNotFoundInRawDb']
    totalMissingArtefacts = data['totalMissingArtefacts']

    actualFiles = rawFilesInDb - totalBuildFiles - totalFilesWithError - totalTypesNotConsidered;
    actualArtefacts = artefactsInDb - totalNotFoundInRawDB - totalMissingArtefacts

    print("Actual files: ", actualFiles)
    print("Actual artefacts: ", actualArtefacts)
    print("Difference: ", actualFiles - actualArtefacts)

# Parse arguments with argparse, with one argument which is a json file
if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description='Process stats from a json file')
    parser.add_argument('file', type=argparse.FileType('r'), help='json file to process')

    args = parser.parse_args()

    main(args)