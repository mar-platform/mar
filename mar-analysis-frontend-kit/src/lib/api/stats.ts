import { STATS_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type Stats from "$lib/dto/Stats";

export const statsApi = async (fetch?: typeof window.fetch): Promise<ApiResponse<Stats>> => {
    const request = await (fetch || window.fetch)(STATS_ENDPOINT, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });

    const status = request.status;
    let response: Stats | null = null;

    if (request.ok) {
        response = await request.json() as Stats;
    }

    return {
        data: response,
        status,
    };
};
