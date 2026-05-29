import {  DUPLICATION_GRAPH_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type Graph from "$lib/dto/Graph";

export const getDuplicationGraphApi = async (): Promise<ApiResponse<Graph>> => {
    const request = await (fetch || window.fetch)(DUPLICATION_GRAPH_ENDPOINT, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });

    const status = request.status;
    let response: Graph | null = null;

    if (request.ok) {
        response = await request.json() as Graph;
    }

    return {
        data: response,
        status,
    };
};
