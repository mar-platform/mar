<script lang="js">
    import API from './API';
    import { onMount } from "svelte";

    let stats;

    onMount(async () => {
        fetch(API.stats())
            .then(res => res.json())
            .then(doc => {
                stats = doc;
            });
    });
</script>  

<style>
.artefact-count {
}

.count {
    text-align: right;
}

.type {
    text-align: left;
}

#stats-container {
    display: flex;
    gap: 40px;
    margin-left: 20px;
}

#stats-container h2 {
    font-size: 14pt;
}
</style>

{#if stats}
<div id="stats-container">
    <div>
        <h2>Crawled artefacts</h2>
        <table class="artefact-count">
            <tr>
                <th class="type">Type</th>
                <th class="count">Count</th>
            </tr>
            {#each Object.entries(stats.raw.artefactTypeCount) as [type, count]}
            <tr>
                <td class="type">{type}</td>
                <td class="count">{new Intl.NumberFormat('en-US', { maximumSignificantDigits: 3 }).format(count)}</td>
            </tr>
            {/each}
        </table> 
    </div>
    <div>
        <h2>Megamodel artefacts</h2>
        <table class="artefact-count">
            <tr>
                <th class="type">Type</th>
                <th class="count">Count</th>
            </tr>
            {#each Object.entries(stats.mega.artefactTypeCount) as [type, count]}
            <tr>
                <td class="type">{type}</td>
                <td class="count">{new Intl.NumberFormat('en-US', { maximumSignificantDigits: 3 }).format(count)}</td>
            </tr>
            {/each}
        </table> 
    </div>
</div>
{/if}