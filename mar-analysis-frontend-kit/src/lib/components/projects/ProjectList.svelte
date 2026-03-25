<script lang="ts">
	import type Project from '$lib/dto/Project';
	import { Button } from '$lib/components/ui/button/index.js';
	import { ScrollArea } from '$lib/components/ui/scroll-area/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { cn } from '$lib/utils';
	import Searchbar from '../basic/Searchbar.svelte';
    import LucideFolder from '@lucide/svelte/icons/folder';
    import * as Empty from "$lib/components/ui/empty/index.js";


    function onClickProject(project: Project) {
        if (globalState.selectedProject?.id === project.id) return;
        globalState.selectProject(project);
    }

    function onSearch(query: string) {
        globalState.searchProject(query);
    }
</script>

<div class="min-w-80 max-w-80 rounded-lg h-full bg-page-foreground p-4 shadow-sm flex flex-col">
	<h2 class="text-lg font-semibold">Projects</h2>

    <Searchbar class="bg-page-background mt-3 mb-5" placeholder="Filter projects..." {onSearch} />

	<ScrollArea class="flex-1 h-10 pr-3">
		{#if globalState.state === 'LOADING'}
            {#each Array.from({ length: 8 }, (_, i) => i) as _(_)}
                <Skeleton class="h-6 mt-2 w-full" />
            {/each}
        {:else}
            {#each globalState.searchProjects as project(project.id)}
                <Button size="sm" variant={globalState.selectedProject?.id === project.id ? "secondary" : "ghost"} class="rounded-full justify-start w-full" onclick={() => onClickProject(project)}>
                    <span title={project.id} class={cn("whitespace-nowrap overflow-hidden text-ellipsis font-normal", globalState.selectedProject?.id === project.id ? "font-semibold" : "")}>
                        {project.id}
                    </span>
                </Button>
            {:else}
                <Empty.Root class="from-muted/50 to-background h-full bg-linear-to-b from-30% border border-dashed -pr-3">
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
            {/each}
        {/if}
	</ScrollArea>
</div>
