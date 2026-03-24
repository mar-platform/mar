<script lang="ts">
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { fade } from 'svelte/transition';
	import Searchbar from '../basic/Searchbar.svelte';
	import GraphVisualizer from './GraphVisualizer.svelte';
    import LucideLoaderCircle from '@lucide/svelte/icons/loader-circle';
	import { artefactTypes } from '$lib/constants/graphNodeTypes';

</script>

{#if globalState.selectedProject}
    <div class="w-full h-full flex flex-col gap-2" in:fade out:fade>
        <div class="flex justify-between items-end">
            {#key globalState.selectedProject.id}
                <span class="mb-0.5 text-text-secondary text-base font-medium" in:fade>{globalState.selectedProject.id}</span>   
            {/key}
            <Searchbar placeholder="Filter nodes..." onSearch={(query) => console.log("Search query:", query)} />
        </div>

        <div class="flex-1 bg-page-foreground rounded-lg shadow-sm">
            {#if globalState.selectedProjectGraph !== null}
                <GraphVisualizer document={globalState.selectedProjectGraph} types={artefactTypes} />
            {:else}
                <div class="relative p-4 w-full h-full">
                    <LucideLoaderCircle class="animate-spin absolute top-1/2 left-1/2 -mt-10 -ml-10 w-10 h-10 z-10 text-text-placeholder" />
                    <Skeleton class="p-4 w-full h-full" />
                </div>
            {/if}
        </div>
    </div>
{/if}
