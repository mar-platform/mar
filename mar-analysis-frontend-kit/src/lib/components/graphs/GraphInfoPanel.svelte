<script lang="ts">
    /* eslint-disable svelte/no-navigation-without-resolve */
    import { Button } from '$lib/components/ui/button/index.js';
	import { globalState } from '$lib/stores/globalState.svelte';
    import LucideX from '@lucide/svelte/icons/x';
    import * as Accordion from "$lib/components/ui/accordion/index.js";
	import type { Node } from '$lib/dto/Graph';
	import NodeOrEdgeItem from './NodeOrEdgeItem.svelte';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
	import type { Snippet } from 'svelte';
    import { ScrollArea } from "$lib/components/ui/scroll-area/index.js";
	import { edgeTypes } from '$lib/constants/edgeTypes';
	import { fade } from 'svelte/transition';
    import { Skeleton } from "$lib/components/ui/skeleton/index.js";
	import { getArtefactGithubLink, getProjectGithubLink } from '$lib/utils/links';
    import GithubWhiteLogo from '$lib/assets/github-white-icon.svg';
	import type GithubUser from '$lib/dto/GithubUser';
	import { SELECTED_PROJECT_GRAPHS_EXPLORATION_PATH } from '$lib/constants/routes';
    import LucideGitCompare from '@lucide/svelte/icons/git-compare';

    interface Dependency {
        source: Node;
        target: Node;
        targetName: string;
        type: string;
    }
    const selectedNode = $derived(globalState.selectedNode);
    const selectedEdge = $derived(globalState.selectedEdge);
    const graph = $derived(globalState.selectedGraph);
    
    const artefactExtraInfo = $derived.by(() => {
        if (!selectedNode || selectedNode._type !== 'artefact') return null;
        return globalState.getArtefactInfo(selectedNode.id);
    });

    const projectExtraInfo = $derived.by(() => {
        if (!selectedNode || selectedNode._type !== 'virtual' || selectedNode.kind !== 'project') return null;
        return globalState.getProjectInfo(selectedNode.id);
    });

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
        globalState.deselectNodeOrEdge();
    }
</script>

