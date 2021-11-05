import csv
import os
import github_crawler as gc

if __name__ == "__main__":
    args = gc.parse_args_files()

    if args.files is None:
        print("Expected --filelist argument")
        exit(-1)
    
    with open(args.files) as f:
        files = f.read().splitlines()

        if not args.check:
            gc.process_single_files(files, args.output)
        else:
            with open(os.path.join(args.output, 'check_report.csv'), 'w', newline='') as report_file:
                writer = csv.writer(report_file)
                gc.check_single_files(files, writer)
