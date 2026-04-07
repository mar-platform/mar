import { ARTEFACT_INFO_ENDPOINT } from "$lib/constants/api-routes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type ArtefactInfo from "$lib/dto/ArtefactInfo";

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
    }

    return {
        data: response,
        status,
    };
};
