<script lang="ts">
    import { Button } from '$lib/components/ui/button/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
    import LucideX from '@lucide/svelte/icons/x';
    import * as Accordion from "$lib/components/ui/accordion/index.js";

    const selectedNode = $derived(globalState.selectedNode);
    
    function closePanel() {
        globalState.deselectNode();
    }
</script>

{#snippet entry(title: string, content: string)}
    <div class="flex flex-col">
        <span class="select-none text-sm text-text-secondary">{title}</span>
        <p class="text-sm wrap-anywhere font-medium">{content}</p>
    </div>
{/snippet}

{#if selectedNode !== null}
    <div class="min-w-80 max-w-80 bg-page-foreground rounded-lg shadow-sm p-4 flex flex-col">
        <Button size="icon-sm" variant="ghost" class="ml-auto rounded-full" onclick={closePanel}>
            <LucideX class="" />
        </Button>

        <Accordion.Root type="multiple" value={['artefact']} >
            <Accordion.Item value="artefact">
                <Accordion.Trigger class="text-base">Artefact</Accordion.Trigger>
                <Accordion.Content class="flex flex-col gap-4">
                    {@render entry('Id', selectedNode.id)}
                    {#if selectedNode._type === 'artefact'}
                        {@render entry('Type', selectedNode.artefact.type)}
                        {@render entry('Name', selectedNode.artefact.name)}
                    {:else if selectedNode._type === 'virtual'}
                        {@render entry('Kind', selectedNode.kind)}    
                    {/if}
                </Accordion.Content>
            </Accordion.Item>
        </Accordion.Root>

    </div>
{/if}
