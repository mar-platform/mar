<script lang="ts">
  import GraphVisualizer from './GraphVisualizer.svelte';
  import API from './API';
  import { artefactTypes } from './GraphNodeTypes';
  import { onMount } from 'svelte';

  let allSubgraphs: any[] = $state([]);
  let filteredSubgraphs: any[] = $state([]);
  let document = $state<any>(undefined);
  let search = $state('');

  onMount(async () => {
    fetch(API.componentGraph())
      .then(res => res.json())
      .then(data => {
        allSubgraphs = data.subgraphs ?? [];
        filteredSubgraphs = allSubgraphs;
      });
  });

  $effect(() => {
    const q = search.trim().toLowerCase();
    filteredSubgraphs = q
      ? allSubgraphs.filter(sg => sg.name.toLowerCase().includes(q))
      : allSubgraphs;
  });

  const selectComponent = (subgraph: any) => {
    document = subgraph;
  };
</script>

{#snippet componentList()}
  <div class="flex flex-col gap-2">
    <strong class="text-sm">Components ({allSubgraphs.length})</strong>
    <input
      class="h-6 text-xs px-2 py-0 w-full rounded-md border border-input bg-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
      type="text"
      placeholder="Search…"
      bind:value={search}
    />
    <ul class="text-sm space-y-0.5 overflow-y-auto max-h-96">
      {#each filteredSubgraphs as subgraph (subgraph.name)}
        <li>
          <button
            class="text-left hover:underline cursor-pointer {document?.name === subgraph.name ? 'font-bold text-primary' : ''}"
            onclick={() => selectComponent(subgraph)}
          >{subgraph.name} ({subgraph.nodeCount})</button>
        </li>
      {/each}
    </ul>
  </div>
{/snippet}

{#snippet nodeList()}
  <div class="flex flex-col gap-1">
    <strong class="text-sm">Elements ({document?.nodes?.length ?? 0})</strong>
    <ul class="text-sm space-y-0.5 overflow-y-auto max-h-80">
      {#each (document?.nodes ?? []) as node (node.id)}
        <li class="truncate text-xs">
          {node._type === 'artefact' ? node.artefact.name : node.id}
        </li>
      {/each}
    </ul>
  </div>
{/snippet}

{#if document}
  <GraphVisualizer {document} types={artefactTypes} rightPanel={nodeList}>
    {#snippet children()}
      {@render componentList()}
    {/snippet}
  </GraphVisualizer>
{:else}
  <div class="max-w-xs">
    {@render componentList()}
  </div>
{/if}
