import { goto } from "$app/navigation";
import { page } from "$app/state";
import { getAllGraphsApi } from "$lib/api/allGraphs";
import { getArtefactInfoApi } from "$lib/api/artefacts";
import { getComponentGraphApi } from "$lib/api/components";
import { getDuplicationGraphApi } from "$lib/api/duplication";
import { getInterProjectGraphApi } from "$lib/api/interproject";
import { getMegamodelGraphApi } from "$lib/api/megamodel";
import { getProjectGraphApi, getProjectInfoApi } from "$lib/api/projects";
import type { edgeTypes } from "$lib/constants/edgeTypes";
import type { nodeTypes } from "$lib/constants/graphNodeTypes";
import { DEFAULT_NUMBER_OF_ITERATIONS, INITIAL_LABEL_SIZE, INITIAL_LABEL_THRESHOLD, INITIAL_NODE_SIZE, INITIAL_SHOW_UNCONNECTED_NODES, MIN_WAIT_TIME_MS } from "$lib/constants/values";
import type ApiResponse from "$lib/dto/ApiResponse";
import type ArtefactInfo from "$lib/dto/ArtefactInfo";
import type { ArtefactNode, Edge, Node } from "$lib/dto/Graph";
import type GraphDTO from "$lib/dto/Graph";
import type ProjectInfo from "$lib/dto/ProjectInfo";
import { paginateArray, type PaginationResult } from "$lib/utils/pagination";
import { scrollToDetails, scrollToGraph } from "$lib/utils/scroll";
import type Graph from "graphology";
import { UndirectedGraph } from "graphology";
import { random } from "graphology-layout";
import type { Sigma } from "sigma";
import { tick } from "svelte";
import { toast } from "svelte-sonner";
import { SvelteURLSearchParams } from "svelte/reactivity";

export type GraphMode = 'ALL' | 'PROJECT' | 'INTER_PROJECT' | 'MEGAMODEL' | 'DUPLICATION' | 'COMPONENT';

class GlobalState {
    state: 'LOADING' | 'LOADING_GRAPH' | 'OK' | 'ERROR' = $state('LOADING');
    mode: GraphMode | null = $state(null);
    
    projects: string[] = $state([]);
    selectedProject: string | null = $state(null);
    
    components: string[] = $state([]);
    selectedComponent: string | null = $state(null);
    
    selectedUnprocessedGraph: GraphDTO | null = $state(null);
    selectedGraph: Graph | null = $state(null);
    selectedNode: Node | null = $state(null);
    selectedEdge: Edge | null = $state(null);

    // Node filter properties
    nodeFilter: string = $state('');
    nodeSize: number = $state(INITIAL_NODE_SIZE);
    showUnconnectedNodes: boolean = $state(INITIAL_SHOW_UNCONNECTED_NODES);
    labelSize: number = $state(INITIAL_LABEL_SIZE);
    labelThreshold: number = $state(INITIAL_LABEL_THRESHOLD);
    numberOfIterations: number | undefined = $state(DEFAULT_NUMBER_OF_ITERATIONS);
    selectedNodeTypes = $state<Record<keyof typeof nodeTypes, boolean>>({
        acceleo: true,
        atl: true,
        duplication: true,
        ecore: true,
        emfatic: true,
        emftext: true,
        epsilon: true,
        henshin: true,
        ocl: true,
        gmf: true,
        project: true,
        qvto: true,
        sirius: true,
        xtext: true,
        error: true
    });
    selectedEdgeTypes = $state<Record<keyof typeof edgeTypes, boolean>>({
        "typed-by": true,
        "import": true,
        "duplicate": true,
        "build_duplicate": true,
        "project-to-project": true,
        "input-type": true,
        "output-type": true,
        "generate": true,
    });
    private currentGraphRenderer: Sigma | null = null;

