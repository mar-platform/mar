<script lang="ts">
  import GraphVisualizer from './GraphVisualizer.svelte';
  import API from './API';
  import { onMount } from "svelte";
  import { artefactTypes, duplicationTypes } from './GraphNodeTypes';

  let document = $state<any>(undefined);
  let groupSearch = $state('');
  let visualizer = $state<any>(null);

  onMount(async () => {
    fetch(API.duplicationGraph())
          .then(res => res.json())
          .then(doc => document = doc);
  });

  function getDuplicationGroups(doc: any) {
    const q = groupSearch.trim().toLowerCase();
    return doc.nodes
      .filter((n: any) => n._type === 'virtual' && n.kind === 'duplication')
      .filter((n: any) => !q || n.id.toLowerCase().includes(q));
  }
</script>

{#if document}
  <GraphVisualizer bind:this={visualizer} document={document} types={[...artefactTypes, ...duplicationTypes]}>
    {#snippet children()}
      <div class="flex flex-col gap-2">
        <strong class="text-sm">Duplication Groups</strong>
        <input
          class="h-6 text-xs px-2 py-0 w-full rounded-md border border-input bg-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
          type="text"
          placeholder="Search…"
          bind:value={groupSearch}
        />
        <ul class="text-sm space-y-0.5 overflow-y-auto max-h-96">
          {#each getDuplicationGroups(document) as group (group.id)}
            <li>
              <button
                class="text-left hover:underline cursor-pointer"
                onclick={() => visualizer?.selectNodeById(group.id)}
              >{group.id}</button>
            </li>
          {/each}
        </ul>
      </div>
    {/snippet}
  </GraphVisualizer>
{/if}
