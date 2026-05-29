import { allComponentsApi } from "$lib/api/components";
import { allProjectsApi } from "$lib/api/projects";
import { globalState } from "$lib/stores/globalState.svelte";
import type { LayoutLoad } from "./$types";

export const ssr = false;
export const prerender = true

export const load: LayoutLoad = ({ fetch }) => {
    const projectData = allProjectsApi(fetch);
    const componentsData = allComponentsApi(fetch);

    const promise = Promise.all([projectData, componentsData]).then(([projects, components]) => {
        let projectData: string[] = [];
        let componentsData: string[] = [];
        if (projects.status === 200 && projects.data) {
            projectData = projects.data;
        }
        if (components.status === 200 && components.data) {
            componentsData = components.data;
        }

        globalState.initialize(projectData, componentsData);
    });

    return {
        promise
    };
};
