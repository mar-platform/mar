<script lang="ts">
	import DataTable from "$lib/components/basic/DataTable.svelte";
	import StatChart from "$lib/components/charts/StatChart.svelte";
	import { cn } from "$lib/utils";
	import type { ColumnDef } from "@tanstack/table-core";
	import { renderSnippet } from "$lib/components/ui/data-table/index.js";
	import { createRawSnippet } from "svelte";
	import type { PageProps } from "./$types";
	import type { ArtefactStats, MegamodelStats, ProjectStats } from "$lib/dto/Stats";
	import NodeOrEdgeItem from "$lib/components/graphs/NodeOrEdgeItem.svelte";
	import { nodeTypes } from "$lib/constants/graphNodeTypes";
	import HorizontalDataTable from "$lib/components/basic/HorizontalDataTable.svelte";

    let { data }: PageProps = $props();

    const { stats } = $derived(data || {});

    const artefactStatsColumns: ColumnDef<ArtefactStats>[] = [
        {
            accessorKey: "type",
            header: "Tool",
            cell: ({ row }) => {
                const type = row.getValue("type") as string;
                return renderSnippet(ArtefactBadge, { name: type });
            }
        },
        {
            accessorKey: "usage",
            header: "Purpose",
        },
        {
            accessorKey: "rawCount",
            header: "Count",
        },
        {
            accessorKey: "projectPercentage",
            header: "Project Occurences",
            cell: ({ row }) => {
                const formattedPercentage = Number(row.original.projectPercentage).toFixed(2);
                return `${formattedPercentage}%`;
            }
        },
        {
            accessorKey: "uniqueCount",
            header: "Unique",
            cell: ({ row }) => {
                const formattedPercentage = Number(row.original.uniquePercentageRaw).toFixed(2);
                return renderSnippet(
                    createRawSnippet(() => ({
                        render: () => `<div class="flex items-center gap-2">
                            <span>${row.original.uniqueCount}</span>
                            <span title="${row.original.uniquePercentageRaw}%" class="select-none inline-flex items-center justify-center rounded-full bg-muted/60 px-2 py-0.5 text-xs text-muted-foreground border">
                                ${formattedPercentage} %
                            </span>
                        </div>`
                    }))
                );
            }
        },
    ];

    const megamodelMetricsColumns: ColumnDef<MegamodelStats>[] = [
        {
            accessorKey: "totalArtefacts",
            header: "Artefacts",
        },
        {
            accessorKey: "totalIsolated",
            header: "Isolated",
            cell: ({ row }) => {
                const formattedPercentage = Number(row.original.percentIsolated).toFixed(2);
                return renderSnippet(
                    createRawSnippet(() => ({
                        render: () => `<div class="flex items-center gap-2">
                            <span>${row.original.totalIsolated}</span>
                            <span title="${row.original.percentIsolated}%" class="select-none inline-flex items-center justify-center rounded-full bg-muted/60 px-2 py-0.5 text-xs text-muted-foreground border">
                                ${formattedPercentage} %
                            </span>
                        </div>`
                    }))
                );
            }
        },
        {
            accessorKey: "totalEdges",
            header: "Edges",
        },
        {
            accessorKey: "avgInDegree",
            header: "Avg. Degree",
            cell: ({ row }) => {
                const formattedPercentage = Number(row.original.avgInDegree).toFixed(2);
                return `${formattedPercentage}%`;
            }
        },
        {
            accessorKey: "connectedComponentSizes",
            header: "Components",
            cell: ({ row }) => {
                const componentSizes = row.original.connectedComponentSizes;
                return componentSizes.reduce((sum, size) => sum + size, 0);
            }
        },
    ];

    const projectMetricsColumns: ColumnDef<ProjectStats>[] = [
        {
            accessorKey: "totalProjects",
            header: "Projects",
        },
        {
            accessorKey: "totalIsolated",
            header: "Isolated",
            cell: ({ row }) => {
                const formattedPercentage = Number(row.original.percentIsolated).toFixed(2);
                return renderSnippet(
                    createRawSnippet(() => ({
                        render: () => `<div class="flex items-center gap-2">
                            <span>${row.original.totalIsolated}</span>
                            <span title="${row.original.percentIsolated}%" class="select-none inline-flex items-center justify-center rounded-full bg-muted/60 px-2 py-0.5 text-xs text-muted-foreground border">
                                ${formattedPercentage} %
                            </span>
                        </div>`
                    }))
                );
            }
        },
        {
            accessorKey: "totalEdges",
            header: "Edges",
        },
        {
            accessorKey: "avgDegree",
            header: "Avg. Degree",
            cell: ({ row }) => {
                const formattedPercentage = Number(row.original.avgDegree).toFixed(2);
                return `${formattedPercentage}%`;
            }
        },
        {
            accessorKey: "connectedComponentSizes",
            header: "Components",
            cell: ({ row }) => {
                const componentSizes = row.original.connectedComponentSizes;
                return componentSizes.reduce((sum, size) => sum + size, 0);
            }
        },
    ];
</script>

{#snippet ArtefactBadge({ name }: { name: string })}
    <NodeOrEdgeItem leaveEmptySpaceWhenUnchecked={false} type="node" {name} checked={false} color={`var(${nodeTypes[name as keyof typeof nodeTypes].color})`} />
{/snippet}

{#snippet smallCard(color: string, number: string | number, title: string, subtitle?: string)}
    <div class="flex min-w-70 items-center gap-4 shadow-sm rounded-lg p-2 pr-4 bg-page-foreground hover:scale-[1.05] transition-transform ease-in-out duration-300">
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
        <div class="flex flex-wrap gap-5">
            {@render smallCard("bg-blue-500/10 text-blue-500", stats.megamodelAnalysisStats.totalRawArtefacts, "Raw artefacts", "excluding configuration files")}
            {@render smallCard("bg-green-500/10 text-green-500", stats.totalMega, "Recovered artefacts")}
            {@render smallCard("bg-purple-500/10 text-purple-500", `${stats.totalCompletion.toFixed(2)}%`, "Recovery completion")}
        </div>

        <div class="flex flex-col gap-6">
            <StatChart title="Crawled Artefacts" barColor="var(--color-blue-500)" data={stats.raw.artefactTypeCount} />
            
            <!-- Other charts
                <div class="flex gap-6 flex-col lg:flex-row">
                    <StatChart class="flex-1" title="Megamodel artefacts" barColor="var(--color-green-500)" data={stats.mega.artefactTypeCount} />
                    <StatChart class="flex-1" title="Recovered artefacts" barColor="var(--color-purple-500)" data={stats.artefactRecoveryCompletion} />
                </div>
            -->

            <div class="flex gap-6 flex-col lg:flex-row">
                <DataTable title="Artefact Statistics" class="flex-2" data={stats.megamodelAnalysisStats.artefactStats} columns={artefactStatsColumns} />
                <div class="flex flex-col gap-6 flex-1">
                    <HorizontalDataTable title="Megamodel Metrics" data={[stats.megamodelAnalysisStats.graphStats]} columns={megamodelMetricsColumns} />
                    <HorizontalDataTable title="Project Metrics" data={[stats.megamodelAnalysisStats.projectStats]} columns={projectMetricsColumns} />
                </div>
            </div>
        </div>
    </div>
{/if}