{#snippet entry(title: string, content: string | Snippet, link: string | null = null)}
    <div class="flex flex-col">
        <span class="select-none text-sm text-text-secondary">{title}</span>
        {#if typeof content === 'string'}
            {#if link}
                <a href={link} target="_blank" class="text-sm wrap-anywhere font-medium no-underline hover:font-semibold">{content}</a>
            {:else}
                <p class="text-sm wrap-anywhere font-medium">{content}</p>
            {/if}
        {:else}
            {@render content()}
        {/if}
    </div>
{/snippet}

{#snippet githubUser(userData: GithubUser)}
    <Button class="flex gap-1 justify-start items-center h-12 px-1" variant="ghost" href={userData.url} target="_blank">
        <img src={userData.photoUrl} alt={userData.name} class="h-8 w-8 rounded-full mr-2 border border-input-border" />
        <div class="flex flex-col items-start">
            <span class="text-sm font-medium">{userData.name}</span>
            <span class="text-xs text-text-secondary">{userData.id}</span>
        </div>
    </Button>
{/snippet}

{#if (selectedNode !== null || selectedEdge !== null) && graph !== null}
    <div id="details" class="min-w-85 min-[1100px]:max-w-85 h-lvh min-[1100px]:h-[calc(100svh-152px-40px)] rounded-lg bg-page-foreground p-4 shadow-sm flex flex-col" in:fade>
        <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold">Details</h2>
            <Button size="icon-sm" variant="ghost" class="rounded-full" onclick={closePanel}>
                <LucideX />
            </Button>
        </div>

        <ScrollArea type="hover" class="flex-1 min-h-0 pr-4">
            <Accordion.Root type="multiple" value={['artefact', 'project', 'duplication', 'members', 'dependencies', 'relationship']} >
                {#if selectedNode !== null}
                    <!-- Artefact details -->
                    {#if selectedNode._type === 'artefact'}
                        <Accordion.Item value="artefact">
                            <Accordion.Trigger class="text-base mt-2 flex items-center gap-3">
                                Artefact
                                <NodeOrEdgeItem
                                    type="node"
                                    class="w-fit mt-1"
                                    name={selectedNode.type}
                                    checked={false}
                                    color={`var(${nodeTypes[selectedNode.type as keyof typeof nodeTypes].color})`}
                                />
                            </Accordion.Trigger>
                            <Accordion.Content class="flex flex-col gap-4">
                                {@render entry('Id', selectedNode.id)}
                                {@render entry('Name', selectedNode.name)}
                                {#await artefactExtraInfo}
                                    {#each Array.from({ length: 5 }, (_, i) => i) as _(_)}
                                        <Skeleton class="h-10 w-full" />  
                                    {/each}
                                {:then resolvedInfo}
                                    {#if resolvedInfo !== null}
                                        {#snippet author()}
                                            {@render githubUser(resolvedInfo.createdAuthorData!)}
                                        {/snippet}
                                        {@render entry('Author', resolvedInfo.createdAuthorData ? author: resolvedInfo.createdAuthor)}
                                        {@render entry('Last Updated By', resolvedInfo.updatedAuthor)}
                                        {@render entry('Created At', new Date(resolvedInfo.createdAt).toLocaleString())}
                                        {@render entry('Last Updated At', new Date(resolvedInfo.updatedAt).toLocaleString())}
                                        <Button class="gap-3 border border-input-border text-white bg-black hover:text-white! hover:bg-black/85" href={getArtefactGithubLink(resolvedInfo.project, selectedNode.id)} target="_blank" size="default">
                                            View on GitHub
                                            <img src={GithubWhiteLogo} alt="GitHub" class="h-4 aspect-square w-auto" />
                                        </Button>
                                    {/if}
                                {/await}
                            </Accordion.Content>
                        </Accordion.Item>
                    {/if}
        
                    <!-- Virtual node -->
                    {#if selectedNode._type === 'virtual'}
                        {#if selectedNode.kind === 'project'}
                            <Accordion.Item value="project">
                                <Accordion.Trigger class="text-base mt-2 flex items-center gap-3">
                                    Project
                                </Accordion.Trigger>
                                <Accordion.Content class="flex flex-col gap-4">
                                    {@render entry('Id', selectedNode.id)}
                                    {#await projectExtraInfo}
                                        {#each Array.from({ length: 6 }, (_, i) => i) as _(_)}
                                            <Skeleton class="h-10 w-full" />  
                                        {/each}
                                    {:then resolvedInfo}
                                        {#if resolvedInfo !== null}
                                            {#snippet author()}
                                                {@render githubUser(resolvedInfo.authorData!)}
                                            {/snippet}
                                            {@render entry('Description', resolvedInfo.description || 'No description available')}
                                            {@render entry('Author', resolvedInfo.authorData ? author: resolvedInfo.author)}
                                            {@render entry('Created At', new Date(resolvedInfo.createdAt).toLocaleString())}
                                            {@render entry('Last Updated At', new Date(resolvedInfo.updatedAt).toLocaleString())}
                                            <div class="flex flex-col gap-1">
                                                <Button class="gap-3 border border-input-border text-white bg-black hover:text-white! hover:bg-black/85" href={getProjectGithubLink(resolvedInfo.id)} target="_blank" size="default">
                                                    View on GitHub
                                                    <img src={GithubWhiteLogo} alt="GitHub" class="h-4 aspect-square w-auto" />
                                                </Button>
                                                <Button class="gap-3 border border-input-border" variant="secondary" href={SELECTED_PROJECT_GRAPHS_EXPLORATION_PATH(resolvedInfo.id)} size="default">
                                                    Inspect project
                                                    <LucideGitCompare />
                                                </Button>
                                            </div>
                                        {/if}
                                    {/await}
                                </Accordion.Content>
                            </Accordion.Item>
                        {:else if selectedNode.kind === 'duplication'}
                            <Accordion.Item value="duplication">
                                <Accordion.Trigger class="text-base mt-2 flex items-center gap-3">
                                    Artefact Group
                                    <NodeOrEdgeItem
                                        type="node"
                                        class="w-fit mt-1"
                                        name={selectedNode.artefactType!}
                                        checked={false}
                                        color={`var(${nodeTypes[selectedNode.artefactType! as keyof typeof nodeTypes].color})`}
                                    />
                                </Accordion.Trigger>
                                <Accordion.Content class="flex flex-col gap-4">
                                    {@render entry('Id', selectedNode.id)}
                                </Accordion.Content>
                            </Accordion.Item>

                            <!-- Artefact members of this group -->
                            {#if selectedNode.artefacts}
                                <Accordion.Item value="members">
                                    <Accordion.Trigger class="text-base flex items-center gap-3">
                                        Members
                                        <div class="py-0.5 min-w-6 px-2 flex items-center justify-center bg-accent rounded-full text-sm">
                                            {selectedNode.artefacts.length}
                                        </div>
                                    </Accordion.Trigger>
                                    <Accordion.Content class="flex flex-col gap-1">
                                        {#each selectedNode.artefacts as nodeId(nodeId)}
                                            {@const attrs = graph.hasNode(nodeId) ? graph.getNodeAttributes(nodeId) : null}
                                            {#if attrs}
                                                <Button
                                                    size="sm"
                                                    variant="secondary"
                                                    class="rounded-full justify-start w-full"
                                                    title={attrs.label}
                                                    onclick={() => globalState.selectNode(attrs.impl)}
                                                >
                                                    <span title={attrs.label} class="whitespace-nowrap overflow-hidden text-ellipsis">
                                                        {attrs.label}
                                                    </span>
                                                </Button>
                                            {:else}
                                                <span class="whitespace-nowrap overflow-hidden text-ellipsis text-muted-foreground">{nodeId}</span>
                                            {/if}
                                        {:else}
                                            <span class="text-text-secondary">No members found</span>
                                        {/each}
                                    </Accordion.Content>
                                </Accordion.Item>
                            {/if}
                        {/if}
                    
                    {/if}
        
                    <!-- Dependencies -->
                    <Accordion.Item value="dependencies">
                        <Accordion.Trigger class="text-base flex items-center gap-3">
                            Dependencies
                            <div class="py-0.5 min-w-6 px-2 flex items-center justify-center bg-accent rounded-full text-sm">
                                {dependencies.length}
                            </div>
                        </Accordion.Trigger>
                        <Accordion.Content class="flex flex-col gap-1">
                            {#each dependencies as dep(dep)}
                                <Button
                                    size="sm"
                                    variant="secondary"
                                    class="rounded-full justify-start w-full"
                                    title={dep.targetName}
                                    onclick={() => globalState.selectNode(dep.target)}
                                >
                                    <span class="whitespace-nowrap overflow-hidden text-ellipsis">
                                        {dep.targetName}
                                    </span>
                                </Button>
                            {:else}
                                <span class="text-text-secondary">No dependencies found</span>
                            {/each}
                        </Accordion.Content>
                    </Accordion.Item>
                {:else if selectedEdge !== null}
                    {#snippet sourceNode()}
                        <Button
                            size="sm"
                            variant="secondary"
                            class="mt-1 rounded-full justify-start w-full"
                            title={selectedEdge.source}
                            onclick={() => globalState.selectNode(graph.getNodeAttribute(selectedEdge.source, 'impl'))}
                        >
                            <span title={selectedEdge.source} class="whitespace-nowrap overflow-hidden text-ellipsis">
                                {graph.getNodeAttribute(selectedEdge.source, 'label')}
                            </span>
                        </Button>
                    {/snippet}
                    {#snippet targetNode()}
                        <Button
                            size="sm"
                            variant="secondary"
                            class="mt-1 rounded-full justify-start w-full"
                            title={selectedEdge.target}
                            onclick={() => globalState.selectNode(graph.getNodeAttribute(selectedEdge.target, 'impl'))}
                        >
                            <span title={selectedEdge.target} class="whitespace-nowrap overflow-hidden text-ellipsis">
                                {graph.getNodeAttribute(selectedEdge.target, 'label')}
                            </span>
                        </Button>
                    {/snippet}

                    <!-- Edge details -->
                    <Accordion.Item value="relationship">
                        <Accordion.Trigger class="text-base mt-2 flex items-center gap-3">
                            Relationship
                            <NodeOrEdgeItem
                                type="edge"
                                class="w-fit mt-1"
                                name={selectedEdge.types[0]}
                                checked={false}
                                color={`var(${edgeTypes[selectedEdge.types[0] as keyof typeof edgeTypes].color})`}
                            />
                        </Accordion.Trigger>
                        
                        <Accordion.Content class="flex flex-col gap-4">
                            {@render entry('Source', sourceNode)}
                            {@render entry('Target', targetNode)}
                        </Accordion.Content>
                    </Accordion.Item>
                {/if}
            </Accordion.Root>
        </ScrollArea>
    </div>
{/if}
