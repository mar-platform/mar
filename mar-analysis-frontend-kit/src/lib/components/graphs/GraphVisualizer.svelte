<script lang="ts">
    import Sigma from "sigma";
    import type { EdgeDisplayData, NodeDisplayData } from "sigma/types";
    import type Graph from "graphology";
    import type GraphDTO from "$lib/dto/Graph";
    import UndirectedGraph from "graphology";

    import FA2Layout from "graphology-layout-forceatlas2/worker";
    import forceAtlas2 from "graphology-layout-forceatlas2";
    import random from 'graphology-layout/random';

    import { Button } from "$lib/components/ui/button";

    import { untrack } from 'svelte';
	  import { edgeTypes } from "$lib/constants/edgeTypes";
	import type ArtefactType from "$lib/dto/ArtefactType";

    interface GraphVisualizerProps {
      types: ArtefactType[];
      document: GraphDTO;
    }

    let { types, document }: GraphVisualizerProps = $props();

    // ── Graph state ──────────────────────────────────────────
    let container: HTMLDivElement;
    let graph: Graph;
    let currentNode = $state<any>(null);
    let currentEdge = $state<{ key: string; type: string; sourceId: string; targetId: string } | null>(null);
    let hoveredEdge: string | null = null;
    let renderer: Sigma | null = null;
    let fa2: InstanceType<typeof FA2Layout> | null = null;
    let fa2Running = $state(false);
    let numberOfIterations = 20; //$state(20);
    let nodeNameFilter = $state('');
    let showUnconnectedNodes = $state(false);
    let artefactSearch = $state('');
    let projectSearch = $state('');

    const colorMap = types.reduce(function(map: any, obj: any) {
        map[obj.type] = obj.color;
        return map;
    }, {} as Record<string, string>);

    const edgeColorMap = edgeTypes.reduce(function(map: any, obj: any) {
        map[obj.type] = obj.color;
        return map;
    }, {} as Record<string, string>);

    let checkedTypes = $state<Record<string, boolean>>({});
    for (let i = 0; i < types.length; i++) {
      checkedTypes[types[i].type] = true;
    }

    let checkedEdgeTypes = $state<Record<string, boolean>>({});
    for (const et of edgeTypes) {
      checkedEdgeTypes[et.type] = true;
    }

    $effect(() => {
      // capture dependencies
      const doc = document;
      const c = container;
      if (doc != undefined && c != undefined) {
        untrack(() => {
          currentNode = null;
          currentEdge = null;
          if (fa2) { fa2.kill(); fa2 = null; fa2Running = false; }
          if (renderer != null) renderer.kill();
          createNetwork(doc);
        });
      }
    });

    function createNetwork(doc: any) {
      graph = new UndirectedGraph();
      doc.nodes.forEach((node: any) => {
        let type: string, name: string;
        if (node._type == 'artefact') {
            type = node.artefact.type; 
            name = node.artefact.name;
        } else if (node._type == 'virtual') {
            type = node.kind == 'duplication' ? node.artefactType : node.kind;
            console.log("Type for virtual: ", type);
            name = node.id;
        } else {
            type = 'error'; name = 'unknown';
        }
        graph.addNode(node.id, { x: 0, y: 0, impl: node, nodeType: type, label: name, color: colorMap[type] || '#b34f47' });
      });
      doc.edges.forEach((edge: any) => {
        graph.addEdge(edge.source, edge.target, { edgeTypes: edge.types, size: 2 });
      });

      random.assign(graph);

      renderer = new Sigma(graph, container, {
        enableEdgeClickEvents: true,
        enableEdgeHoverEvents: true,
        hideEdgesOnMove: true,
        renderEdgeLabels: false,
        labelRenderedSizeThreshold: 6,
      });
      renderer.on("clickNode", (e) => {
        selectNode(graph.getNodeAttributes(e.node).impl);
      });
      renderer.on("clickEdge", (e) => {
        selectEdge(e.edge);
      });
      renderer.on("enterEdge", (e) => {
        hoveredEdge = e.edge;
        container.style.cursor = 'pointer';
        renderer?.refresh();
      });
      renderer.on("leaveEdge", () => {
        hoveredEdge = null;
        container.style.cursor = '';
        renderer?.refresh();
      });
      renderer.on("doubleClickStage", (e) => {
        e.preventSigmaDefault();
      });
      renderer.setSetting("nodeReducer", (nodeId, data) => {
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

      renderer.setSetting("edgeReducer", (edge, data) => {
        const res: Partial<EdgeDisplayData> = { ...data };
        const srcType = graph.getNodeAttribute(graph.source(edge), "nodeType");
        const tgtType = graph.getNodeAttribute(graph.target(edge), "nodeType");
        if (!(checkedTypes[srcType] && checkedTypes[tgtType])) res.hidden = true;
        const edgeTypes = graph.getEdgeAttribute(edge, 'edgeTypes');
        
        
        const selectedEdgeType = selectEdgeType(checkedEdgeTypes, edgeTypes)
        //console.log(edge, checkedEdgeTypes, edgeTypes, selectedEdgeType);
        if (!selectedEdgeType)
          res.hidden = true;
        
        res.color = edgeColorMap[selectedEdgeType] ?? '#94a3b8';
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

    let pendingRefresh = false;
    function refresh() {
      if (pendingRefresh) return;
      pendingRefresh = true;
      requestAnimationFrame(() => { renderer?.refresh(); pendingRefresh = false; });
    }

    function startLayout() {
      console.log("startLayout");
      if (fa2) { fa2.kill(); fa2 = null; }
      const s = forceAtlas2.inferSettings(graph);
      
      //let iterationOptions = {}
      //if (numberOfIterations > 0) 
      //  iterationOptions = { iterations: numberOfIterations }
      //fa2 = new FA2Layout(graph, { settings: s, ...iterationOptions });
      fa2 = new FA2Layout(graph, { settings: s});
      fa2.start();
      fa2Running = true;
      console.log(numberOfIterations)
      setTimeout(() => stopLayout(), numberOfIterations * 1000);
    }

    function stopLayout() {
      if (fa2) { fa2.stop(); fa2.kill(); fa2 = null; }
      fa2Running = false;
      renderer?.refresh();
    }

    function selectNode(nodeImpl: any) {
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
        targetId: graph.target(edgeKey),
      };
      currentNode = null;
      renderer?.refresh();
    }

    function redoLayout() {
      if (fa2Running) stopLayout(); else startLayout();
    }

    function onLabelThreshold(event: Event) {
      renderer?.setSetting("labelRenderedSizeThreshold", +(<HTMLInputElement>event.target).value);
    }

    function applyNodeFilter(_event: Event) { console.log(renderer); }


    function selectEdgeType(checkedEdgeTypes: Record<string, boolean>, e: string[]) : string | null {      
      return e.find(type => checkedEdgeTypes[type]) || null;
    }
</script>

<div class="flex items-start">
  <div class="flex-1 min-w-0 flex flex-col gap-2 pl-2">

    <!-- Node type filter -->
    <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
      <strong class="text-sm">Types:</strong>
      {#each types as {type}}
        <label style="color: {colorMap[type]}" class="flex items-center gap-1 text-sm cursor-pointer">
          <input type="checkbox" bind:checked={checkedTypes[type]} onchange={() => refresh()}>
          {type}
        </label>
      {/each}
    </div>

    <!-- Edge type legend / filter -->
    <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
      <strong class="text-sm">Edges:</strong>
      {#each edgeTypes as et}
        <label class="flex items-center gap-1 text-sm cursor-pointer" style="color: {et.color}">
          <input type="checkbox" bind:checked={checkedEdgeTypes[et.type]} onchange={() => refresh()}>
          <svg width="18" height="8" aria-hidden="true">
            <line x1="0" y1="4" x2="18" y2="4" stroke={et.color} stroke-width="2.5" />
          </svg>
          {et.label}
        </label>
      {/each}
    </div>

    <!-- Other controls -->
    <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm">
      <label class="flex items-center gap-1 cursor-pointer">
        <input type="checkbox" bind:checked={showUnconnectedNodes} onchange={() => refresh()}>
        Show unconnected
      </label>
      <div class="flex items-center gap-1">
        <span>Layout timeout:</span>
        <input class="w-16 h-7 text-xs px-2 py-0 border rounded" type="number" bind:value={numberOfIterations} />
        <Button size="sm" class="h-7 text-xs" onclick={redoLayout}>{fa2Running ? 'Stop' : 'Layout'}</Button>
      </div>
      <div class="flex items-center gap-1">
        <span>Label size:</span>
        <input class="w-24" type="range" min="0" max="15" step="0.5" oninput={onLabelThreshold} />
      </div>
      <div class="flex items-center gap-1">
        <input class="w-32 h-7 text-xs px-2 py-0 border rounded" type="text" placeholder="Filter nodes…" bind:value={nodeNameFilter} />
        <Button size="sm" class="h-7 text-xs" onclick={applyNodeFilter}>Filter</Button>
      </div>
    </div>

    <!-- Graph canvas -->
    <div
      class="border border-gray-400 resize overflow-auto"
      style="height: 600px"
      bind:this={container}
    ></div>
  </div>

</div>
