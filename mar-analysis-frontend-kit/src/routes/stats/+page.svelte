<script lang="ts">
	import StatChart from "$lib/components/charts/StatChart.svelte";
	import { cn } from "$lib/utils";
	import type { PageProps } from "./$types";

    let { data }: PageProps = $props();

    const { stats } = $derived(data || {});
</script>

{#snippet smallCard(color: string, number: string | number, title: string, subtitle?: string)}
    <div class="flex items-center gap-4 shadow-sm rounded-lg p-2 pr-4 bg-page-foreground hover:scale-[1.05] transition-transform ease-in-out duration-300">
        <span class={cn(color, "py-2 px-3 rounded-lg font-semibold")}>
            {number}
        </span>
        <div class="flex flex-col select-none">
            {title}
            {#if subtitle}
                <span class="text-text-secondary text-xs">
                    {subtitle}
                </span>
            {/if}
        </div>
    </div>
{/snippet}

{#if stats}
    <div class="flex flex-col gap-10">
        <div class="flex gap-5">
            {@render smallCard("bg-blue-500/10 text-blue-500", stats.totalRaw, "Raw artefacts", "excluding configuration files")}
            {@render smallCard("bg-green-500/10 text-green-500", stats.totalMega, "Recovered artefacts")}
            {@render smallCard("bg-purple-500/10 text-purple-500", `${stats.totalCompletion.toFixed(2)}%`, "Recovery completion")}
        </div>

        <div class="flex flex-col gap-6">
            <StatChart title="Crawled Artefacts" barColor="var(--color-blue-500)" data={stats.raw.artefactTypeCount} />
            <div class="flex gap-6 flex-col lg:flex-row">
                <StatChart class="flex-1" title="Megamodel artefacts" barColor="var(--color-green-500)" data={stats.mega.artefactTypeCount} />
                <StatChart class="flex-1" title="Recovered artefacts" barColor="var(--color-purple-500)" data={stats.artefactRecoveryCompletion} />
            </div>
        </div>
    </div>
{/if}
