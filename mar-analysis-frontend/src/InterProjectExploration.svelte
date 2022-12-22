<script lang="ts">
    import GraphVisualizer from './GraphVisualizer.svelte';
    import API from './API';
  
    import { onMount } from "svelte";
    import { projectTypes } from './GraphNodeTypes';

    onMount(async () => {
      fetch(API.interProjectGraph()).
            then(res => res.json()).
            then(doc => document = doc);
    });
  
    let document;
  
    $: if (document) console.log(document);
  </script>
  
  {#if document}
    <GraphVisualizer document={document} types={projectTypes} />
  {/if}
  