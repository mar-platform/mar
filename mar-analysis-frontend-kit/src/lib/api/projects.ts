import { ALL_PROJECTS_ENDPOINT, PROJECT_GRAPH_ENDPOINT, SEARCH_PROJECT_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type Graph from "$lib/dto/Graph";
import type Project from "$lib/dto/Project";

export const allProjectsApi = async (fetch?: typeof window.fetch): Promise<ApiResponse<Project[]>> => {
    const request = await (fetch || window.fetch)(ALL_PROJECTS_ENDPOINT, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });

    const status = request.status;
    let response: Project[] | null = null;

    if (request.ok) {
        response = await request.json() as Project[];
    }

    return {
        data: response,
        status,
    };
};

export const searchProjectsApi = async (query: string): Promise<ApiResponse<Project[]>> => {
    const request = await (fetch || window.fetch)(SEARCH_PROJECT_ENDPOINT(query), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        },
    });

    const status = request.status;
    let response: Project[] | null = null;

    if (request.ok) {
        response = await request.json() as Project[];
    }

    return {
        data: response,
        status,
    };
};

export const getProjectGraphApi = async (projectId: Project['id']): Promise<ApiResponse<Graph>> => {
    const request = await (fetch || window.fetch)(PROJECT_GRAPH_ENDPOINT(projectId), {
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
