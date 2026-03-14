<script lang="ts">
  import GraphVisualizer from './GraphVisualizer.svelte';
  import API from './API';
  import { onMount } from "svelte";
  import { artefactTypes, duplicationTypes } from './GraphNodeTypes';

  let document = $state<any>(undefined);

  onMount(async () => {
    fetch(API.megamodelGraph()).
          then(res => res.json()).
          then(doc => document = doc);
  });

  $effect(() => {
    if (document != undefined) {
      console.log("Megamodel graph document:");
      console.log(document);
    }
  });
</script>

{#if document}
  <GraphVisualizer document={document} types={[...artefactTypes, ...duplicationTypes]}/>
{/if}
