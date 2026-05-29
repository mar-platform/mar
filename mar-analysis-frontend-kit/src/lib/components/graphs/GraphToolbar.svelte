<script lang="ts">
	import { edgeTypes } from "$lib/constants/edgeTypes";
	import { nodeTypes } from "$lib/constants/graphNodeTypes";
    import { Checkbox } from "$lib/components/ui/checkbox/index.js";
    import { Label } from "$lib/components/ui/label/index.js";
    import { Slider } from "$lib/components/ui/slider/index.js";
	import NodeOrEdgeItem from "./NodeOrEdgeItem.svelte";
	import { globalState } from "$lib/stores/globalState.svelte";

    interface GraphToolbarProps {
        onLabelThresholdChange: () => void;
        onLabelSizeChange: () => void;
    }

    let {
        onLabelSizeChange,
        onLabelThresholdChange,
    }: GraphToolbarProps = $props();

    function toggleVisibility(type: 'node' | 'edge', name: string) {
        if (type === 'node') {
            toggleNodeType(name as keyof typeof nodeTypes)
        } else {
            toggleEdgeType(name as keyof typeof edgeTypes);
        }
    }

    function toggleNodeType(type: keyof typeof nodeTypes) {
        globalState.selectedNodeTypes[type] = !globalState.selectedNodeTypes[type];
        globalState.refreshGraph();
    }

    function toggleEdgeType(type: keyof typeof edgeTypes) {
        globalState.selectedEdgeTypes[type] = !globalState.selectedEdgeTypes[type];
        globalState.refreshGraph();
    }

    function handleUnconnectedNodesChange(checked: boolean) {
        globalState.showUnconnectedNodes = checked;
        globalState.refreshGraph();
    }

    function handleNodeSizeChange(value: number) {
        globalState.nodeSize = value;
        globalState.refreshGraph();
    }

    function handleLabelSizeChange(value: number) {
        globalState.labelSize = value;
        onLabelSizeChange();
        globalState.refreshGraph();
    }

    function handleLabelThresholdChange(value: number) {
        globalState.labelThreshold = value;
        onLabelThresholdChange();
        globalState.refreshGraph();
    }

</script>

<div class="flex flex-col gap-6">
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Nodes</span>
        <div class="flex flex-wrap gap-2">
            {#each Object.entries(globalState.selectedNodeTypes) as [name, checked](name)}
                <NodeOrEdgeItem type="node" {name} {checked} color={`var(${nodeTypes[name as keyof typeof nodeTypes].color})`} onClick={() => toggleVisibility('node', name)} />
            {/each}
        </div>
    </div>
    
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Edges</span>
        <div class="flex flex-wrap gap-2">
            {#each Object.entries(globalState.selectedEdgeTypes) as [name, checked](name)}
                <NodeOrEdgeItem type="edge" {name} {checked} color={`var(${edgeTypes[name as keyof typeof edgeTypes].color})`} onClick={() => toggleVisibility('edge', name)} />
            {/each}
        </div>
    </div>
    
    <div class="flex flex-col gap-2">
        <span class="text-text-secondary text-sm font-medium">Advanced Options</span>
        <div class="flex flex-wrap gap-10">
            <div class="flex items-center gap-3">
                <Checkbox id="unconnected-nodes" checked={globalState.showUnconnectedNodes} onCheckedChange={(checked) => handleUnconnectedNodesChange(checked)} />
                <Label for="unconnected-nodes">Unconnected nodes</Label>
            </div>

            <div class="flex flex-col gap-3">
                <Label for="node-size">Node size</Label>
                <Slider id="node-size" type="single" onValueCommit={(value) => handleNodeSizeChange(value)} value={globalState.nodeSize} min={1} max={30} step={1} class="w-50" />
            </div>
            
            <div class="flex flex-col gap-3">
                <Label for="label-size">Label size</Label>
                <Slider id="label-size" type="single" onValueCommit={(value) => handleLabelSizeChange(value)} value={globalState.labelSize} min={10} max={32} step={0.5} class="w-50" />
            </div>

            <div class="flex flex-col gap-3">
                <Label for="label-threshold">Label visibility threshold</Label>
                <Slider id="label-threshold" type="single" onValueCommit={(value) => handleLabelThresholdChange(value)} value={globalState.labelThreshold} min={0} max={15} step={0.5} class="w-50" />
            </div>
        </div>
    </div>
</div>
