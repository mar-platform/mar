<script lang="ts">
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { fade } from 'svelte/transition';
	import Searchbar from '../basic/Searchbar.svelte';
	import GraphVisualizer from './GraphVisualizer.svelte';
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
	import { DEFAULT_NUMBER_OF_ITERATIONS } from '$lib/constants/values';


    let graphVisualizerRef: GraphVisualizer | null = $state(null);
    const selectedProjectGraph = $derived(createGraphFromDTO(globalState.selectedProjectGraph));
    let fa2Running = $state(false);
    let numberOfIterations = $state(DEFAULT_NUMBER_OF_ITERATIONS);

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
                    type = /* FIXME node.kind === 'duplication' ? node.artefactType :*/ node.kind;
                    console.log('Type for virtual: ', type);
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

    function handleNodesChange(nodeSize: number, showConnectedNodes: boolean) {
        graphVisualizerRef?.setNodeConfig(nodeSize, showConnectedNodes, selectedNodeTypes);
    }

</script>

{#if globalState.selectedProject}
    <div class="w-full h-full flex flex-col gap-2" in:fade out:fade>
        <div class="flex justify-between items-end">
            {#key globalState.selectedProject.id}
                <span class="mb-0.5 text-text-secondary text-base font-medium" in:fade>{globalState.selectedProject.id}</span>   
            {/key}
            <div class="flex gap-4 items-center">
                {#key fa2Running}
                    <div in:fade class="gap-2 flex items-center">
                        {#if fa2Running}
                        <Button class="bg-destructive hover:bg-destructive/80" onclick={() => graphVisualizerRef?.stopLayout()}>
                            <LucideSquare />
                            <span class="animate-pulse">Applying layout...</span>
                        </Button>
                        {:else}
                            <Button class="bg-blue-400 hover:bg-blue-400/80" onclick={() => graphVisualizerRef?.startLayout(numberOfIterations)}>
                                Start layout
                                <LucidePlay />
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
                            onLabelSizeChange={handleLabelSizeChange}
                            onLabelThresholdChange={handleLabelThresholdChange}
                            handleNodesChange={handleNodesChange}
                        />
                    </Popover.Content>
                </Popover.Root>
                <Searchbar placeholder="Filter nodes..." onSearch={(query) => console.log("Search query:", query)} />
            </div>
        </div>

        <div class="flex-1 bg-page-foreground rounded-lg shadow-sm">
            {#if selectedProjectGraph !== null}
                {#key selectedProjectGraph}
                    <GraphVisualizer bind:this={graphVisualizerRef} bind:fa2Running={fa2Running} {selectedNodeTypes} {selectedEdgeTypes} graph={selectedProjectGraph} />
                {/key}
            {:else}
                <div class="relative p-4 w-full h-full">
                    <LucideLoaderCircle class="animate-spin absolute top-1/2 left-1/2 -mt-10 -ml-10 w-10 h-10 z-10 text-text-placeholder" />
                    <Skeleton class="p-4 w-full h-full" />
                </div>
            {/if}
        </div>
    </div>
{/if}
