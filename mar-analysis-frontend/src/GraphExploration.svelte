<script lang="ts">
  import GraphVisualizer from './GraphVisualizer.svelte';

  //  import logo from './assets/svelte.png'
  //  import Counter from './lib/Counter.svelte'
  import { onMount } from "svelte";
  import { artefactTypes } from './GraphNodeTypes';

  onMount(async () => {
    call();
  });

  let document;

  async function call() {
    //fetch(`https://localhost:8443/graph`)
    fetch(`http://localhost:8080/graph`)
      .then(apiResponse => apiResponse.json())
      .then(doc => {
        document = doc;
      })
  }

</script>

{#if document}
  <GraphVisualizer document={document} types={artefactTypes} />
{/if}
