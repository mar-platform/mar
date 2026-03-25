<script lang="ts">
	import { edgeTypes } from "$lib/constants/edgeTypes";
	import { nodeTypes } from "$lib/constants/graphNodeTypes";
    import LucideCheck from '@lucide/svelte/icons/check';
	import { fade } from "svelte/transition";
    import { Checkbox } from "$lib/components/ui/checkbox/index.js";
    import { Label } from "$lib/components/ui/label/index.js";
    import { Slider } from "$lib/components/ui/slider/index.js";
	import { INITIAL_LABEL_SIZE, INITIAL_LABEL_THRESHOLD, INITIAL_NODE_SIZE, INITIAL_SHOW_UNCONNECTED_NODES } from "$lib/constants/values";
    
    interface GraphToolbarProps {
        selectedNodeTypes: Record<keyof typeof nodeTypes, boolean>;
        selectedEdgeTypes: Record<keyof typeof edgeTypes, boolean>;
        onLabelSizeChange: (size: number) => void;
        onLabelThresholdChange: (threshold: number) => void;
        handleNodesChange: (nodeSize: number, showConnectedNodes: boolean) => void;
    }

    let {
        selectedNodeTypes = $bindable(),
        selectedEdgeTypes = $bindable(),
        onLabelSizeChange,
        onLabelThresholdChange,
        handleNodesChange,
    }: GraphToolbarProps = $props();

    let showUnconnectedNodes = $state(INITIAL_SHOW_UNCONNECTED_NODES);
    let labelSize = $state(INITIAL_LABEL_SIZE);
    let labelThreshold = $state(INITIAL_LABEL_THRESHOLD);
    let nodeSize = $state(INITIAL_NODE_SIZE);

    function toggleVisibility(type: 'node' | 'edge', name: string) {
        if (type === 'node') {
            toggleNodeType(name as keyof typeof nodeTypes)
        } else {
            toggleEdgeType(name as keyof typeof edgeTypes);
        }
    }

    function toggleNodeType(type: keyof typeof nodeTypes) {
        selectedNodeTypes[type] = !selectedNodeTypes[type];
    }

    function toggleEdgeType(type: keyof typeof edgeTypes) {
        selectedEdgeTypes[type] = !selectedEdgeTypes[type];
    }
</script>

{#snippet filterItem(type: 'node' | 'edge', name: string, checked: boolean, color: string)}
    <button 
        onclick={() => toggleVisibility(type, name)}
        class="select-none text-xs font-semibold flex items-center gap-2 px-1 py-0.5 cursor-pointer active:scale-95 transition-transform rounded-full"
        style={`background-color: color-mix(in srgb, ${color}, transparent 85%); color: ${color}; border: 1px solid color-mix(in srgb, ${color}, transparent 60%);`}
    >
        {#if type === 'node'}
            <div class="size-3 rounded-full" style={`background-color: ${color};`}></div>
        {:else}
            <svg class="size-2.5" style={`color: ${color};`} viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M0 16L16 0" stroke="currentColor" stroke-width="2.5" />
            </svg>
        {/if}
        {name}
        {#key checked}
            <div class="w-3" in:fade>
                {#if checked}
                    <LucideCheck class="size-3" strokeWidth={3} color={color} />
                {/if}
            </div>    
        {/key}
    </button>
{/snippet}

<div class="flex flex-col gap-6">
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Nodes</span>
        <div class="flex flex-wrap gap-2">
            {#each Object.entries(selectedNodeTypes) as [name, checked](name)}
                {@render filterItem('node', name, checked, `var(${nodeTypes[name as keyof typeof nodeTypes].color})`)}
            {/each}
        </div>
    </div>
    
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Edges</span>
        <div class="flex flex-wrap gap-2">
            {#each Object.entries(selectedEdgeTypes) as [name, checked](name)}
                {@render filterItem('edge', name, checked, `var(${edgeTypes[name as keyof typeof edgeTypes].color})`)}
            {/each}
        </div>
    </div>
    
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Advanced Options</span>
        <div class="flex flex-wrap gap-10">
            <div class="flex items-center gap-3">
                <Checkbox id="unconnected-nodes" bind:checked={showUnconnectedNodes} onCheckedChange={(checked) => handleNodesChange(nodeSize, checked)} />
                <Label for="unconnected-nodes">Unconnected nodes</Label>
            </div>

            <div class="flex flex-col gap-3">
                <Label for="node-size">Node size</Label>
                <Slider id="node-size" type="single" onValueCommit={(value) => handleNodesChange(value, showUnconnectedNodes)} bind:value={nodeSize} min={1} max={30} step={1} class="w-50" />
            </div>
            
            <div class="flex flex-col gap-3">
                <Label for="label-size">Label size</Label>
                <Slider id="label-size" type="single" onValueCommit={(value) => onLabelSizeChange(value)} bind:value={labelSize} min={10} max={32} step={0.5} class="w-50" />
            </div>

            <div class="flex flex-col gap-3">
                <Label for="label-threshold">Label visibility threshold</Label>
                <Slider id="label-threshold" type="single" onValueCommit={(value) => onLabelThresholdChange(value)} bind:value={labelThreshold} min={0} max={15} step={0.5} class="w-50" />
            </div>
        </div>
    </div>
</div>
