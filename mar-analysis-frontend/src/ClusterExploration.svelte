<script lang="ts">
    import { onMount } from "svelte";
    import API from "./API";
 
    let clusters = [];

    onMount(async () => {
    fetch(API.labelPropagation())
        .then(apiResponse => apiResponse.json())
        .then(document => {
            clusters = document;        
        })
  });
</script>

{#each clusters as cluster, idx}
    Cluster {idx}
    <ul>
    {#each cluster as artefact}
        <li>{artefact.id}</li>
    {/each}
    </ul>
{/each}