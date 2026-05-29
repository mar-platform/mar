<script lang="ts">
  import GraphVisualizer from './GraphVisualizer.svelte';
  import { onMount } from "svelte";
  import { artefactTypes } from './GraphNodeTypes';

  let document = $state<any>(undefined);

  onMount(async () => {
    fetch(`http://localhost:8080/graph`)
      .then(apiResponse => apiResponse.json())
      .then(doc => {
        document = doc;
      })
  });
</script>

{#if document}
  <GraphVisualizer document={document} types={artefactTypes} />
{/if}
