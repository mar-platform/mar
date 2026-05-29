<script lang="ts">
    import API from './API';
    import { onMount } from "svelte";

    let stats = $state<any>(undefined);

    onMount(async () => {
        fetch(API.stats())
            .then(res => res.json())
            .then(doc => {
                stats = doc;
            });
    });
</script>

{#if stats}
<div class="flex gap-10 ml-5">
    <div>
        <h2 class="text-sm font-bold">Crawled artefacts</h2>
        <table>
            <thead>
                <tr>
                    <th class="text-left pr-4">Type</th>
                    <th class="text-right">Count</th>
                </tr>
            </thead>
            <tbody>
                {#each Object.entries(stats.raw.artefactTypeCount) as [type, count]}
                <tr>
                    <td class="text-left pr-4">{type}</td>
                    <td class="text-right">{new Intl.NumberFormat('en-US').format(count as number)}</td>
                </tr>
                {/each}
            </tbody>
        </table>
    </div>
    <div>
        <h2 class="text-sm font-bold">Megamodel artefacts</h2>
        <table>
            <thead>
                <tr>
                    <th class="text-left pr-4">Type</th>
                    <th class="text-right">Count</th>
                </tr>
            </thead>
            <tbody>
                {#each Object.entries(stats.mega.artefactTypeCount) as [type, count]}
                <tr>
                    <td class="text-left pr-4">{type}</td>
                    <td class="text-right">{new Intl.NumberFormat('en-US').format(count as number)}</td>
                </tr>
                {/each}
            </tbody>
        </table>
    </div>
    <div>
        <h2 class="text-sm font-bold">Recovered artefacts</h2>
        <table>
            <thead>
                <tr>
                    <th class="text-left pr-4">Type</th>
                    <th class="text-right">Completion</th>
                </tr>
            </thead>
            <tbody>
                {#each Object.entries(stats.artefactRecoveryCompletion) as [type, count]}
                <tr>
                    <td class="text-left pr-4">{type}</td>
                    <td class="text-right">{new Intl.NumberFormat('en-US').format(count as number)}%</td>
                </tr>
                {/each}
            </tbody>
        </table>
    </div>
</div>
<div class="mt-8 ml-4">
    <div>Total raw artefacts (excluding configuration files): {stats.totalRaw}</div>
    <div>Total recovered artefacts: {stats.totalMega}</div>
    <div>Recovery completion: {stats.totalCompletion}</div>
</div>
{/if}
