import { getInterProjectGraphApi } from "$lib/api/interproject";
import { getProjectGraphApi, searchProjectsApi } from "$lib/api/projects";
import type { nodeTypes } from "$lib/constants/graphNodeTypes";
import type ApiResponse from "$lib/dto/ApiResponse";
import type { Edge, Node } from "$lib/dto/Graph";
import type GraphDTO from "$lib/dto/Graph";
import type Project from "$lib/dto/Project";
import type Graph from "graphology";
import { UndirectedGraph } from "graphology";
import { random } from "graphology-layout";
import type { Sigma } from "sigma";
import { tick } from "svelte";
import { toast } from "svelte-sonner";

type GraphMode = 'PROJECT' | 'INTER_PROJECT';

class GlobalState {
    state: 'LOADING' | 'OK' | 'ERROR' = $state('LOADING');
    mode: GraphMode | null = $state(null);
    projects: Project[] = $state([]);
    searchProjects = $state<Project[]>([]);
    selectedProject: Project | null = $state(null);
    selectedGraph: Graph | null = $state(null);
    selectedNode: Node | null = $state(null);
    private currentGraphRenderer: Sigma | null = null;

    initialize(projects: Project[]) {
        this.projects = projects;
        this.state = 'OK';
        this.searchProjects = projects;
        this.selectedProject = null;
        this.selectedGraph = null; // Reset the graph when initializing with new projects
        this.selectedNode = null; 
        this.mode = null; // Reset the mode

        // Clean up the existing graph renderer if it exists
        if (this.currentGraphRenderer) {
            this.currentGraphRenderer.kill();
        }
        this.currentGraphRenderer = null;
    }

    // —— Projects —————————————————————————————

    async selectProject(project: Project) {
        this.deselectNode(); // Deselect any selected node when changing projects

        this.selectedProject = project;

        // Start loading the project graph
        const graph = await getProjectGraphApi(project.id);

        if (graph.status === 200 && graph.data) {
            this.selectGraph(graph.data);
        } else {
            toast.error('Failed to load project graph. Please try again later.');
            this.selectedGraph = null;
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

    // —— Graph —————————————————————————————

    async setGraphMode(mode: GraphMode) {
        this.mode = mode;
        
        // Reset selected project, nodes...
        this.selectedProject = null;
        this.selectedGraph = null;
        this.selectedNode = null;
        
        await tick(); // Ensure that any reactive updates related to mode change are processed before proceeding
        
        // Start calculating the graph depending on the mode
        let apiResponse: ApiResponse<GraphDTO> | null = null;
        switch(mode) {
            case 'PROJECT':
                if (this.projects.length > 0) {
                    await this.selectProject(this.projects[0]);
                }
                return; // Exit early since selectProject will handle graph loading
            case 'INTER_PROJECT':
                apiResponse = await getInterProjectGraphApi();
                break;
            default:
                toast.error('Invalid graph mode selected.');
        }

        if (apiResponse && apiResponse.status === 200 && apiResponse.data) {
            this.selectGraph(apiResponse.data);
        } else {
            toast.error('Failed to load graph data. Please try again later.');
            this.selectedGraph = null;
        }
    }

    selectGraph(dto: GraphDTO) {
        if (dto === null) {
            return null;
        }

		const graph = new UndirectedGraph();

		dto.nodes.forEach((node: Node) => {
			let type: keyof typeof nodeTypes, name: string;

            switch(node._type) {
                case 'artefact':
                    type = node.artefact.type;
                    name = node.artefact.name;
                    break;
                case 'virtual':
                    type = node.kind === 'duplication' ? node.artefactType! : node.kind;
                    name = node.id;
                    break;
                default:
                    type = 'error';
                    name = 'unknown';
            }

            graph.addNode(node.id, {
				x: 0,
				y: 0,
				impl: node,
				nodeType: type,
				label: name,
			});
		});

		dto.edges.forEach((edge: Edge) => {
            // FIXME Why duplicate edges???
			if (graph.hasEdge(edge.source, edge.target)) {
                console.log(`Edge between ${edge.source} and ${edge.target} already exists. Skipping duplicate edge.`);
            } else {
                graph.addEdge(edge.source, edge.target, { edgeTypes: edge.types, size: 2 });
            }
		});

		random.assign(graph);

        this.selectedGraph = graph;
    }

    get renderer() {
        return this.currentGraphRenderer;
    }

    set renderer(renderer: Sigma | null) {
        this.currentGraphRenderer = renderer;
    }

    // —— Nodes —————————————————————————————

    selectNode(node: Node | null) {
        this.selectedNode = node;
        this.renderer?.refresh();
    }

    async deselectNode() {
        this.selectedNode = null;
        await tick();
        // Important to refresh the graph after deselecting a node to ensure that any visual changes (like unhighlighting) are applied correctly
        this.renderer?.refresh();
    }

}

export const globalState = new GlobalState();
