import csv
import os
import github_crawler as gc

if __name__ == "__main__":
    args = gc.parse_args()
    
    print("Generating in ", args.output)
    print("  with initial=", args.init)
    
    with open(os.path.join(args.output, 'sculptor_download.csv'), 'a+', newline='') as file:
        writer = csv.writer(file)
        gc.process('Application', 'btdesign', writer, args.output, args.init, 30_000_000, args.step)
        gc.process('ApplicationPart', 'btdesign', writer, args.output, args.init, 30_000_000, args.step)



