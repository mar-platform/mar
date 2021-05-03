import csv
import os
import github_crawler as gc

if __name__ == "__main__":
    args = gc.parse_args()
    
    print("Generating in ", args.output)
    print("  with initial=", args.init)
    
    with open(os.path.join(args.output, 'ecore_download.csv'), 'a+', newline='') as file:
        writer = csv.writer(file)
        gc.process('EPackage', 'ecore', writer, args.output, args.init, 30_000_000, args.step)

        
# from github import Github
# import github
# import csv
# import random
# from pathlib import Path
# import os.path
# import time
# import base64
# import os
# import argparse
# from datetime import datetime, timezone

# # https://github.com/PyGithub/PyGithub
# # Token (Mining): ad9015f5f1517ed8e3865be6a47292201f17baad

# token=os.environ.get('GH_TOKEN')
# if token is None:
#     print("GH_TOKEN variable required")
#     exit(-1)

# g = Github(token, per_page=100)

# #from github import enable_console_debug_logging
# #enable_console_debug_logging()

# # files = g.search_code(query='bpmn+extension:bpmn+created_at:>2018-11-29')
# #files = g.search_code(query='extension:bpmn, created:2018-11-01..2018-11-29', per_page=100)
# #files = g.search_code(query='bpmn extension:bpmn created:2016-01-01..2018-11-29') #size:1..100')

# def api_wait_search(git):
#   limits = git.get_rate_limit()
#   print("   Limits: ", limits)
#   if limits.search.remaining <= 10:
#     seconds = (limits.search.reset - datetime.now()).total_seconds()
#     print("Waiting for %d seconds ..." % (seconds))
#     time.sleep(seconds)
#     print("Done waiting - resume!")

# def api_wait_search2(github):
#     limits = github.get_rate_limit()
#     reset = limits.search.reset.replace(tzinfo=timezone.utc)
#     now = datetime.now(timezone.utc)
#     seconds = (reset - now).total_seconds()
#     print(f"Rate limit exceeded")
#     print(f"Reset is in {seconds:.3g} seconds.")
#     if seconds > 0.0:
#         print(f"Waiting for {seconds:.3g} seconds...")
#         time.sleep(seconds)
#         print("Done waiting - resume!")
    
# def process(file, output_folder, init = 10_000, end = 1_000_000, step = 5):
#     total = 0

#     # step = 5 # very small steps to avoid loosing files
    
#     for i in range(init, end, step):
#         finished_chunk = False        
#         while not finished_chunk:
#             try:            
#                 size = 'size:' + str(i) + '..' + str(i + step - 1)        
#                 print("Processing with " + size)
#                 files = g.search_code(query='EPackage extension:ecore ' + size)
#                 print("   There are " , files.totalCount)
            
#                 for f in files:
#                     # api_wait_search(g)
            
#                     print("   Processing ", total, "... ", f.name)            
#                     #if i % 100 == 0:
#                     seconds = random.randint(1, 5)
#                     print("   Waiting ", seconds, " seconds")
#                     time.sleep(seconds)
                    
#                     # print(f.repository.name)
#                     # print(f.repository.owner.name)
#                     # print(f.repository.organization)
#                     name = f.repository.full_name
#                     path = Path(output_folder, "data", name, f.path);
#                     path.parents[0].mkdir(parents=True, exist_ok=True)
#                     text = base64.b64decode(f.content).decode('utf-8')
#                     with path.open("w") as target:
#                         target.write(text)
                        
#                     print("   Wrote to ", total, "...", path)
#                     writer.writerow([os.path.join("data", name, f.path), f.name, f.download_url, f.size])
            
#                     total = total + 1
                    
#                 finished_chunk = True
#             # except github.GithubException.RateLimitExceededException:
#             except Exception as exception:
#                 api_wait_search2(g)
                

                
# if __name__ == "__main__":
#     parser = argparse.ArgumentParser(description='Download ecore files from github.')
#     parser.add_argument('output', metavar='OUTPUT_FOLDER', type=str,
#                    help='output folder to store the downloaded files')
#     parser.add_argument('--init', dest='init', action='store', type=int,
#                    default=10_000,
#                    help='initial value in bytes')
#     parser.add_argument('--step', dest='step', action='store', type=int,
#                    default=5,
#                    help='step')

#     args = parser.parse_args()

#     print("Generating in ", args.output)
#     print("  with initial=", args.init)
    
#     with open(os.path.join(args.output, 'ecore_download.csv'), 'a+', newline='') as file:
#         writer = csv.writer(file)
#         process(file, args.output, args.init, 3_000_000, args.step)


# # uses dates for filtering: https://github.com/PyGithub/PyGithub/issues/824
