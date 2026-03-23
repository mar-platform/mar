import { statsApi } from "$lib/api/stats";
import { toast } from "svelte-sonner";
import type { PageLoad } from "./$types";

export const load: PageLoad = async ({ fetch }) => {
    const statsResponse = await statsApi(fetch);

    if (statsResponse.status !== 200 || !statsResponse.data) {
        toast.error('Failed to load stats data. Please try again later.');
    }

    return {
        stats: statsResponse.data
    };
};
