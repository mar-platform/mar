<script lang="ts">
	import { Button } from '$lib/components/ui/button/index.js';
	import { ScrollArea } from '$lib/components/ui/scroll-area/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { cn } from '$lib/utils';
	import { fade } from 'svelte/transition';
	import Searchbar from '../basic/Searchbar.svelte';

</script>

{#if globalState.selectedProject}
    <div class="w-full h-full flex flex-col gap-2" in:fade out:fade>
        <div class="flex justify-between items-end">
            {#key globalState.selectedProject.id}
                <span class="mb-0.5 text-text-secondary text-sm font-medium" in:fade>{globalState.selectedProject.id}</span>   
            {/key}
            <Searchbar placeholder="Filter nodes..." onSearch={(query) => console.log("Search query:", query)} />
        </div>

        <div class="flex-1 bg-page-foreground rounded-lg shadow-sm"></div>
    </div>
{/if}
