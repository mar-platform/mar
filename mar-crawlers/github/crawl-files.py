import csv
import os
import github_crawler as gc

if __name__ == "__main__":
    args = gc.parse_args()

    if args.files is None:
        print("Expected --filelist argument")
        exit(-1)
    
    with open(args.files) as f:
        files = f.read().splitlines()
        print("  with initial=", args.init)

        gc.process_single_files(files, args.output)
