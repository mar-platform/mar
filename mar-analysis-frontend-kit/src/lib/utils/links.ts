export function getArtefactGithubLink(projectId: string, artefactId: string): string {
    const cleanProject = projectId.replace(/^\/+|\/+$/g, '');
    let cleanFilePath = artefactId.replace(/^\/+|\/+$/g, '');
    cleanFilePath = cleanFilePath.substring(cleanProject.length).replace(/^\/+|\/+$/g, '');

    return `https://github.com/${cleanProject}/blob/HEAD/${cleanFilePath}`;
}       
