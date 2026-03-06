<script lang="ts">
  import GraphVisualizer from './GraphVisualizer.svelte';
  import API from './API';
  import { onMount } from "svelte";
  import { artefactTypes, duplicationTypes } from './GraphNodeTypes';

  let document = $state<any>(undefined);

  onMount(async () => {
    fetch(API.megamodelGraph()).
          then(res => res.json()).
          then(json => {console.log(json); return json;}).
          then(doc => document = doc);
  });
</script>

{#if document}
  <GraphVisualizer document={document} types={[...artefactTypes, duplicationTypes]}/>
{/if}
