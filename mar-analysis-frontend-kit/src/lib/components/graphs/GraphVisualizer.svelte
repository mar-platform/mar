<script lang="ts">
	import Sigma from 'sigma';
	import type { EdgeDisplayData, NodeDisplayData, PlainObject } from 'sigma/types';
	import type Graph from 'graphology';
	import UndirectedGraph from 'graphology';

	import FA2Layout from 'graphology-layout-forceatlas2/worker';
	import forceAtlas2 from 'graphology-layout-forceatlas2';

	import type { Edge, EdgeType, Node } from '$lib/dto/Graph';
	import { onMount } from 'svelte';
	import { edgeTypes } from '$lib/constants/edgeTypes';
	import { nodeTypes } from '$lib/constants/graphNodeTypes';
	import { globalState } from '$lib/stores/globalState.svelte';
	import { NodePointProgram, EdgeLineProgram } from "sigma/rendering";
	import { mode } from 'mode-watcher';

	interface NodeData extends NodeDisplayData {
		labelColor: string;
		hoverBgColor: string;
	}

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
			const res: Partial<NodeData> = { ...data };
			res.labelColor = getComputedStyle(document.documentElement).getPropertyValue("--color-text-primary");
			res.hoverBgColor = getComputedStyle(document.documentElement).getPropertyValue("--color-page-background");

			// Node visibility logic
			if (globalState.nodeFilter !== '' && nodeId.toLowerCase()?.includes(globalState.nodeFilter.toLowerCase()) === false) {
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
				res.color = mode.current === 'dark' ? 'white' : 'black';
			}
			if (currentEdge?.key === edge) {
				res.size = 4;
				res.zIndex = 2;
				res.color = mode.current === 'dark' ? 'white' : 'black';
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

		startLayout(globalState.numberOfSeconds);
	}

	/** Controls the label rendered when hovering over a node */
	const customHoverRenderer = (
		context: CanvasRenderingContext2D,
		data: PlainObject,
		settings: PlainObject
	): void => {
		const size = settings.labelSize as number;
		const font = settings.labelFont as string;
		const weight = settings.labelWeight as string;
		const label = data.label as string;

		if (!label) return;

		// Properties of the label
		context.font = `${weight} ${size}px ${font}`;
		const textWidth = context.measureText(label).width;
		
		// Coordinates of the node
		const nodeX = data.x as number;
		const nodeY = data.y as number;
		const nodeSize = data.size as number;

		const padding = 8;

		const boxX = nodeX - nodeSize - padding;
    	const boxWidth = (nodeSize * 2) + textWidth + (padding * 3);
    
		const boxHeight = Math.max(size + 10, (nodeSize * 2) + padding);
		const boxY = nodeY - (boxHeight / 2);

		// Fill background
		context.fillStyle = (data.hoverBgColor as string);
		context.beginPath();
		context.roundRect(boxX, boxY, boxWidth, boxHeight, boxHeight / 2);
		context.fill();

		// Paint the node on top of the label background
		context.fillStyle = (data.color as string);
		context.beginPath();
		context.arc(nodeX, nodeY, nodeSize, 0, Math.PI * 2);
		context.fill();

		// Text
		context.fillStyle = (data.labelColor as string);
		const textX = nodeX + nodeSize + 8;
    	context.fillText(label, textX, nodeY + size / 3);
	};

	function startRenderer(graph: Graph) {
		globalState.renderer = new Sigma(graph, container, {
			hideEdgesOnMove: true,
			renderEdgeLabels: true,
			enableEdgeEvents: true,
			defaultDrawNodeHover: customHoverRenderer,
			labelRenderedSizeThreshold: 6,
			labelFont: "Noto Sans, sans-serif",
			labelColor: {
				attribute: "labelColor",
				color: "red"
			},

			// Use most efficient programs for nodes and edges
			nodeProgramClasses: {
				point: NodePointProgram
			},
			edgeProgramClasses: {
				line: EdgeLineProgram
			},
			defaultNodeType: "point",
  			defaultEdgeType: "line",
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
			globalState.refreshGraph();
			container.style.cursor = 'pointer';
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

	export function startLayout(numberOfIterations: number | undefined) {
		if (!numberOfIterations) {
			return;
		}
		if (fa2) {
			fa2.kill();
			fa2 = null;
		}
		const settings = forceAtlas2.inferSettings(graph);

		fa2 = new FA2Layout(graph, { settings: { ...settings, barnesHutOptimize: true } });
		fa2.start();
		fa2Running = true;
		setTimeout(() => stopLayout(), numberOfIterations * 1000);
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

<div class="flex-1 cursor-grab active:cursor-grabbing" bind:this={container}></div>
