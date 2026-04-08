import { ALL_COMPONENTS_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";

export const allComponentsApi = async (fetch?: typeof window.fetch): Promise<ApiResponse<string[]>> => {
    const request = await (fetch || window.fetch)(ALL_COMPONENTS_ENDPOINT, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });

    const status = request.status;
    let response: string[] | null = null;

    if (request.ok) {
        response = await request.json() as string[];
    }

    return {
        data: response,
        status,
    };
};
