<script lang="ts">
    import type Graph from 'graphology'

    let { node, graph }: { node: any; graph: Graph } = $props();

    interface Dependency {
        source: any;
        target: any;
        targetName: string;
        type: string;
    }

    let dependencies = $derived.by(() => {
        console.log(node);
        const deps: Array<Dependency> = [];
        graph.forEachNeighbor(node.id, function(neighbor, attributes) {
            const edgeType = graph.hasEdge(node.id, neighbor) ?
                graph.getEdgeAttribute(node.id, neighbor, 'edgeType') :
                graph.getEdgeAttribute(neighbor, node.id, 'edgeType');

            deps.push({
                source: node,
                target: attributes.impl,
                targetName: attributes.label,
                type: edgeType
            });
        });
        return deps;
    });
</script>

<h2 class="text-lg font-bold mt-2">Artefact</h2>
<div>Id: {node.id}</div>
{#if node._type == 'artefact'}
    <div>Type: {node.artefact.type}</div>
    <div>Name: {node.artefact.name}</div>
{:else if node._type == 'virtual'}
    {#each node.artefacts as node_id}
        <div>{node_id}</div>
    {/each}
{/if}

<h2 class="text-lg font-bold mt-2">Dependencies</h2>
{#each dependencies as dep}
    <div>{dep.targetName}</div>
{/each}
