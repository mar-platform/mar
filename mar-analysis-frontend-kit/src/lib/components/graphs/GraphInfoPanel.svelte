<script lang="ts">
    import { Button } from '$lib/components/ui/button/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
    import LucideX from '@lucide/svelte/icons/x';
    import * as Accordion from "$lib/components/ui/accordion/index.js";
	import type { Node } from '$lib/dto/Graph';
	import NodeOrEdgeItem from './NodeOrEdgeItem.svelte';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
	import type { Snippet } from 'svelte';

    const selectedNode = $derived(globalState.selectedNode);
    const graph = $derived(globalState.selectedGraph);
    
    interface Dependency {
        source: Node;
        target: Node;
        targetName: string;
        type: string;
    }

    const dependencies = $derived.by(() => {
        const deps: Array<Dependency> = [];
        if (!graph || !selectedNode) return deps;

        // We consider both incoming and outgoing edges as dependencies
        graph.forEachNeighbor(selectedNode.id, function(neighbor, attributes) {
            const edgeType = graph.hasEdge(selectedNode.id, neighbor) ?
                graph.getEdgeAttribute(selectedNode.id, neighbor, 'edgeType') :
                graph.getEdgeAttribute(neighbor, selectedNode.id, 'edgeType');
            
                deps.push({
                source: selectedNode,
                target: attributes.impl,
                targetName: attributes.label,
                type: edgeType
            });
        });
        return deps;
    });

    function closePanel() {
        globalState.deselectNode();
    }
</script>

{#snippet entry(title: string, content: string | Snippet)}
    <div class="flex flex-col">
        <span class="select-none text-sm text-text-secondary">{title}</span>
        {#if typeof content === 'string'}
            <p class="text-sm wrap-anywhere font-medium">{content}</p>
        {:else}
            {@render content()}
        {/if}
    </div>
{/snippet}

{#if selectedNode !== null && graph !== null}
    <div class="min-w-80 max-w-80 bg-page-foreground rounded-lg shadow-sm p-4 flex flex-col">
        <Button size="icon-sm" variant="ghost" class="ml-auto rounded-full" onclick={closePanel}>
            <LucideX class="" />
        </Button>

        <Accordion.Root type="multiple" value={['artefact', 'members', 'dependencies']} >
            <Accordion.Item value="artefact">
                <Accordion.Trigger class="text-base mt-2">Artefact</Accordion.Trigger>
                <Accordion.Content class="flex flex-col gap-4">
                    {@render entry('Id', selectedNode.id)}
                    {#if selectedNode._type === 'artefact'}
                        {#snippet artefactType()}
                            <NodeOrEdgeItem
                                type="node"
                                class="w-fit mt-1"
                                name={selectedNode.artefact.type}
                                checked={false}
                                color={`var(${nodeTypes[selectedNode.artefact.type as keyof typeof nodeTypes].color})`}
                            />
                        {/snippet}
                        {@render entry('Type', artefactType)}
                        {@render entry('Name', selectedNode.artefact.name)}
                    {:else if selectedNode._type === 'virtual'}
                        {@render entry('Kind', selectedNode.kind)}    
                    {/if}
                </Accordion.Content>
            </Accordion.Item>

            <!-- Artefact members of this group -->
            {#if selectedNode._type === 'virtual' && selectedNode.artefacts}
                <Accordion.Item value="members">
                    <Accordion.Trigger class="text-base">Members</Accordion.Trigger>
                    <Accordion.Content class="flex flex-col gap-4">
                        {#each selectedNode.artefacts as nodeId(nodeId)}
                            {@const attrs = graph.hasNode(nodeId) ? graph.getNodeAttributes(nodeId) : null}
                            {#if attrs}
                                <button
                                    class="text-left hover:underline cursor-pointer"
                                    onclick={() => globalState.selectNode(attrs.impl)}
                                >{attrs.label}</button>
                            {:else}
                                <span class="text-muted-foreground">{nodeId}</span>
                            {/if}
                        {/each}
                    </Accordion.Content>
                </Accordion.Item>
            {/if}

            <!-- Dependencies -->
            <Accordion.Item value="dependencies">
                <Accordion.Trigger class="text-base">Dependencies</Accordion.Trigger>
                <Accordion.Content class="flex flex-col gap-1">
                    {#each dependencies as dep(dep)}
                        <Button
                            size="sm"
                            variant="secondary"
                            class="rounded-full justify-start w-full"
                            title={dep.targetName}
                            onclick={() => globalState.selectNode(dep.target)}
                        >
                            {dep.targetName}
                        </Button>
                    {:else}
                        <span class="text-text-secondary">No dependencies found</span>
                    {/each}
                </Accordion.Content>
            </Accordion.Item>
        </Accordion.Root>

    </div>
{/if}
