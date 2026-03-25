<script lang="ts">
	import Sigma from 'sigma';
	import type { EdgeDisplayData, NodeDisplayData } from 'sigma/types';
	import type Graph from 'graphology';
	import UndirectedGraph from 'graphology';

	import FA2Layout from 'graphology-layout-forceatlas2/worker';
	import forceAtlas2 from 'graphology-layout-forceatlas2';

	import type { Node } from '$lib/dto/Graph';
	import { onMount } from 'svelte';
	import { INITIAL_LABEL_SIZE, INITIAL_LABEL_THRESHOLD, INITIAL_SHOW_UNCONNECTED_NODES } from '$lib/constants/values';

	interface GraphVisualizerProps {
		graph: UndirectedGraph;
		selectedNodeTypes: Record<string, boolean>;
		selectedEdgeTypes: Record<string, boolean>;
	}

	let { graph, selectedNodeTypes, selectedEdgeTypes }: GraphVisualizerProps = $props();

	// ── Graph state ──────────────────────────────────────────
	let container: HTMLDivElement = $state(null!);
	let currentNode = $state<Node | null>(null);
	let currentEdge = $state<{ key: string; type: string; sourceId: string; targetId: string } | null>(null);
	let hoveredEdge: string | null = $state(null);

	let renderer: Sigma | null = null;

	let fa2: InstanceType<typeof FA2Layout> | null = null;
	let fa2Running = $state(false);
	let numberOfIterations = 20; //$state(20);

	onMount(() => {
		console.log('Initializing graph...');
		renderer = initGraph();

		// When unmounting, kill the renderer and the layout to free resources
		return () => {
			if (fa2) {
				fa2.kill();
				fa2 = null;
				fa2Running = false;
			}
			if (renderer) {
				renderer.kill();
				renderer = null;
			}
		};
	})

	export function setLabelSize(size: number) {
		renderer?.setSetting("labelSize", size);
	}

	export function setLabelThreshold(threshold: number) {
		renderer?.setSetting("labelRenderedSizeThreshold", threshold);
	}

	export function setNodeConfig(showUnconnectedNodes: boolean, selectedNodeTypes: Record<string, boolean>) {
		renderer?.setSetting('nodeReducer', (nodeId, data) => {
			const res: Partial<NodeDisplayData> = { ...data };
			if (!selectedNodeTypes[data.nodeType]) res.hidden = true;
			if (!showUnconnectedNodes && graph.degree(nodeId) == 0) res.hidden = true;
			if (currentNode?.id === nodeId) {
				res.highlighted = true;
				res.size = (data.size ?? 5) * 2;
			}
			return res;
		});
	}

	export const setEdgeConfig = (selectedEdgeTypes: Record<string, boolean>) => {
		renderer?.setSetting('edgeReducer', (edge, data) => {
			const res: Partial<EdgeDisplayData> = { ...data };
			const srcType = graph.getNodeAttribute(graph.source(edge), 'nodeType');
			const tgtType = graph.getNodeAttribute(graph.target(edge), 'nodeType');
			if (!(selectedNodeTypes[srcType] && selectedNodeTypes[tgtType])) res.hidden = true;
			const edgeTypes = graph.getEdgeAttribute(edge, 'edgeTypes');

			const selectedEdgeType = selectEdgeType(selectedEdgeTypes, edgeTypes);
			//console.log(edge, selectedEdgeTypes, edgeTypes, selectedEdgeType);
			if (!selectedEdgeType) {
				res.hidden = true;
				res.color = '#94a3b8';
			} else {
				res.color = /*edgeColorMap[selectedEdgeType] ?? */'#94a3b8';
			}

			if (hoveredEdge === edge) {
				res.size = 3;
				res.zIndex = 1;
			}
			if (currentEdge?.key === edge) {
				res.color = '#f97316';
				res.size = 4;
				res.zIndex = 2;
			}
			return res;
		});
	}

	function initGraph() {
		const renderer = startRenderer(graph);

		setLabelSize(INITIAL_LABEL_SIZE);
		setLabelThreshold(INITIAL_LABEL_THRESHOLD);
		setNodeConfig(INITIAL_SHOW_UNCONNECTED_NODES, selectedNodeTypes);
		setEdgeConfig(selectedEdgeTypes);

		startLayout();
		return renderer;
	}

	function startRenderer(graph: Graph) {
		renderer = new Sigma(graph, container, {
			hideEdgesOnMove: true,
			renderEdgeLabels: true,
			labelRenderedSizeThreshold: 6,
		});
		renderer.on('clickNode', (e) => {
			selectNode(graph.getNodeAttributes(e.node).impl);
		});
		renderer.on('clickEdge', (e) => {
			console.log('Clicked edge: ', e.edge);
			selectEdge(e.edge);
		});
		renderer.on('enterEdge', (e) => {
			console.log('Hovering edge: ', e.edge);
			hoveredEdge = e.edge;
			container.style.cursor = 'pointer';
			renderer?.refresh();
		});
		renderer.on('leaveEdge', () => {
			hoveredEdge = null;
			container.style.cursor = '';
			renderer?.refresh();
		});
		renderer.on('doubleClickStage', (e) => {
			e.preventSigmaDefault(); // We dont want to zoom on double click
		});

		return renderer;
	}

	function startLayout() {
		if (fa2) {
			fa2.kill();
			fa2 = null;
		}
		const settings = forceAtlas2.inferSettings(graph);

		fa2 = new FA2Layout(graph, { settings });
		fa2.start();
		fa2Running = true;
		setTimeout(() => stopLayout(), numberOfIterations * 1000);
	}

	function stopLayout() {
		if (fa2) {
			fa2.stop();
			fa2.kill();
			fa2 = null;
		}
		fa2Running = false;
		renderer?.refresh();
	}

	function selectNode(nodeImpl: Node) {
		currentNode = nodeImpl;
		currentEdge = null;
		renderer?.refresh();
	}

	export function selectNodeById(id: string) {
		if (graph?.hasNode(id)) {
			selectNode(graph.getNodeAttributes(id).impl);
		}
	}

	function selectEdge(edgeKey: string) {
		currentEdge = {
			key: edgeKey,
			type: graph.getEdgeAttribute(edgeKey, 'edgeType'),
			sourceId: graph.source(edgeKey),
			targetId: graph.target(edgeKey)
		};
		currentNode = null;
		renderer?.refresh();
	}

	function selectEdgeType(checkedEdgeTypes: Record<string, boolean>, e: string[]): string | null {
		return e.find((type) => checkedEdgeTypes[type]) || null;
	}
</script>

<div class="h-full w-full cursor-grab active:cursor-grabbing" bind:this={container}></div>
