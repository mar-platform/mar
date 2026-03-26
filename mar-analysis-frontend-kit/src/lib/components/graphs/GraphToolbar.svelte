<script lang="ts">
	import { edgeTypes } from "$lib/constants/edgeTypes";
	import { nodeTypes } from "$lib/constants/graphNodeTypes";
    import { Checkbox } from "$lib/components/ui/checkbox/index.js";
    import { Label } from "$lib/components/ui/label/index.js";
    import { Slider } from "$lib/components/ui/slider/index.js";
	import { INITIAL_LABEL_SIZE, INITIAL_LABEL_THRESHOLD, INITIAL_NODE_SIZE, INITIAL_SHOW_UNCONNECTED_NODES } from "$lib/constants/values";
	import NodeOrEdgeItem from "./NodeOrEdgeItem.svelte";
    
    interface GraphToolbarProps {
        selectedNodeTypes: Record<keyof typeof nodeTypes, boolean>;
        selectedEdgeTypes: Record<keyof typeof edgeTypes, boolean>;
        labelThreshold: number;
        onLabelThresholdChange: (threshold: number) => void;
        nodeSize: number;
        onNodeSizeChange: (nodeSize: number) => void;
        showUnconnectedNodes: boolean;
        onShowUnconnectedNodesChange: (show: boolean) => void;
        labelSize: number;
        onLabelSizeChange: (size: number) => void;
    }

    let {
        selectedNodeTypes = $bindable(),
        selectedEdgeTypes = $bindable(),
        onLabelSizeChange,
        onLabelThresholdChange,
        nodeSize = $bindable(INITIAL_NODE_SIZE),
        onNodeSizeChange,
        showUnconnectedNodes = $bindable(INITIAL_SHOW_UNCONNECTED_NODES),
        onShowUnconnectedNodesChange,
        labelSize = $bindable(INITIAL_LABEL_SIZE),
        labelThreshold = $bindable(INITIAL_LABEL_THRESHOLD),
    }: GraphToolbarProps = $props();

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

<div class="flex flex-col gap-6">
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Nodes</span>
        <div class="flex flex-wrap gap-2">
            {#each Object.entries(selectedNodeTypes) as [name, checked](name)}
                <NodeOrEdgeItem type="node" {name} {checked} color={`var(${nodeTypes[name as keyof typeof nodeTypes].color})`} onClick={() => toggleVisibility('node', name)} />
            {/each}
        </div>
    </div>
    
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Edges</span>
        <div class="flex flex-wrap gap-2">
            {#each Object.entries(selectedEdgeTypes) as [name, checked](name)}
                <NodeOrEdgeItem type="edge" {name} {checked} color={`var(${edgeTypes[name as keyof typeof edgeTypes].color})`} onClick={() => toggleVisibility('edge', name)} />
            {/each}
        </div>
    </div>
    
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Advanced Options</span>
        <div class="flex flex-wrap gap-10">
            <div class="flex items-center gap-3">
                <Checkbox id="unconnected-nodes" bind:checked={showUnconnectedNodes} onCheckedChange={(checked) => onShowUnconnectedNodesChange(checked)} />
                <Label for="unconnected-nodes">Unconnected nodes</Label>
            </div>

            <div class="flex flex-col gap-3">
                <Label for="node-size">Node size</Label>
                <Slider id="node-size" type="single" onValueCommit={(value) => onNodeSizeChange(value)} bind:value={nodeSize} min={1} max={30} step={1} class="w-50" />
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
