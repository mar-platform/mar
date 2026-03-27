<script lang="ts">
	import Sigma from 'sigma';
	import type { EdgeDisplayData, NodeDisplayData } from 'sigma/types';
	import type Graph from 'graphology';
	import UndirectedGraph from 'graphology';

	import FA2Layout from 'graphology-layout-forceatlas2/worker';
	import forceAtlas2 from 'graphology-layout-forceatlas2';

	import type { Edge, EdgeType, Node } from '$lib/dto/Graph';
	import { onMount } from 'svelte';
	import { edgeTypes } from '$lib/constants/edgeTypes';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
	import { globalState } from '$lib/stores/globalState.svelte';
	import { toast } from 'svelte-sonner';

	interface GraphVisualizerProps {
		graph: UndirectedGraph;
		fa2Running: boolean;
	}

	let { graph, fa2Running = $bindable(false) }: GraphVisualizerProps = $props();

	// ── Graph state ──────────────────────────────────────────
	let container: HTMLDivElement = $state(null!);
	let currentNode = $derived<Node | null>(globalState.selectedNode);
	let currentEdge = $derived<Edge | null>(globalState.selectedEdge);
	let hoveredEdge: string | null = $state(null);

	const nodeFilter = $derived(globalState.nodeFilter);


	let fa2: InstanceType<typeof FA2Layout> | null = null;

	onMount(() => {
		initGraph();

		// When unmounting, kill the renderer and the layout to free resources
		return () => {
			if (fa2) {
				fa2.kill();
				fa2 = null;
				fa2Running = false;
			}
			if (globalState.renderer) {
				globalState.renderer.kill();
				globalState.renderer = null;
			}
		};
	})

	export function updateLabelSize() {
		globalState.renderer?.setSetting("labelSize", globalState.labelSize);
	}

	export function updateLabelThreshold() {
		globalState.renderer?.setSetting("labelRenderedSizeThreshold", globalState.labelThreshold);
	}

	function setNodeConfig() {
		globalState.renderer?.setSetting('nodeReducer', (nodeId, data) => {
			const res: Partial<NodeDisplayData> = { ...data };
			// Node visibility logic
			if (nodeFilter !== '' && nodeId.toLowerCase()?.includes(nodeFilter.toLowerCase()) === false) {
				res.hidden = true;
			} else if (!globalState.selectedNodeTypes[data.nodeType as keyof typeof nodeTypes]) {
				res.hidden = true;
			} else if (!globalState.showUnconnectedNodes && graph.degree(nodeId) == 0) {
				res.hidden = true;
			}

			// Highlight the selected node
			if (currentNode?.id === nodeId) {
				res.highlighted = true;
				res.size = globalState.nodeSize * 1.35;
			} else {
				res.size = globalState.nodeSize;
			}

			// Set the color
			const color = getComputedStyle(document.documentElement).getPropertyValue(nodeTypes[data.nodeType as keyof typeof nodeTypes]?.color || nodeTypes['error'].color); // Needed to calculate the value of the css variable
			res.color = color;

			return res;
		});
	}

	const setEdgeConfig = () => {
		globalState.renderer?.setSetting('edgeReducer', (edge, data) => {
			const res: Partial<EdgeDisplayData> = { ...data };
			const srcType = graph.getNodeAttribute(graph.source(edge), 'nodeType');
			const tgtType = graph.getNodeAttribute(graph.target(edge), 'nodeType');
			
			if (!(globalState.selectedNodeTypes[srcType as keyof typeof nodeTypes] && globalState.selectedNodeTypes[tgtType as keyof typeof nodeTypes])) res.hidden = true;
			const currentEdgeTypes = graph.getEdgeAttribute(edge, 'edgeTypes');

			const selectedEdgeType = selectEdgeType(globalState.selectedEdgeTypes, currentEdgeTypes);

			// Set the color
			if (selectedEdgeType === null) {
				res.hidden = true;
				res.color = 'transparent';
			} else {
				const color = getComputedStyle(document.documentElement).getPropertyValue(edgeTypes[selectedEdgeType].color); // Needed to calculate the value of the css variable
				res.color = color;
			}

			if (hoveredEdge === edge) {
				res.size = 3;
				res.zIndex = 1;
			}
			if (currentEdge?.key === edge) {
				res.size = 4;
				res.zIndex = 2;
			}
			return res;
		});
	}

	function initGraph() {
		startRenderer(graph);
		updateLabelSize();
		updateLabelThreshold();
		setNodeConfig();
		setEdgeConfig();

		startLayout();
	}

	function startRenderer(graph: Graph) {
		globalState.renderer = new Sigma(graph, container, {
			hideEdgesOnMove: true,
			renderEdgeLabels: true,
			enableEdgeEvents: true,
			labelRenderedSizeThreshold: 6,
		});
		globalState.renderer.on('enterNode', () => {
			container.style.cursor = 'pointer';
		});
		globalState.renderer.on('leaveNode', () => {
			container.style.cursor = '';
		});
		globalState.renderer.on('clickNode', (e) => {
			selectNode(graph.getNodeAttributes(e.node).impl);
		});
		globalState.renderer.on('clickEdge', (e) => {
			selectEdge(e.edge);
		});
		globalState.renderer.on('enterEdge', (e) => {
			hoveredEdge = e.edge;
			container.style.cursor = 'pointer';
			globalState.refreshGraph();
		});
		globalState.renderer.on('leaveEdge', () => {
			hoveredEdge = null;
			container.style.cursor = '';
			globalState.refreshGraph();
		});
		globalState.renderer.on('doubleClickStage', (e) => {
			e.preventSigmaDefault(); // We dont want to zoom on double click
		});
	}

	export function startLayout() {
		if (fa2) {
			fa2.kill();
			fa2 = null;
		}
		if (globalState.numberOfIterations === undefined || globalState.numberOfIterations < 1) {
			toast.error('Please enter a valid number of iterations');
			return;
		}
		const settings = forceAtlas2.inferSettings(graph);

		fa2 = new FA2Layout(graph, { settings });
		fa2.start();
		fa2Running = true;
		setTimeout(() => stopLayout(), globalState.numberOfIterations * 1000);
	}

	export function stopLayout() {
		if (fa2) {
			fa2.stop();
			fa2.kill();
			fa2 = null;
		}
		fa2Running = false;
		globalState.refreshGraph();
	}

	function selectNode(node: Node) {
		currentEdge = null;
		globalState.selectNode(node);
	}

	function selectEdge(edgeKey: string) {
		currentNode = null;
		globalState.selectEdge({
			key: edgeKey,
			types: graph.getEdgeAttribute(edgeKey, 'edgeTypes'),
			source: graph.source(edgeKey),
			target: graph.target(edgeKey)
		});
	}

	function selectEdgeType(checkedEdgeTypes: Record<string, boolean>, e: string[]): EdgeType | null {
		return e.find((type) => checkedEdgeTypes[type]) as EdgeType || null;
	}
</script>

<div class="h-full w-full cursor-grab active:cursor-grabbing" bind:this={container}></div>
