repos = ['repo-github-ecore',
         'repo-github-qvto',
         'repo-github-atl',
         'repo-github-epsilon-etl',
         'repo-github-epsilon-eol',
         'repo-github-epsilon-etl',
         'repo-github-epsilon-egl',
         'repo-github-jet',
         'repo-github-xtext',
         'repo-github-odesign',
         'repo-github-emfatic',
         'repo-github-acceleo',
         'repo-github-henshin',
         'repo-github-ocl',
         'repo-github-emftext']

repos_emf = ['acceleo',
         'atl',
         'ecore',
         'emfatic',
#         'emf-projects',
         'emftext',
         'epsilon', # Sometimes in Epsilon folder with everything, sometimes separated
         'eol', 'etl', 'evl',
         'henshin',
         'ocl',
         'qvto',
         'sirius',
#         'uml',
         'vql',
         'xtend',
         'xcore',
         'xtext']

repos_mps = ['mps']
repos_spoofax = ['spoofax']

def get_repos_by_type(type: str) -> list[str]:
    if type == 'emf':
        return repos_emf
    elif type == 'spoofax':
        return repos_spoofax
    elif type == 'mps':
        return repos_mps
    else:
        raise ValueError('Unknown repo type: ' + type)

