<script lang="ts">
	import { ScrollArea } from '$lib/components/ui/scroll-area/index.js';
	import type { Snippet } from 'svelte';
	import { infiniteScroll } from '$lib/actions/infiniteScroll';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
    import LucideLoaderCircle from '@lucide/svelte/icons/loader-circle';
	import { cn } from '$lib/utils';

    interface ListProps {
        loadingStyle: 'skeleton' | 'spinner';
        orientation?: 'vertical' | 'horizontal';
        isLoading: boolean;
        hasMore: boolean;
        onLoadMore: () => void;
        children: Snippet;
        class?: string;
    }

    let {
        loadingStyle,
        orientation = 'vertical',
        isLoading = false,
        hasMore = true,
        onLoadMore,
        children,
        class: className = "",
    } : ListProps = $props();
</script>

<ScrollArea {orientation} class={cn("flex-1 min-h-0 pr-3 overflow-x-hidden", className)}>
  {@render children()}

  {#if hasMore}
    <div use:infiniteScroll={onLoadMore} class="w-full mt-2"></div>
  {/if}

  {#if isLoading}
    {#if loadingStyle === 'spinner'}
        <div class="flex items-center justify-center pb-4">
        <LucideLoaderCircle class="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
    {:else if loadingStyle === 'skeleton'}
        {#each Array.from({ length: 3 }, (_, i) => i) as _(_)}
            <Skeleton class="h-5.5 mt-2 w-full" />
        {/each}
    {/if}
  {/if}

  {#if !hasMore}
    <div class="py-4 text-center text-xs text-muted-foreground select-none">
        No more results
    </div>
  {/if}
</ScrollArea>
