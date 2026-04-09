<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { cn } from '$lib/utils';
	import Searchbar from '../basic/Searchbar.svelte';
    import LucideFolder from '@lucide/svelte/icons/folder';
    import * as Empty from "$lib/components/ui/empty/index.js";
    import LucideFile from '@lucide/svelte/icons/file';
    import LucideNewspaper from '@lucide/svelte/icons/newspaper';
	import { fade } from 'svelte/transition';
	import { DEFAULT_PAGE_SIZE } from '$lib/constants/values';
	import List from '../basic/List.svelte';

    interface ProjectListProps {
        onShowArtefactClick: (project: string) => void;
    }

    let { onShowArtefactClick } : ProjectListProps = $props();

    let items = $state<string[]>([]);
    let isLoading = $state(false);
    let hasMore = $state(true);
    let loadingStyle = $state<'skeleton' | 'spinner'>('skeleton');
    let totalItems = $state<number | null>(null);

    let currentPage = 1;
    let query = "";
    let initialLoadDone = false;

    async function loadData() {
        if (isLoading || !hasMore) return;

        isLoading = true;

        // Fetch next items
        const result = await globalState.fetchProjects(query, currentPage, DEFAULT_PAGE_SIZE);
        console.log("Fetched items:", result);

        items.push(...result.data);
        hasMore = result.hasMore;
        totalItems = result.total;
        currentPage++;
        loadingStyle = 'spinner'; // Switch to spinner for subsequent loads

        isLoading = false;
    }

    function onClickProject(project: string) {
        if (globalState.selectedProject === project) return;
        globalState.selectProject(project);
    }

    function onSearch(q: string) {
        items = [];
        currentPage = 1;
        hasMore = true;
        query = q;
        totalItems = null;
        loadingStyle = 'skeleton'; // Use skeleton for new searches
        loadData();
    }

    // Initial load
    $effect(() => {
        if (globalState.state !== 'LOADING' && !initialLoadDone) {
            initialLoadDone = true;
            loadData();
        }
    });
</script>

<div class="w-full h-full rounded-lg bg-page-foreground p-4 shadow-sm flex flex-col" in:fade>
	<div class="flex gap-3 items-center">
        <LucideFile />
        <h2 class="text-lg font-semibold">Projects</h2>
        {#if totalItems === null}
            <Skeleton class="min-w-6 min-h-6 rounded-full" />
        {:else} 
            <div class="py-0.5 min-w-6 px-2 flex items-center justify-center bg-accent rounded-full text-sm">
                {totalItems}
            </div>
        {/if}
    </div>

    <Searchbar class="bg-page-background mt-3 mb-5" placeholder="Filter projects..." {onSearch} />

    {#if globalState.state === 'LOADING'}
        {#each Array.from({ length: 3 }, (_, i) => i) as _(_)}
            <Skeleton class="h-5.5 mt-2 w-full" />
        {/each}
    {:else}
        <List
            {isLoading}
            {hasMore}
            {loadingStyle}
            onLoadMore={loadData}
        >
            {#each items as project(project)}
                <div class="grid grid-cols-[1fr_auto] grid-rows-1 gap-2">
                    <Button size="sm" variant={globalState.selectedProject === project ? "secondary" : "ghost"} class="min-w-0 rounded-full justify-start" onclick={() => onClickProject(project)}>
                        <span title={project} class={cn("whitespace-nowrap overflow-hidden text-ellipsis font-normal", globalState.selectedProject === project ? "font-semibold" : "")}>
                            {project}
                        </span>
                    </Button>
                    <Button title="Show Artefacts" variant="ghost" size="icon-sm" class="rounded-full" onclick={() => onShowArtefactClick(project)}>
                        <LucideNewspaper />
                    </Button>
                </div>
            {:else}
                {#if !isLoading}
                    <Empty.Root class="min-h-60 h-full from-muted/50 to-background bg-linear-to-b from-30% border border-dashed -pr-3">
                        <Empty.Header >
                            <Empty.Media variant="icon">
                                <LucideFolder />
                            </Empty.Media>
                            <Empty.Title>No projects found</Empty.Title>
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
