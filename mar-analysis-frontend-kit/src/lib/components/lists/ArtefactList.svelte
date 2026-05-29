<script lang="ts">
	import LucideArrowLeft from '@lucide/svelte/icons/arrow-left';
	import { Button } from '$lib/components/ui/button/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
	import { cn } from '$lib/utils';
	import Searchbar from '../basic/Searchbar.svelte';
    import LucideNewspaper from '@lucide/svelte/icons/newspaper';
    import * as Empty from "$lib/components/ui/empty/index.js";
	import type { ArtefactNode } from '$lib/dto/Graph';
	import { fade } from 'svelte/transition';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { DEFAULT_PAGE_SIZE, MIN_WAIT_TIME_MS } from '$lib/constants/values';
	import { paginateArray } from '$lib/utils/pagination';
	import List from '../basic/List.svelte';
	import { untrack } from 'svelte';

    interface ArtefactListProps {
        onClickBackButton?: () => void;
    }

    let { onClickBackButton } : ArtefactListProps = $props();

    let items = $state<ArtefactNode[]>([]);
    let isLoading = $state(false);
    let hasMore = $state(true);
    let loadingStyle = $state<'skeleton' | 'spinner'>('skeleton');
    let totalItems = $state<number | null>(null);

    let currentPage = 1;
    let query = $state("");

    async function loadData() {
        if (isLoading || !hasMore || nodes === 'LOADING') return;

        isLoading = true;

        // Fetch next items
        const minWaitTime = new Promise((resolve) => setTimeout(resolve, MIN_WAIT_TIME_MS));
        const result = paginateArray(nodes as ArtefactNode[], currentPage, DEFAULT_PAGE_SIZE);
        await minWaitTime;
        console.log("Fetched items:", result);

        items.push(...result.data);
        hasMore = result.hasMore;
        totalItems = result.total;
        currentPage++;
        loadingStyle = 'spinner'; // Switch to spinner for subsequent loads

        isLoading = false;
    }

    let nodes = $state<ArtefactNode[] | 'LOADING'>('LOADING');

    function onClickArtefact(node: ArtefactNode) {
        if (globalState.selectedNode?.id === node.id) return;
        globalState.selectNode(node);
    }

    function onSearch(q: string) {
        items = [];
        currentPage = 1;
        hasMore = true;
        query = q;
        totalItems = null;
        loadingStyle = 'skeleton'; // Use skeleton for new searches
        // NOTE: Data will be loaded from the effect when nodes update!
    }

    function handleBackButton() {
        if (onClickBackButton) {
            onClickBackButton();
            query = '';
            nodes = 'LOADING';
        }
    }

    // Initial load when nodes change
    $effect(() => {
        if ((globalState.mode === 'PROJECT' && !globalState.selectedProject) || globalState.state === 'LOADING_GRAPH' || !globalState.selectedUnprocessedGraph) {
            nodes = 'LOADING';
            return;
        }
        const artefacts = globalState.getArtefactsFromNodes(globalState.selectedUnprocessedGraph?.nodes || [], globalState.selectedNodeTypes, query);
        nodes = artefacts;

        untrack(() => {
            loadData();
        });
    });
</script>

<div class="w-full h-full rounded-lg bg-page-foreground p-4 shadow-sm flex flex-col" in:fade>
	<div class="flex gap-3 items-center">
        {#if onClickBackButton}
            <Button title="Back to projects" variant="ghost" size="icon" class="rounded-full" onclick={handleBackButton}>
                <LucideArrowLeft />
            </Button>
        {/if}
        <LucideNewspaper />
        <h2 class="text-lg font-semibold">Artefacts</h2>
        {#if totalItems === null}
            <Skeleton class="min-w-6 min-h-6 rounded-full" />
        {:else} 
            <div class="py-0.5 min-w-6 px-2 flex items-center justify-center bg-accent rounded-full text-sm">
                {totalItems}
            </div>
        {/if}
    </div>

    <Searchbar disabled={nodes === 'LOADING'} class="bg-page-background mt-3 mb-5" placeholder="Filter artefacts..." {onSearch} />

    {#if nodes === 'LOADING'}
        {#each Array.from({ length: 3 }, (_, i) => i) as _(_)}
            <Skeleton class="h-5.5 mt-2 w-full" />
        {/each}
    {:else}
        <List
            {isLoading}
            {hasMore}
            {loadingStyle}
            showNoMore={false}
            onLoadMore={loadData}
        >
            {#each items as node(node.id)}
                <Button size="sm" variant={globalState.selectedNode?.id === node.id ? "secondary" : "ghost"} class="gap-2 rounded-full justify-start w-full" onclick={() => onClickArtefact(node)}>
                    <div class="min-w-2 min-h-2 rounded-full" style={`background-color: ${`var(${nodeTypes[node.type as keyof typeof nodeTypes].color})`};`}></div>
                    <span title={node.name} class={cn("whitespace-nowrap overflow-hidden text-ellipsis font-normal", globalState.selectedNode?.id === node.id ? "font-semibold" : "")}>
                        {node.name}
                    </span>
                </Button>
            {:else}
                {#if !isLoading}
                    <Empty.Root class="min-h-60 h-full from-muted/50 to-background bg-linear-to-b from-30% border border-dashed -pr-3">
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
                {/if}
            {/each}
        </List>
    {/if}
</div>
