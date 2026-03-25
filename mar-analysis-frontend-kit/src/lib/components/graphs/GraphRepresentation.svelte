<script lang="ts">
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { fade } from 'svelte/transition';
	import Searchbar from '../basic/Searchbar.svelte';
	import GraphVisualizer, { type GraphVisualizerInitial } from './GraphVisualizer.svelte';
    import LucideLoaderCircle from '@lucide/svelte/icons/loader-circle';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
	import { random } from 'graphology-layout';
	import { UndirectedGraph } from 'graphology';
	import type Graph from '$lib/dto/Graph';
	import type { Edge, Node } from '$lib/dto/Graph';
	import GraphToolbar from './GraphToolbar.svelte';
	import type { edgeTypes } from '$lib/constants/edgeTypes';
    import * as Popover from "$lib/components/ui/popover/index.js";
    import { Button } from "$lib/components/ui/button/index.js";
    import LucideFilter from '@lucide/svelte/icons/filter';
    import LucideSquare from '@lucide/svelte/icons/square';
    import LucidePlay from '@lucide/svelte/icons/play';
    import { Separator } from "$lib/components/ui/separator/index.js";    
	import { DEFAULT_NUMBER_OF_ITERATIONS, INITIAL_LABEL_SIZE, INITIAL_LABEL_THRESHOLD, INITIAL_NODE_SIZE, INITIAL_SHOW_UNCONNECTED_NODES } from '$lib/constants/values';
	import { toast } from 'svelte-sonner';
	import GraphInfoPanel from './GraphInfoPanel.svelte';


    let graphVisualizerRef: GraphVisualizer | null = $state(null);
    const selectedProjectGraph = $derived(createGraphFromDTO(globalState.selectedProjectGraph));
    let fa2Running = $state(false);
    let numberOfIterations = $state(DEFAULT_NUMBER_OF_ITERATIONS);
    let nodeFilter = $state('');
    let labelThreshold = $state(INITIAL_LABEL_THRESHOLD);
    let labelSize = $state(INITIAL_LABEL_SIZE);
    let showUnconnectedNodes = $state(INITIAL_SHOW_UNCONNECTED_NODES);
    let nodeSize = $state(INITIAL_NODE_SIZE);

    let initialProps = $derived<GraphVisualizerInitial>({
        nodeFilter,
        nodeSize,
        showUnconnectedNodes,
        labelSize,
        labelThreshold,
        numberOfIterations
    });

    function createGraphFromDTO(dto: Graph | null) {
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
                    type = node.kind;
                    console.log('Type for virtual: ', type);
                    name = node.id;
                    break;
                case 'duplication':
                    type = node.artefactType; // When a node is duplicated we set its type as the original (instead of 'duplication')
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
			graph.addEdge(edge.source, edge.target, { edgeTypes: edge.types, size: 2 });
		});

		random.assign(graph);

        return graph;
	}

    let selectedNodeTypes = $state<Record<keyof typeof nodeTypes, boolean>>({
        acceleo: true,
        atl: true,
        duplication: true,
        ecore: true,
        emfatic: true,
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

    let selectedEdgeTypes = $state<Record<keyof typeof edgeTypes, boolean>>({
        "typed-by": true,
        "import": true,
        "duplicate": true,
        "build_duplicate": true,
        "project-to-project": true,
        "input-type": true,
        "output-type": true
    });

    function handleLabelSizeChange(size: number) {
        graphVisualizerRef?.setLabelSize(size);
    }

    function handleLabelThresholdChange(threshold: number) {
        graphVisualizerRef?.setLabelThreshold(threshold);
    }

    function handleNodesChange(nodeFilter: string, nodeSize: number, showConnectedNodes: boolean) {
        graphVisualizerRef?.setNodeConfig(nodeFilter, nodeSize, showConnectedNodes, selectedNodeTypes);
    }

</script>

{#if globalState.selectedProject}
    <div class="w-full h-full overflow-hidden flex flex-col gap-2" in:fade out:fade>
        <div class="flex justify-between items-end gap-4">
            {#key globalState.selectedProject.id}
                <span title={globalState.selectedProject.id} class="mb-0.5 text-text-secondary text-base font-medium whitespace-nowrap overflow-hidden text-ellipsis" in:fade>{globalState.selectedProject.id}</span>   
            {/key}
            <div class="flex gap-4 items-center">
                <div class={`relative flex flex-col ${fa2Running ? 'bg-gray-200' : 'bg-input-background'} rounded-md px-2 w-20 h-10 border border-input-border`}>
                    <label for="number-iterations" class="select-none text-text-placeholder absolute top-0.75 left-1 text-[0.65rem] px-1">Iterations</label>
                    <input id="number-iterations" bind:value={numberOfIterations} min={1} disabled={fa2Running} type="number" class="h-full pt-3 text-sm outline-none"/>
                </div>
                {#key fa2Running}
                    <div in:fade class="gap-2 flex items-center">
                        {#if fa2Running}
                        <Button class="bg-destructive hover:bg-destructive/80" onclick={() => graphVisualizerRef?.stopLayout()}>
                            <LucideSquare class="fill-text-tertiary" />
                            <span class="animate-pulse">Applying layout</span>
                        </Button>
                        {:else}
                            <Button
                                class="bg-blue-400 hover:bg-blue-400/80"
                                disabled={!numberOfIterations}
                                onclick={() => {
                                    if (!numberOfIterations) {
                                        toast.error('Please enter a valid number of iterations');
                                        return;
                                    }
                                    graphVisualizerRef?.startLayout(numberOfIterations);
                                }}
                            >
                                Start layout
                                <LucidePlay class="fill-text-tertiary" />
                            </Button>
                        {/if}
                    </div>
                {/key}
                <Separator orientation="vertical" class="min-h-5 bg-gray-400" />
                <Popover.Root>
                    <Popover.Trigger>
                        <Button variant="outline">
                            <LucideFilter />
                            Filters
                        </Button>
                    </Popover.Trigger>
                    <Popover.Content class="xl:w-200">
                        <GraphToolbar 
                            bind:selectedNodeTypes={selectedNodeTypes}
                            bind:selectedEdgeTypes={selectedEdgeTypes}
                            bind:nodeSize={nodeSize}
                            bind:showUnconnectedNodes={showUnconnectedNodes}
                            bind:labelSize={labelSize}
                            bind:labelThreshold={labelThreshold}
                            onLabelSizeChange={handleLabelSizeChange}
                            onLabelThresholdChange={handleLabelThresholdChange}
                            onNodeSizeChange={(nodeSize) => handleNodesChange(nodeFilter, nodeSize, showUnconnectedNodes)}
                            onShowUnconnectedNodesChange={(show) => handleNodesChange(nodeFilter, nodeSize, show)}
                        />
                    </Popover.Content>
                </Popover.Root>
                <Searchbar placeholder="Filter nodes..." onSearch={(query) => { nodeFilter = query; handleNodesChange(nodeFilter, nodeSize, showUnconnectedNodes) }} />
            </div>
        </div>

        <div class="flex-1 bg-page-foreground rounded-lg shadow-sm">
            {#if selectedProjectGraph !== null}
                {#key selectedProjectGraph}
                    <GraphVisualizer
                        bind:this={graphVisualizerRef} 
                        bind:fa2Running={fa2Running}
                        {selectedNodeTypes}
                        {selectedEdgeTypes}
                        graph={selectedProjectGraph}
                        {initialProps}
                     />
                {/key}
            {:else}
                <div class="relative p-4 w-full h-full">
                    <LucideLoaderCircle class="animate-spin absolute top-1/2 left-1/2 -mt-10 -ml-10 w-10 h-10 z-10 text-text-placeholder" />
                    <Skeleton class="p-4 w-full h-full" />
                </div>
            {/if}
        </div>
    </div>
    <GraphInfoPanel />
{/if}
