import { getProjectGraphApi, searchProjectsApi } from "$lib/api/projects";
import type { Node } from "$lib/dto/Graph";
import type Graph from "$lib/dto/Graph";
import type Project from "$lib/dto/Project";
import { toast } from "svelte-sonner";

class GlobalState {
    state: 'LOADING' | 'OK' | 'ERROR' = $state('LOADING');
    projects: Project[] = $state([]);
    searchProjects = $state<Project[]>([]);
    selectedProject: Project | null = $state(null);
    selectedProjectGraph: Graph | null = $state(null);
    selectedNode: Node | null = $state(null);

    initialize(projects: Project[]) {
        this.projects = projects;
        this.state = 'OK';
        this.searchProjects = projects;
        this.selectedProject = null;
        this.selectedProjectGraph = null; // Reset the graph when initializing with new projects
        this.selectedNode = /*{
            "_type": "artefact",
            "artefact": {
                "category": "transformation",
                "fileStatus": "EXISTS",
                "id": "101companies/101repo/contributions/atlTotalPlugin/bin/ATL_ComputeTotalPlugin/files/ComputeTotal.atl",
                "name": "ComputeTotal.atl",
                "project": "101companies/101repo",
                "type": "atl"
            },
            "attributes": [],
            "id": "101companies/101repo/contributions/atlTotalPlugin/bin/ATL_ComputeTotalPlugin/files/ComputeTotal.atl"
        }*/ null; 

        if (projects.length > 0) {
            this.selectProject(projects[0]);
        }
    }

    // —— Projects —————————————————————————————

    async selectProject(project: Project) {
        this.deselectNode(); // Deselect any selected node when changing projects

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

    // —— Nodes —————————————————————————————

    selectNode(node: Node | null) {
        this.selectedNode = node;
    }

    deselectNode() {
        this.selectedNode = null;
    }

}

export const globalState = new GlobalState();
