<script lang="ts">
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { fade } from 'svelte/transition';
	import Searchbar from '../basic/Searchbar.svelte';
	import GraphVisualizer, { type GraphVisualizerInitial } from './GraphVisualizer.svelte';
    import LucideLoaderCircle from '@lucide/svelte/icons/loader-circle';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
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
	import ProjectList from '../lists/ProjectList.svelte';
	import type Project from '$lib/dto/Project';
	import ArtefactList from '../lists/ArtefactList.svelte';

    // List states
    let listState = $derived<'PROJECT' | 'ARTEFACTS'>(globalState.mode === 'PROJECT' ? 'PROJECT' : 'ARTEFACTS');
    
    // Graph states
    let graphMode = $derived(globalState.mode);
    let graphVisualizerRef: GraphVisualizer | null = $state(null);
    const selectedGraph = $derived(globalState.selectedGraph);
    let fa2Running = $state(false);

    // Filter options
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
        "output-type": true,
        "copy-from": true,
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

    function onShowArtefactClick(project: Project) {
        if (globalState.mode !== 'PROJECT') {
            return;
        }
        // Select the project if it's not already selected
        if (globalState.selectedProject?.id !== project.id) {
            globalState.selectProject(project);
        }
        listState = 'ARTEFACTS';
    }

    function goBackToProjectList() {
        listState = 'PROJECT';
    }

</script>

{#if globalState.mode === 'PROJECT' && listState === 'PROJECT'}
    <ProjectList {onShowArtefactClick} />
{:else if listState === 'ARTEFACTS'}
    <ArtefactList filterNodes={selectedNodeTypes} onClickBackButton={globalState.mode === 'PROJECT' ? goBackToProjectList : undefined} />
{/if}
{#if graphMode !== null}
    <div class="w-full h-full overflow-hidden flex flex-col gap-2" in:fade out:fade>
        <div class="flex justify-between items-end gap-4">
            {#if globalState.selectedProject}
                {#key globalState.selectedProject.id}
                    <span title={globalState.selectedProject.id} class="mb-0.5 text-text-secondary text-base font-medium whitespace-nowrap overflow-hidden text-ellipsis" in:fade>{globalState.selectedProject.id}</span>   
                {/key}
            {/if}
            {#if selectedGraph !== null}
                <div class="ml-auto flex gap-4 items-center" in:fade>
                    <div class={`relative flex flex-col ${fa2Running ? 'bg-transparent' : 'bg-input-background'} rounded-md px-2 w-20 h-10 border border-input-border`}>
                        <label for="number-iterations" class="select-none text-text-placeholder absolute top-0.75 left-1 text-[0.65rem] px-1">Iterations</label>
                        <input id="number-iterations" bind:value={numberOfIterations} min={1} disabled={fa2Running} type="number" class="h-full pt-3 text-sm outline-none"/>
                    </div>
                    {#key fa2Running}
                        <div in:fade class="gap-2 flex items-center">
                            {#if fa2Running}
                            <Button class="bg-destructive hover:bg-destructive/80 text-white" onclick={() => graphVisualizerRef?.stopLayout()}>
                                <LucideSquare class="fill-white" />
                                <span class="animate-pulse">Applying layout</span>
                            </Button>
                            {:else}
                                <Button
                                    class="bg-blue-400 hover:bg-blue-400/80 text-white"
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
                                    <LucidePlay class="fill-white" />
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
            {:else}
                <div class="min-h-10"></div>
            {/if}
        </div>

        <div class="flex-1 bg-page-foreground rounded-lg shadow-sm">
            {#if selectedGraph !== null}
                {#key selectedGraph}
                    <GraphVisualizer
                        bind:this={graphVisualizerRef} 
                        bind:fa2Running={fa2Running}
                        {selectedNodeTypes}
                        {selectedEdgeTypes}
                        graph={selectedGraph}
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
