import type GithubUser from "./GithubUser";

export default interface ArtefactInfo {
    project: string;
    filepath: string;
    extension: string;
    type: string;
    createdAt: Date;
    createdAuthor: string;
    createdAuthorData: GithubUser | null;
    updatedAt: Date;
    updatedAuthor: string;
}
