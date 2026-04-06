<script lang="ts">
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { fade } from 'svelte/transition';
	import Searchbar from '../basic/Searchbar.svelte';
	import GraphVisualizer from './GraphVisualizer.svelte';
    import LucideLoaderCircle from '@lucide/svelte/icons/loader-circle';
	import GraphToolbar from './GraphToolbar.svelte';
    import * as Popover from "$lib/components/ui/popover/index.js";
    import { Button } from "$lib/components/ui/button/index.js";
    import LucideFilter from '@lucide/svelte/icons/filter';
    import LucideSquare from '@lucide/svelte/icons/square';
    import LucidePlay from '@lucide/svelte/icons/play';
    import { Separator } from "$lib/components/ui/separator/index.js";    
	import { toast } from 'svelte-sonner';
	import GraphInfoPanel from './GraphInfoPanel.svelte';
	import ProjectList from '../lists/ProjectList.svelte';
	import type Project from '$lib/dto/Project';
	import ArtefactList from '../lists/ArtefactList.svelte';
    import { ScrollArea } from "$lib/components/ui/scroll-area/index.js";

    // List states
    let listState = $derived<'PROJECT' | 'ARTEFACTS'>(globalState.mode === 'PROJECT' ? 'PROJECT' : 'ARTEFACTS');
    
    // Graph states
    let graphMode = $derived(globalState.mode);
    let graphVisualizerRef: GraphVisualizer | null = $state(null);
    const selectedGraph = $derived(globalState.selectedGraph);
    let fa2Running = $state(false);

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

    function onNumberOfIterationsChange(value: string) {
        if (!value || isNaN(Number(value)) || Number(value) < 1) {
            globalState.numberOfIterations = undefined;
        } else {
            globalState.numberOfIterations = Number(value);
        }
    }

    // NOTE: Some properties do not react to changes in the global state
    // So we notify the GraphVisualizer to update them when they change in the toolbar

    function onLabelSizeChange() {
        graphVisualizerRef?.updateLabelSize();
    }

    function onLabelThresholdChange() {
        graphVisualizerRef?.updateLabelThreshold();
    }
</script>

<div class="flex flex-1 gap-8 min-[1100px]:gap-4 min-[1100px]:flex-row flex-col">
    <div class="w-full h-[calc(100svh-120px-40px)] min-[1100px]:max-w-80">
        {#if globalState.mode === 'PROJECT' && listState === 'PROJECT'}
            <ProjectList {onShowArtefactClick} />
        {:else if globalState.state !== 'LOADING' && listState === 'ARTEFACTS'}
            <ArtefactList onClickBackButton={globalState.mode === 'PROJECT' ? goBackToProjectList : undefined} />
        {/if}
    </div>
    {#if graphMode !== null}
        <div id="graph-view" class="flex-1 min-w-0 min-h-[calc(100svh-120px-40px)] flex flex-col gap-2" in:fade out:fade>
            <div class="w-full flex flex-col">
                {#if globalState.selectedProject}
                    {#key globalState.selectedProject.id}
                        <span title={globalState.selectedProject.id} class="mb-3 text-text-secondary text-base font-medium whitespace-nowrap overflow-hidden text-ellipsis" in:fade>{globalState.selectedProject.id}</span>   
                    {/key}
                {/if}
                
                <ScrollArea orientation="horizontal" class="flex-1 min-w-0 pb-3">
                    <div class="flex flex-col min-[700px]:flex-row min-[700px]:gap-4 gap-2 min-[700px]:items-center" in:fade>
                        <div class="flex gap-2">
                            <div class={`relative flex flex-col ${fa2Running ? 'bg-transparent' : 'bg-input-background'} rounded-md px-2 w-20 h-10 border border-input-border`}>
                                <label for="number-iterations" class="select-none text-text-placeholder absolute top-0.75 left-1 text-[0.65rem] px-1">Iterations</label>
                                <input id="number-iterations" value={globalState.numberOfIterations} oninput={(e) => onNumberOfIterationsChange((e.target as HTMLInputElement).value)} min={1} disabled={fa2Running || selectedGraph === null} type="number" class="h-full pt-3 text-sm outline-none"/>
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
                                            disabled={!globalState.numberOfIterations || selectedGraph === null}
                                            onclick={() => {
                                                if (!globalState.numberOfIterations) {
                                                    toast.error('Please enter a valid number of iterations');
                                                    return;
                                                }
                                                graphVisualizerRef?.startLayout(globalState.numberOfIterations);
                                            }}
                                        >
                                            Start layout
                                            <LucidePlay class="fill-white" />
                                        </Button>
                                    {/if}
                                </div>
                            {/key}
                        </div>

                        <Separator orientation="vertical" class="min-[700px]:block hidden min-h-5 bg-gray-400" />
                        
                        <div class="flex gap-4">
                            <Popover.Root>
                                <Popover.Trigger disabled={selectedGraph === null}>
                                    <Button disabled={selectedGraph === null} variant="outline">
                                        <LucideFilter />
                                        Filters
                                    </Button>
                                </Popover.Trigger>
                                <Popover.Content side="top" strategy="absolute" preventScroll={false} class="lg:w-200 mx-4 w-[calc(100vw-2rem)]">
                                    <GraphToolbar {onLabelSizeChange} {onLabelThresholdChange} />
                                </Popover.Content>
                            </Popover.Root>
                            <Searchbar disabled={selectedGraph === null} placeholder="Filter nodes..." onSearch={(query) => { globalState.nodeFilter = query }} />
                        </div>
                    </div>
                </ScrollArea>
            </div>
    
            <div class="flex-1 bg-page-foreground rounded-lg shadow-sm">
                {#if selectedGraph !== null}
                    {#key selectedGraph}
                        <GraphVisualizer
                            bind:this={graphVisualizerRef} 
                            bind:fa2Running={fa2Running}
                            graph={selectedGraph}
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
</div>
