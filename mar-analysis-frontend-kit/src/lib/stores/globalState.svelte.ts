import { getProjectGraphApi, searchProjectsApi } from "$lib/api/projects";
import type Graph from "$lib/dto/Graph";
import type Project from "$lib/dto/Project";
import { toast } from "svelte-sonner";

class GlobalState {
    state: 'LOADING' | 'OK' | 'ERROR' = $state('LOADING');
    projects: Project[] = $state([]);
    searchProjects = $state<Project[]>([]);
    selectedProject: Project | null = $state(null);
    selectedProjectGraph: Graph | null = $state(null);

    initialize(projects: Project[]) {
        this.projects = projects;
        this.state = 'OK';
        this.selectedProject = projects.length > 0 ? projects[0] : null;
        this.searchProjects = projects;
        this.selectedProjectGraph = null; // Reset the graph when initializing with new projects
    }

    // —— Projects —————————————————————————————

    async selectProject(project: Project) {
        this.selectedProject = project;

        // Start loading the project graph
        const graph = await getProjectGraphApi(project.id);

        if (graph.status === 200 && graph.data) {
            this.selectedProjectGraph = graph.data;
        } else {
            toast.error('Failed to load project graph. Please try again later.');
            this.selectedProjectGraph = null;
        }
    }

    async searchProject(query: string) {
        if (!query) {
            this.searchProjects = this.projects;
            return;
        }

        const result = await searchProjectsApi(query);
        this.searchProjects = result.data || [];

        if (result.status !== 200) {
            toast.error('Failed to search projects. Please try again later.');
        }
    }


}

export const globalState = new GlobalState();
