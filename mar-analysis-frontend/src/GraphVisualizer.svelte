<script lang="ts">
    import Sigma from "sigma";
    import type { EdgeDisplayData, NodeDisplayData } from "sigma/types";
    import type Graph from "graphology";
    import UndirectedGraph from "graphology";

    import FA2Layout from "graphology-layout-forceatlas2/worker";
    import forceAtlas2 from "graphology-layout-forceatlas2";
    import random from 'graphology-layout/random';

    import ArtifactInfo from './ArtefactInfo.svelte'
    import EdgeInfo from './EdgeInfo.svelte'
    import { edgeTypes } from './GraphEdgeTypes.js'
    import { Accordion, AccordionItem } from "$lib/components/ui/accordion";
    import { Button } from "$lib/components/ui/button";
    import { Input } from "$lib/components/ui/input";

    let { types, document, children }: {
      types: any;
      document: any;
      children?: import('svelte').Snippet;
    } = $props();

    // ── Graph state ──────────────────────────────────────────
    let container: HTMLDivElement;
    let graph: Graph;
    let currentNode = $state<any>(null);
    let currentEdge = $state<{ key: string; type: string; sourceId: string; targetId: string } | null>(null);
    let hoveredEdge: string | null = null;
    let renderer: Sigma | null = null;
    let numberOfIterations = $state(20);
    let nodeNameFilter = $state('');
    let showUnconnectedNodes = $state(false);
    let artefactSearch = $state('');

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
      if (document != undefined && container != undefined) {
        currentNode = null;
        currentEdge = null;
        if (renderer != null) renderer.kill();
        createNetwork(document);
      }
    });

    function createNetwork(doc: any) {
      graph = new UndirectedGraph();
      doc.nodes.forEach((node: any) => {
        let type: string, name: string;
        if (node._type == 'artefact') {
            type = node.artefact.type; name = node.artefact.name;
        } else if (node._type == 'virtual') {
            type = node.kind; name = node.id;
        } else {
            type = 'error'; name = 'unknown';
        }
        graph.addNode(node.id, { x: 0, y: 0, impl: node, nodeType: type, label: name, color: colorMap[type] || '#b34f47' });
      });
      doc.edges.forEach((edge: any) => {
        graph.addEdge(edge.source, edge.target, { edgeTypes: edge.types, size: 2 });
      });

      random.assign(graph);
      const sensibleSettings = forceAtlas2.inferSettings(graph);
      forceAtlas2.assign(graph, { iterations: numberOfIterations, settings: sensibleSettings });

      renderer = new Sigma(graph, container, { enableEdgeClickEvents: true, enableEdgeHoverEvents: true });
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

    function refresh() { renderer?.refresh(); }

    function selectNode(nodeImpl: any) {
      currentNode = nodeImpl;
      currentEdge = null;
      renderer?.refresh();
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
      const s = forceAtlas2.inferSettings(graph);
      forceAtlas2.assign(graph, { iterations: numberOfIterations, settings: s });
      refresh();
    }

    function onLabelThreshold(event: Event) {
      renderer?.setSetting("labelRenderedSizeThreshold", +(<HTMLInputElement>event.target).value);
    }

    function applyNodeFilter(_event: Event) { console.log(renderer); }

    function getArtefactNodes(nodes: any[]) {
      const q = artefactSearch.trim().toLowerCase();
      let matches: (name: string) => boolean;
      if (!q) {
        matches = () => true;
      } else if (q.includes('*') || q.includes('?')) {
        const pattern = q.replace(/[.+^${}()|[\]\\]/g, '\\$&')
                         .replace(/\*/g, '.*')
                         .replace(/\?/g, '.');
        const re = new RegExp(`^${pattern}$`);
        matches = (name: string) => re.test(name.toLowerCase());
      } else {
        matches = (name: string) => name.toLowerCase().includes(q);
      }
      return nodes
        .filter((n: any) => n._type == 'artefact')
        .filter((n: any) => checkedTypes[n.artefact.type])
        .filter((n: any) => matches(n.artefact.name));
    }

    // ── Resize logic ─────────────────────────────────────────
    let containerEl: HTMLDivElement;
    let sidebarWidth = $state(256);
    let dragging = $state(false);
    let rightPanelWidth = $state(280);
    let draggingRight = $state(false);

    $effect(() => {
      if (!dragging) return;

      const onMove = (e: MouseEvent) => {
        const rect = containerEl.getBoundingClientRect();
        sidebarWidth = Math.max(150, Math.min(600, e.clientX - rect.left));
      };
      const onUp = () => { dragging = false; };

      window.addEventListener('mousemove', onMove);
      window.addEventListener('mouseup', onUp);
      window.document.body.style.cursor = 'col-resize';
      window.document.body.style.userSelect = 'none';

      return () => {
        window.removeEventListener('mousemove', onMove);
        window.removeEventListener('mouseup', onUp);
        window.document.body.style.cursor = '';
        window.document.body.style.userSelect = '';
      };
    });

    $effect(() => {
      if (!draggingRight) return;

      const onMove = (e: MouseEvent) => {
        const rect = containerEl.getBoundingClientRect();
        rightPanelWidth = Math.max(150, Math.min(600, rect.right - e.clientX));
      };
      const onUp = () => { draggingRight = false; };

      window.addEventListener('mousemove', onMove);
      window.addEventListener('mouseup', onUp);
      window.document.body.style.cursor = 'col-resize';
      window.document.body.style.userSelect = 'none';

      return () => {
        window.removeEventListener('mousemove', onMove);
        window.removeEventListener('mouseup', onUp);
        window.document.body.style.cursor = '';
        window.document.body.style.userSelect = '';
      };
    });


    function selectEdgeType(checkedEdgeTypes: Record<string, boolean>, e: string[]) : string | null {      
      return e.find(type => checkedEdgeTypes[type]) || null;
    }
</script>

<div class="flex items-start" bind:this={containerEl}>

  <!-- ── Left sidebar ── -->
  <aside
    class="shrink-0 flex flex-col gap-2 overflow-y-auto max-h-[680px]"
    style="width: {sidebarWidth}px"
  >
    {#if children}
      {@render children()}
    {/if}

    <div class="mt-auto pt-2 border-t">
      <Accordion>
        <AccordionItem header="All artefacts">
          <div class="pb-1">
            <input class="h-6 text-xs px-2 py-0 w-full rounded-md border border-input bg-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" type="text" placeholder="Search…" bind:value={artefactSearch} />
          </div>
          <ul class="text-sm space-y-0.5">
            {#each getArtefactNodes(document.nodes) as node}
              <li>
                <button
                  class="text-left hover:underline cursor-pointer {currentNode?.id === node.id ? 'font-semibold' : ''}"
                  onclick={() => selectNode(node)}
                >{node.artefact.name}</button>
              </li>
            {/each}
          </ul>
        </AccordionItem>
      </Accordion>
    </div>
  </aside>

  <!-- ── Drag handle ── -->
  <div
    class="w-2 shrink-0 self-stretch cursor-col-resize flex items-center justify-center group"
    role="separator"
    aria-orientation="vertical"
    onmousedown={(e) => { e.preventDefault(); dragging = true; }}
  >
    <div class="w-px h-full bg-border group-hover:bg-primary/50 transition-colors"></div>
  </div>

  <!-- ── Right: controls + canvas ── -->
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
        <span>Iterations:</span>
        <Input class="w-16 h-7 text-xs px-2 py-0" type="number" bind:value={numberOfIterations} />
        <Button size="sm" class="h-7 text-xs" onclick={redoLayout}>Layout</Button>
      </div>
      <div class="flex items-center gap-1">
        <span>Label size:</span>
        <input class="w-24" type="range" min="0" max="15" step="0.5" oninput={onLabelThreshold} />
      </div>
      <div class="flex items-center gap-1">
        <Input class="w-32 h-7 text-xs px-2 py-0" type="text" placeholder="Filter nodes…" bind:value={nodeNameFilter} />
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

  {#if currentNode || currentEdge}
    <!-- ── Right drag handle ── -->
    <div
      class="w-2 shrink-0 self-stretch cursor-col-resize flex items-center justify-center group"
      role="separator"
      aria-orientation="vertical"
      onmousedown={(e) => { e.preventDefault(); draggingRight = true; }}
    >
      <div class="w-px h-full bg-border group-hover:bg-primary/50 transition-colors"></div>
    </div>

    <!-- ── Right panel: artefact or edge info ── -->
    <aside
      class="shrink-0 overflow-y-auto max-h-[680px] pl-2"
      style="width: {rightPanelWidth}px"
    >
      <button
        class="text-xs text-muted-foreground hover:text-foreground flex items-center gap-1 cursor-pointer mb-2"
        onclick={() => { currentNode = null; currentEdge = null; renderer?.refresh(); }}
      >
        ✕ Close
      </button>
      {#if currentNode}
        <ArtifactInfo graph={graph} node={currentNode} onNodeSelect={selectNode} />
      {:else if currentEdge}
        <EdgeInfo graph={graph} edge={currentEdge} onNodeSelect={selectNode} />
      {/if}
    </aside>
  {/if}

</div>
