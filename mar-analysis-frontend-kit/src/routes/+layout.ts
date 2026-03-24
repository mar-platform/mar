import { allProjectsApi } from "$lib/api/projects";
import type Project from "$lib/dto/Project";
import { globalState } from "$lib/stores/globalState.svelte";
import type { LayoutLoad } from "./$types";

export const ssr = false;

export const load: LayoutLoad = ({ fetch }) => {
    const projectData = allProjectsApi(fetch);
    
    const promise = Promise.all([projectData]).then(([projects]) => {
        let projectData: Project[] = [];
        if (projects.status === 200 && projects.data) {
            projectData = projects.data;
        }

        globalState.initialize(projectData);
    });

    return {
        promise
    };
};
