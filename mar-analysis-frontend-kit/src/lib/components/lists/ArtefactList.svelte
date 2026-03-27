<script lang="ts">
	import LucideArrowLeft from '@lucide/svelte/icons/arrow-left';
	import { Button } from '$lib/components/ui/button/index.js';
	import { ScrollArea } from '$lib/components/ui/scroll-area/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
	import { cn } from '$lib/utils';
	import Searchbar from '../basic/Searchbar.svelte';
    import LucideNewspaper from '@lucide/svelte/icons/newspaper';
    import * as Empty from "$lib/components/ui/empty/index.js";
	import type { ArtefactNode } from '$lib/dto/Graph';
	import { fade } from 'svelte/transition';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";

    interface ArtefactListProps {
        onClickBackButton?: () => void;
    }

    let { onClickBackButton } : ArtefactListProps = $props();

    let query = $state('');

    const nodes = $derived.by(() => {
        if (globalState.mode === 'PROJECT' && !globalState.selectedProject || globalState.state === 'LOADING_GRAPH' && !globalState.selectedUnprocessedGraph) {
            return [];
        }
        return globalState.getArtefactsFromNodes(globalState.selectedUnprocessedGraph?.nodes || [], globalState.selectedNodeTypes, query);
    });

    function onClickArtefact(node: ArtefactNode) {
        if (globalState.selectedNode?.id === node.id) return;
        globalState.selectNode(node);
    }

    function onSearch(newQuery: string) {
        query = newQuery;
    }

    function handleBackButton() {
        if (onClickBackButton) {
            onClickBackButton();
            query = '';
        }
    }
</script>

<div class="min-w-80 max-w-80 rounded-lg h-full bg-page-foreground p-4 shadow-sm flex flex-col" in:fade>
	<div class="flex gap-3 items-center">
        {#if onClickBackButton}
            <Button title="Back to projects" variant="ghost" size="icon" class="rounded-full" onclick={handleBackButton}>
                <LucideArrowLeft />
            </Button>
        {/if}
        <LucideNewspaper />
        <h2 class="text-lg font-semibold">Artefacts</h2>
        {#if globalState.state === 'LOADING_GRAPH'}
            <Skeleton class="min-w-6 min-h-6 rounded-full" />
        {:else} 
            <div class="py-0.5 min-w-6 px-2 flex items-center justify-center bg-accent rounded-full text-sm">
                {nodes.length}
            </div>
        {/if}
    </div>

    <Searchbar class="bg-page-background mt-3 mb-5" placeholder="Filter artefacts..." {onSearch} />

	<ScrollArea class="flex-1 h-10 pr-3">
        {#if globalState.state === 'LOADING_GRAPH'}
            {#each Array.from({ length: 8 }, (_, i) => i) as _(_)}
                <Skeleton class="h-6 mt-2 w-full" />
            {/each}
        {:else} 
            {#each nodes as node(node.id)}
                <Button size="sm" variant={globalState.selectedNode?.id === node.id ? "secondary" : "ghost"} class="gap-2 rounded-full justify-start w-full" onclick={() => onClickArtefact(node)}>
                    <div class="min-w-2 min-h-2 rounded-full" style={`background-color: ${`var(${nodeTypes[node.artefact.type as keyof typeof nodeTypes].color})`};`}></div>
                    <span title={node.artefact.name} class={cn("whitespace-nowrap overflow-hidden text-ellipsis font-normal", globalState.selectedNode?.id === node.id ? "font-semibold" : "")}>
                        {node.artefact.name}
                    </span>
                </Button>
            {:else}
                <Empty.Root class="from-muted/50 to-background h-full bg-linear-to-b from-30% border border-dashed -pr-3">
                    <Empty.Header >
                        <Empty.Media variant="icon">
                            <LucideNewspaper />
                        </Empty.Media>
                        <Empty.Title>No artefacts found</Empty.Title>
                        <Empty.Description>
                            Try adjusting your search or filter to find what you're looking for.
                        </Empty.Description>
                    </Empty.Header>
                </Empty.Root>
            {/each}
        {/if}
	</ScrollArea>
</div>
