<script lang="ts">
    import * as Chart from "$lib/components/ui/chart/index.js";
    import { BarChart } from "layerchart";
    import { scaleBand } from "d3-scale";
	import { cn } from "$lib/utils";

  export interface StatChartProps {
    title: string;
    data: Record<string, number>;
    barColor: string;
    class?: string;
  }

  let { title, data, barColor, class: className }: StatChartProps = $props();

  const chartData = $derived(Object.entries(data)
        .map(([x, y]) => ({
            x,
            y,
            fill: barColor
        }))
        .sort((a, b) => b.y - a.y)
    );
 
  const chartConfig = {
        count: {
            label: "Count",
        },
    } satisfies Chart.ChartConfig;
</script>

{#if chartData.length > 0}
    <div class={cn("flex flex-col gap-1.5", className)}>
        <h2 class="text-xl font-medium">{title}</h2>
        <div class="p-6 w-full bg-page-foreground rounded-lg shadow-sm">
            <Chart.Container config={chartConfig} class="pl-5 h-50 w-full">
                <BarChart 
                    data={chartData} 
                    x="x" 
                    y="y" 
                    c="x"
                    cRange={chartData.map(d => d.fill)}
                    cDomain={chartData.map(d => d.x)}
                    xScale={scaleBand().padding(0.30)}
                    props={{ 
                        bars: { 
                            radius: 4,
                            stroke: 'none'
                        },
                        xAxis: {
                            format: (d) => d.slice(0, 3),
                        },
                    }}
                    series={[
                        {
                            key: "y",
                            label: chartConfig.count.label,
                        },
                    ]}
                >
                    {#snippet tooltip()}
                        <Chart.Tooltip />
                    {/snippet}
                    
                </BarChart>
            </Chart.Container>
        </div>
    </div>
{/if}