    initialize(projects: string[], components: string[]) {
        this.projects = projects;
        this.components = components;
        this.state = 'OK';
        this.selectedProject = null;
        this.selectedComponent = null;
        this.selectedGraph = null; // Reset the graph when initializing with new projects
        this.selectedUnprocessedGraph = null;
        this.selectedNode = null;
        this.selectedEdge = null;
        this.nodeFilter = '';
        this.mode = null; // Reset the mode

        // Clean up the existing graph renderer if it exists
        if (this.currentGraphRenderer) {
            this.currentGraphRenderer.kill();
        }
        this.currentGraphRenderer = null;
    }

    // —— Projects —————————————————————————————

    async fetchProjects(query: string, page: number, pageSize: number): Promise<PaginationResult<string>> {
        const projects = this.projects.filter(project => project.toLowerCase().includes(query.toLowerCase()));
        const minWaitTime = new Promise((resolve) => setTimeout(resolve, MIN_WAIT_TIME_MS));
        const result = paginateArray(projects, page, pageSize);
        await minWaitTime;

        return result;
    }

    async selectProject(project: string, autoScroll = true) {
        this.deselectNodeOrEdge(); // Deselect any selected node or edge when selecting a new project

        this.selectedProject = project;

        // Start loading the project graph
        const graph = await getProjectGraphApi(project);
        
        if (graph.status === 200 && graph.data) {
            await this.selectGraph(graph.data);

            // Update the URL with the selected project as a query parameter
            const params = new SvelteURLSearchParams(page.url.searchParams);
            params.set('q', project);
            // eslint-disable-next-line svelte/no-navigation-without-resolve
            goto(`?${params.toString()}`, {
                keepFocus: true,
                replaceState: true,
                noScroll: true
            });
        } else {
            toast.error('Failed to load project graph. Please try again later.');
            this.selectedGraph = null;
            this.selectedUnprocessedGraph = null;
        }
        if (autoScroll) {
            scrollToGraph();
        }
    }

    async getProjectInfo(projectId: string): Promise<ProjectInfo | null> {
        return (await getProjectInfoApi(projectId)).data;
    }

    // —— Graph —————————————————————————————

    async setGraphMode(mode: GraphMode) {
        this.mode = mode;
        
        // Reset selected project, nodes...
        this.selectedProject = null;
        this.selectedComponent = null;
        this.selectedGraph = null;
        this.selectedNode = null;
        this.selectedUnprocessedGraph = null;
        this.state = 'LOADING_GRAPH';
        
        await tick(); // Ensure that any reactive updates related to mode change are processed before proceeding
        
        // Start calculating the graph depending on the mode
        let apiResponse: ApiResponse<GraphDTO> | null = null;
        switch(mode) {
            case 'ALL':
                apiResponse = await getAllGraphsApi();
                break;
            case 'PROJECT':
                return; // Exit early since selectProject will handle graph loading
            case 'INTER_PROJECT':
                apiResponse = await getInterProjectGraphApi();
                break;
            case 'MEGAMODEL':
                apiResponse = await getMegamodelGraphApi();
                break;
            case 'DUPLICATION':
                apiResponse = await getDuplicationGraphApi();
                break;
            case 'COMPONENT':
                return; // Exit early since selectComponent will handle graph loading
            default:
                toast.error('Invalid graph mode selected.');
        }

        // Some APIs take a long time, check if the user is still on the same mode before loading the graph
        if (this.mode !== mode) {
            return;
        }
        
        if (apiResponse && apiResponse.status === 200 && apiResponse.data) {
            await this.selectGraph(apiResponse.data);
        } else {
            toast.error('Failed to load graph data. Please try again later.');
            this.selectedGraph = null;
            this.selectedUnprocessedGraph = null;
        }
        this.state = 'OK';
    }

