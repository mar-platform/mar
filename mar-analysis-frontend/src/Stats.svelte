<script lang="js">
    import API from './API';
    import { onMount } from "svelte";

    let stats;

    onMount(async () => {
        fetch(API.stats())
            .then(res => res.json())
            .then(doc => {
                stats = doc;
                console.log(stats);
            });
    });
</script>  

<style>
#artefact-count td, tr{
    padding-left: 15px;
}
</style>

{#if stats}
        
    <table id="artefact-count">
        <tr>
            <th>Type</th>
            <th>Count</th>
        </tr>
        {#each Object.entries(stats.artefactTypeCount) as [type, count]}
        <tr>
            <td>{type}</td>
            <td>{new Intl.NumberFormat('en-US', { maximumSignificantDigits: 3 }).format(count)}</td>
        </tr>
        {/each}
    </table> 

{/if}