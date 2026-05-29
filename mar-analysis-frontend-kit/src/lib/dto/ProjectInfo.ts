import type GithubUser from "./GithubUser";

export default interface ProjectInfo {
    id: string;
    author: string;
    authorData: GithubUser | null;
    description: string;
    createdAt: Date;
    updatedAt: Date;
}
