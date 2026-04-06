<script lang="ts">
	import { SEARCHBAR_DEBOUNCE_MS } from "$lib/constants/values";
	import { cn } from "$lib/utils";
    import LucideSearch from '@lucide/svelte/icons/search';
    import { Button } from '$lib/components/ui/button/index.js';
    import LucideX from '@lucide/svelte/icons/x';
	import { fade } from "svelte/transition";
	import { tick } from "svelte";
    
    interface SearchbarProps {
        onSearch: (query: string) => Promise<void> | void;
        placeholder: string;
        class?: string;
        disabled?: boolean;
    }

    let { onSearch, placeholder, class: className, disabled }: SearchbarProps = $props();

    // Apply a debounce to the search function to avoid excessive calls
    let timeout = $state<NodeJS.Timeout | null>(null);
    let query = $state('');
    let lastSearch = $state('');

    function handleInput(event: Event) {
        const target = event.target as HTMLInputElement;
        const query = target.value;

        if (timeout) {
            clearTimeout(timeout);
        }

        timeout = setTimeout(() => {
            executeSearch(query);
        }, SEARCHBAR_DEBOUNCE_MS);
    }

    function executeSearch(query: string) {
        if (timeout) {
            clearTimeout(timeout);
        }
        timeout = null;

        if (query === lastSearch) {
            return; // No need to search if the query hasn't changed
        }

        lastSearch = query;
        onSearch(query);
    }

    function handleKeyDown(event: KeyboardEvent) {
        if (event.key === 'Enter') {
            const target = event.target as HTMLInputElement;
            executeSearch(target.value);
        }
    }

    async function clearSearch() {
        query = '';
        await tick()
        onSearch('');
    }
</script>

<div class={cn("relative flex gap-2 py-2 px-2 bg-input-background border border-input-border rounded-full", className)}>
    <LucideSearch class="text-text-placeholder h-5 w-5" />
    <input
        bind:value={query}
        autocomplete="off"
        class="text-sm outline-none w-[calc(100%-3.5rem)]"
        oninput={handleInput}
        placeholder={placeholder}
        onkeydown={handleKeyDown}
        {disabled}
    >
    {#if query !== ''}
        <div in:fade out:fade class="absolute right-2 top-1.5">
            <Button variant="ghost" class="rounded-full" size="icon-xs" onclick={clearSearch} aria-label="Clear search">
                <LucideX class="text-text-placeholder" />
            </Button>
        </div>
    {/if}
</div>
