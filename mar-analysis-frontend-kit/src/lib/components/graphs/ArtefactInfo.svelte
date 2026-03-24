<script lang="ts">
    import type Graph from 'graphology'

    let { node, graph, onNodeSelect }: {
      node: any;
      graph: Graph;
      onNodeSelect?: (node: any) => void;
    } = $props();

    interface Dependency {
        source: any;
        target: any;
        targetName: string;
        type: string;
    }

    let dependencies = $derived.by(() => {
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

<div class="text-sm flex flex-col gap-3">
  <!-- Artefact properties -->
  <div>
    <p class="font-medium mb-1">Artefact</p>
    <dl class="space-y-0.5">
      <div class="flex gap-2">
        <dt class="text-muted-foreground shrink-0">Id:</dt>
        <dd class="break-all">{node.id}</dd>
      </div>
      {#if node._type == 'artefact'}
        <div class="flex gap-2">
          <dt class="text-muted-foreground shrink-0">Type:</dt>
          <dd>{node.artefact.type}</dd>
        </div>
        <div class="flex gap-2">
          <dt class="text-muted-foreground shrink-0">Name:</dt>
          <dd class="break-all">{node.artefact.name}</dd>
        </div>
      {:else if node._type == 'virtual'}
        <div class="flex gap-2">
          <dt class="text-muted-foreground shrink-0">Kind:</dt>
          <dd>{node.kind}</dd>
        </div>
      {/if}
    </dl>
  </div>

  {#if node._type == 'virtual' && node.artefacts?.length > 0}
  <!-- Artefact members of this group -->
  <div>
    <p class="font-medium mb-1">Members ({node.artefacts.length})</p>
    <ul class="space-y-0.5">
      {#each node.artefacts as node_id}
        {@const attrs = graph.hasNode(node_id) ? graph.getNodeAttributes(node_id) : null}
        <li>
          {#if attrs}
            <button
              class="text-left hover:underline cursor-pointer"
              onclick={() => onNodeSelect?.(attrs.impl)}
            >{attrs.label}</button>
          {:else}
            <span class="text-muted-foreground">{node_id}</span>
          {/if}
        </li>
      {/each}
    </ul>
  </div>
  {/if}

  <!-- Dependencies -->
  <div>
    <p class="font-medium mb-1">Dependencies</p>
    {#if dependencies.length === 0}
      <p class="text-muted-foreground">None</p>
    {:else}
      <ul class="space-y-0.5">
        {#each dependencies as dep}
          <li>
            <button
              class="text-left hover:underline cursor-pointer"
              onclick={() => onNodeSelect?.(dep.target)}
            >{dep.targetName}</button>
          </li>
        {/each}
      </ul>
    {/if}
  </div>
</div>
