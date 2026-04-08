<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import { ScrollArea } from '$lib/components/ui/scroll-area/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { cn } from '$lib/utils';
	import Searchbar from '../basic/Searchbar.svelte';
    import LucideFolder from '@lucide/svelte/icons/folder';
    import * as Empty from "$lib/components/ui/empty/index.js";
    import LucideFile from '@lucide/svelte/icons/file';
    import LucideNewspaper from '@lucide/svelte/icons/newspaper';
	import { fade } from 'svelte/transition';

    interface ProjectListProps {
        onShowArtefactClick: (project: string) => void;
    }

    let { onShowArtefactClick } : ProjectListProps = $props();

    function onClickProject(project: string) {
        if (globalState.selectedProject === project) return;
        globalState.selectProject(project);
    }

    function onSearch(query: string) {
        globalState.searchProject(query);
    }
</script>

<div class="w-full h-full rounded-lg bg-page-foreground p-4 shadow-sm flex flex-col" in:fade>
	<div class="flex gap-3 items-center">
        <LucideFile />
        <h2 class="text-lg font-semibold">Projects</h2>
    </div>

    <Searchbar class="bg-page-background mt-3 mb-5" placeholder="Filter projects..." {onSearch} />

    <ScrollArea orientation="vertical" class="flex-1 min-h-0 pr-3 overflow-x-hidden">
        {#if globalState.state === 'LOADING'}
            {#each Array.from({ length: 8 }, (_, i) => i) as _(_)}
                <Skeleton class="h-5.5 mt-2 w-full" />
            {/each}
        {:else}
            {#each globalState.searchProjects as project(project)}
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
            {/each}
        {/if}
    </ScrollArea>
</div>
