<script lang="ts">
    import type Graph from 'graphology'

    export let node
    export let graph : Graph

    interface Dependency {
        source: any; // node
        target: any; // node
        targetName: String,
        type: String;
    }

    let dependencies : Array<Dependency>;

    $: {
        console.log(node);
        dependencies = []
        graph.forEachNeighbor(node.id, function(neighbor, attributes) {
            // console.log(graph.hasEdge(node.id, neighbor));
            // console.log(graph.hasEdge(neighbor, node.id));

            const edgeType = graph.hasEdge(node.id, neighbor) ? 
                graph.getEdgeAttribute(node.id, neighbor, 'edgeType') :
                graph.getEdgeAttribute(neighbor, node.id, 'edgeType');

            dependencies.push({
                source: node, 
                target: attributes.impl,
                targetName: attributes.label,
                type: edgeType 
            });
        });
    }
</script>

<h2>Artefact</h2>
<div>Id: {node.id}</div>
{#if node._type == 'artefact'}
    <div>Type: {node.artefact.type}</div>
    <div>Name: {node.artefact.name}</div>
{:else if node._type == 'virtual'}
    {#each node.artefacts as node_id}
        <div>{node_id}</div>    
    {/each}        
{/if}

<h2>Dependencies</h2>
{#each dependencies as dep}
    <div>{dep.targetName}</div>    
{/each}