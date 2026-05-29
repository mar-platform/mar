import { GITHUB_USER_API_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type GithubUser from "$lib/dto/GithubUser";

export const getGithubUserInfoApi = async (userId: string): Promise<ApiResponse<GithubUser>> => {
    const request = await fetch(GITHUB_USER_API_ENDPOINT(userId), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });

    const status = request.status;
    let response: GithubUser | null = null;

    if (request.ok) {
        const data = await request.json();
        response = {
            id: data.login,
            name: data.name,
            photoUrl: data.avatar_url,
            bio: data.bio,
            createdAt: new Date(data.created_at),
            updatedAt: new Date(data.updated_at),
            url: data.html_url,
        }
    }

    return {
        data: response,
        status,
    };
};
