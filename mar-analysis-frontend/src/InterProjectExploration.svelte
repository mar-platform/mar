<script lang="ts">
    import GraphVisualizer from './GraphVisualizer.svelte';
    import API from './API';
    import { onMount } from "svelte";
    import { projectTypes } from './GraphNodeTypes';

    let document = $state<any>(undefined);

    onMount(async () => {
      fetch(API.interProjectGraph()).
            then(res => res.json()).
            then(doc => document = doc);
    });

    $effect(() => {
      if (document) console.log(document);
    });
</script>

{#if document}
  <GraphVisualizer document={document} types={projectTypes} />
{/if}
