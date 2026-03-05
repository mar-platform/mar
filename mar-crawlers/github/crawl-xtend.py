import csv
import os
import github_crawler as gc

if __name__ == "__main__":
    args = gc.parse_args()
    
    print("Generating in ", args.output)
    print("  with initial=", args.init)
    print("       save_contents=", args.save_contents)
    with open(os.path.join(args.output, 'xtend_download.csv'), 'a+', newline='') as file:
        writer = csv.writer(file)
        gc.process('package', 'xtend', writer, args.output, args.init, 30_000_000, args.step, args.save_contents)

        
