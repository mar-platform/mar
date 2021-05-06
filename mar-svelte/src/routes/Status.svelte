
<script>
	import { onMount } from 'svelte';
    let url = MAR.toStatusURL();
	
    let models = undefined;
	onMount(async () => {
		const res = await fetch(url);
		const status = await res.json();
        models = status.models
	});
</script>

<style>
    h4, p, li {
        color: whitesmoke;        
    }

    h4 {
        font-weight: bold;
    }
</style>

<h4>Stats</h4>
{#if models == undefined }
	<p>Loading...</p>
{:else if models.length == 0}
    <p>There is something weird going on... No models found :-(</p>
{:else}
    <ul>
    {#each models as m}
        <li>{m.name} - {m.count}</li>
    {/each}
    </ul>    
{/if}

