<script lang="ts">
	import Sigma from 'sigma';
	import type { EdgeDisplayData, NodeDisplayData } from 'sigma/types';
	import type Graph from 'graphology';
	import type GraphDTO from '$lib/dto/Graph';
	import UndirectedGraph from 'graphology';

	import FA2Layout from 'graphology-layout-forceatlas2/worker';
	import forceAtlas2 from 'graphology-layout-forceatlas2';
	import random from 'graphology-layout/random';

	import { untrack } from 'svelte';
	import { edgeTypes } from '$lib/constants/edgeTypes';
	import type ArtefactType from '$lib/dto/ArtefactType';
	import type { Edge, Node } from '$lib/dto/Graph';

	interface GraphVisualizerProps {
		types: ArtefactType[];
		document: GraphDTO;
	}

	let { types, document }: GraphVisualizerProps = $props();

	// ── Graph state ──────────────────────────────────────────
	let container: HTMLDivElement;
	let graph: Graph;
	let currentNode = $state<Node | null>(null);
	let currentEdge = $state<(Edge & { key: string }) | null>(null);
	let hoveredEdge: string | null = null;
	let renderer: Sigma | null = null;
	let fa2: InstanceType<typeof FA2Layout> | null = null;
	let fa2Running = $state(false);
	let numberOfIterations = 20; //$state(20);
	let showUnconnectedNodes = $state(false);

  const colorMap = $derived(types.reduce(
    function (map: Record<string, string>, obj: ArtefactType): Record<string, string> {
      map[obj.type] = obj.color;
      return map;
    },
    {} as Record<string, string>
  ));

  const edgeColorMap = edgeTypes.reduce(
    function (map: Record<string, string>, obj: { type: string; color: string }) {
      map[obj.type] = obj.color;
      return map;
    },
    {} as Record<string, string>
  );

	let checkedTypes = $derived.by<Record<string, boolean>>(() => {
		const result: Record<string, boolean> = {};
		for (let i = 0; i < types.length; i++) {
			result[types[i].type] = true;
		}
		return result;
	});

	let checkedEdgeTypes = $derived.by<Record<string, boolean>>(() => {
		const result: Record<string, boolean> = {};
		for (const et of edgeTypes) {
			result[et.type] = true;
		}
		return result;
	});

	$effect(() => {
		// capture dependencies
		const doc = document;
		const c = container;
		if (doc != undefined && c != undefined) {
			untrack(() => {
				currentNode = null;
				currentEdge = null;
				if (fa2) {
					fa2.kill();
					fa2 = null;
					fa2Running = false;
				}
				if (renderer != null) renderer.kill();
				createNetwork(doc);
			});
		}
	});

	function createNetwork(doc: GraphDTO) {
		graph = new UndirectedGraph();

		doc.nodes.forEach((node: Node) => {
			let type: string, name: string;
			if (node._type == 'artefact') {
				type = node.artefact.type;
				name = node.artefact.name;
			} else if (node._type == 'virtual') {
				type = node.kind == 'duplication' ? node.artefactType : node.kind;
				console.log('Type for virtual: ', type);
				name = node.id;
			} else {
				type = 'error';
				name = 'unknown';
			}
			graph.addNode(node.id, {
				x: 0,
				y: 0,
				impl: node,
				nodeType: type,
				label: name,
				color: colorMap[type] || '#b34f47'
			});
		});

		doc.edges.forEach((edge: Edge) => {
			graph.addEdge(edge.source, edge.target, { edgeTypes: edge.types, size: 2 });
		});

		random.assign(graph);

		renderer = new Sigma(graph, container, {
			hideEdgesOnMove: true,
			renderEdgeLabels: false,
			labelRenderedSizeThreshold: 6
		});
		renderer.on('clickNode', (e) => {
			selectNode(graph.getNodeAttributes(e.node).impl);
		});
		renderer.on('clickEdge', (e) => {
			selectEdge(e.edge);
		});
		renderer.on('enterEdge', (e) => {
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
			e.preventSigmaDefault();
		});
		renderer.setSetting('nodeReducer', (nodeId, data) => {
			const res: Partial<NodeDisplayData> = { ...data };
			if (!checkedTypes[data.nodeType]) res.hidden = true;
			if (!showUnconnectedNodes && graph.degree(nodeId) == 0) res.hidden = true;
			if (currentNode?.id === nodeId) {
				res.highlighted = true;
				res.size = (data.size ?? 5) * 2;
			}
			return res;
		});
		startLayout();

		renderer.setSetting('edgeReducer', (edge, data) => {
			const res: Partial<EdgeDisplayData> = { ...data };
			const srcType = graph.getNodeAttribute(graph.source(edge), 'nodeType');
			const tgtType = graph.getNodeAttribute(graph.target(edge), 'nodeType');
			if (!(checkedTypes[srcType] && checkedTypes[tgtType])) res.hidden = true;
			const edgeTypes = graph.getEdgeAttribute(edge, 'edgeTypes');

			const selectedEdgeType = selectEdgeType(checkedEdgeTypes, edgeTypes);
			//console.log(edge, checkedEdgeTypes, edgeTypes, selectedEdgeType);
			if (!selectedEdgeType) {
        res.hidden = true;
        res.color = '#94a3b8';
      } else {
        res.color = edgeColorMap[selectedEdgeType] ?? '#94a3b8';
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

	function startLayout() {
		if (fa2) {
			fa2.kill();
			fa2 = null;
		}
		const s = forceAtlas2.inferSettings(graph);

		//let iterationOptions = {}
		//if (numberOfIterations > 0)
		//  iterationOptions = { iterations: numberOfIterations }
		//fa2 = new FA2Layout(graph, { settings: s, ...iterationOptions });
		fa2 = new FA2Layout(graph, { settings: s });
		fa2.start();
		fa2Running = true;
		console.log(numberOfIterations);
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
