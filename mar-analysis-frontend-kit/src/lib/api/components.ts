import { ALL_COMPONENTS_ENDPOINT, COMPONENT_GRAPH_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type Graph from "$lib/dto/Graph";

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

export const getComponentGraphApi = async (componentId: string): Promise<ApiResponse<Graph>> => {
    const request = await (fetch || window.fetch)(COMPONENT_GRAPH_ENDPOINT(componentId), {
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
