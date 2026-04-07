import { ARTEFACT_INFO_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type ArtefactInfo from "$lib/dto/ArtefactInfo";
import type GithubUser from "$lib/dto/GithubUser";
import { getGithubUserInfoApi } from "./github";

export const getArtefactInfoApi = async (id: string): Promise<ApiResponse<ArtefactInfo>> => {
    const request = await (fetch || window.fetch)(ARTEFACT_INFO_ENDPOINT(id), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });

    const status = request.status;
    let response: ArtefactInfo | null = null;

    if (request.ok) {
        response = await request.json() as ArtefactInfo;

        // Convert date strings to Date objects
        response.createdAt = new Date(response.createdAt);
        response.updatedAt = new Date(response.updatedAt);

        // Try to fetch the author github data
        let githubAuthorData: GithubUser | null = null;
        try {
            const githubResponse = await getGithubUserInfoApi(response.project.split('/')[0]); // Extract the author from the projects as createdAuthor shows the name, not the userId
            githubAuthorData = githubResponse.data;
        } catch (error) {
            console.error('Error fetching GitHub user data:', error);
        }
        response.createdAuthorData = githubAuthorData;
    }

    return {
        data: response,
        status,
    };
};
