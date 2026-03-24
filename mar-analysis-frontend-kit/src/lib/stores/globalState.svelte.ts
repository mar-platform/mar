import { searchProjectsApi } from "$lib/api/projects";
import type Project from "$lib/dto/Project";
import { toast } from "svelte-sonner";

class GlobalState {
    state: 'LOADING' | 'OK' | 'ERROR' = $state('LOADING');
    projects: Project[] = $state([]);
    searchProjects = $state<Project[]>([]);
    selectedProject: Project | null = $state(null);

    initialize(projects: Project[]) {
        this.projects = projects;
        this.state = 'OK';
        this.selectedProject = projects.length > 0 ? projects[0] : null;
        this.searchProjects = projects;
    }

    // —— Projects —————————————————————————————

    selectProject(project: Project) {
        this.selectedProject = project;
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