    private async selectGraph(dto: GraphDTO) {
        if (dto === null) {
            return;
        }

        this.state = 'LOADING_GRAPH';
        await tick(); // Ensure the loading state is rendered before processing the graph

		const graph = new UndirectedGraph();

		dto.nodes.forEach((node: Node) => {
			let type: keyof typeof nodeTypes, name: string;

            switch(node._type) {
                case 'artefact':
                    type = node.type;
                    name = node.name;
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
                //console.log(`Edge between ${edge.source} and ${edge.target} already exists. Skipping duplicate edge.`);
            } else {
                graph.addEdge(edge.source, edge.target, { edgeTypes: edge.types, size: 2 });
            }
		});

		random.assign(graph);

        this.selectedGraph = graph;
        this.selectedUnprocessedGraph = dto;
        this.state = 'OK';
    }

    // —— Graph renderer —————————————————————————————

    get renderer() {
        return this.currentGraphRenderer;
    }

    set renderer(renderer: Sigma | null) {
        this.currentGraphRenderer = renderer;
    }

    refreshGraph() {
        if (!this.currentGraphRenderer) {
            return;
        }
        this.currentGraphRenderer = this.currentGraphRenderer.refresh();
    }

    // —— Nodes —————————————————————————————

    async selectNode(node: Node | null) {
        this.selectedEdge = null; // Deselect any selected edge when selecting a node
        this.selectedNode = node;
        await tick();
        // Important to refresh the graph after selecting a node
        this.refreshGraph();
        scrollToDetails();
    }

    // —— Edges —————————————————————————————

    async selectEdge(edge: Edge | null) {
        this.selectedNode = null; // Deselect any selected node when selecting an edge
        this.selectedEdge = edge;
        await tick();
        // Important to refresh the graph after selecting an edge
        this.refreshGraph();
        scrollToDetails();
    }

    async deselectNodeOrEdge() {
        this.selectedNode = null;
        this.selectedEdge = null;

        await tick();
        // Important to refresh the graph after deselecting a node
        this.refreshGraph();
    }

    // —— Artefacts —————————————————————————————

    getArtefactsFromNodes(nodes: Node[], filterNodes: Record<keyof typeof nodeTypes, boolean>, query: string): ArtefactNode[] {
        const q = query.trim().toLowerCase();
        let filteredNodes = nodes
            .filter(node => node._type === 'artefact')
            .filter(node => filterNodes[node.type]);

        if (q != '') {
            filteredNodes = filteredNodes.filter(node => node.name.toLowerCase().includes(q));
        }
        return filteredNodes;
    }

    async getArtefactInfo(artefactId: string): Promise<ArtefactInfo | null> {
        return (await getArtefactInfoApi(artefactId)).data;
    }

    // —— Components —————————————————————————————
    
    async fetchComponents(query: string, page: number, pageSize: number): Promise<PaginationResult<string>> {
        const components = this.components.filter(component => component.toLowerCase().includes(query.toLowerCase()));
        const minWaitTime = new Promise((resolve) => setTimeout(resolve, MIN_WAIT_TIME_MS));
        const result = paginateArray(components, page, pageSize);
        await minWaitTime;

        return result;
    }

    async selectComponent(component: string, autoScroll = true) {
        this.deselectNodeOrEdge(); // Deselect any selected node or edge when selecting a new component

        this.selectedComponent = component;

        // Start loading the component graph
        const graph = await getComponentGraphApi(component);
        
        if (graph.status === 200 && graph.data) {
            await this.selectGraph(graph.data);

            // Update the URL with the selected component as a query parameter
            const params = new SvelteURLSearchParams(page.url.searchParams);
            params.set('q', component);
            // eslint-disable-next-line svelte/no-navigation-without-resolve
            goto(`?${params.toString()}`, {
                keepFocus: true,
                replaceState: true,
                noScroll: true
            });
        } else {
            toast.error('Failed to load component graph. Please try again later.');
            this.selectedGraph = null;
            this.selectedUnprocessedGraph = null;
        }
        if (autoScroll) {
            scrollToGraph();
        }
    }
}

export const globalState = new GlobalState();
