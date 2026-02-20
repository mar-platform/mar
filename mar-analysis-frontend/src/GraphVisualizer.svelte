<script lang="ts">
    import Sigma from "sigma";
    import type { EdgeDisplayData, NodeDisplayData } from "sigma/types";
    import type Graph from "graphology";
    import UndirectedGraph from "graphology";

    import FA2Layout from "graphology-layout-forceatlas2/worker";
    import forceAtlas2 from "graphology-layout-forceatlas2";
    import random from 'graphology-layout/random';

    import ArtifactInfo from './ArtefactInfo.svelte'
    import { Accordion, AccordionItem } from "$lib/components/ui/accordion";
    import { Button } from "$lib/components/ui/button";
    import { Input } from "$lib/components/ui/input";

    let { types, document, children }: {
      types: any;
      document: any;
      children?: import('svelte').Snippet;
    } = $props();

    let container: HTMLDivElement;
    let graph: Graph;
    let currentNode = $state<any>(null);
    let renderer: Sigma | null = null;
    let numberOfIterations = $state(20);
    let nodeNameFilter = $state('');
    let showUnconnectedNodes = $state(false);

    const colorMap = types.reduce(function(map: any, obj: any) {
        map[obj.type] = obj.color;
        return map;
    }, {} as Record<string, string>);

    let checkedTypes = $state<Record<string, boolean>>({});
    for (let i = 0; i < types.length; i++) {
      checkedTypes[types[i].type] = true;
    }

    $effect(() => {
      if (document != undefined && container != undefined) {
        currentNode = null;
        if (renderer != null)
          renderer.kill();
        createNetwork(document);
      }
    });

    function createNetwork(doc: any) {
      graph = new UndirectedGraph();
      doc.nodes.forEach((node: any) => {
        let type: string;
        let name: string;
        if (node._type == 'artefact') {
            type = node.artefact.type;
            name = node.artefact.name;
        } else if (node._type == 'virtual') {
            type = node.kind;
            name = node.id;
        } else {
            type = 'error';
            name = 'unknown';
        }
        graph.addNode(node.id, {
          x: 0, y: 0,
          impl: node,
          nodeType: type,
          label: name,
          color: colorMap[type] || '#b34f47'
        });
      });

      doc.edges.forEach((edge: any) => {
        graph.addEdge(edge.source, edge.target, { edgeType: edge.type });
      });

      random.assign(graph);
      const sensibleSettings = forceAtlas2.inferSettings(graph);
      forceAtlas2.assign(graph, { iterations: numberOfIterations, settings: sensibleSettings });

      renderer = new Sigma(graph, container);
      renderer.on("clickNode", (e) => {
        currentNode = graph.getNodeAttributes(e.node).impl;
      });

      renderer.setSetting("nodeReducer", (nodeId, data) => {
        const res: Partial<NodeDisplayData> = { ...data };
        if (!checkedTypes[data.nodeType]) res.hidden = true;
        if (!showUnconnectedNodes && graph.degree(nodeId) == 0) res.hidden = true;
        return res;
      });

      renderer.setSetting("edgeReducer", (edge, data) => {
        const res: Partial<EdgeDisplayData> = { ...data };
        const srcType = graph.getNodeAttribute(graph.source(edge), "nodeType");
        const tgtType = graph.getNodeAttribute(graph.target(edge), "nodeType");
        if (!(checkedTypes[srcType] && checkedTypes[tgtType])) res.hidden = true;
        return res;
      });
    }

    function refresh() {
      renderer?.refresh();
    }

    function redoLayout() {
      const sensibleSettings = forceAtlas2.inferSettings(graph);
      forceAtlas2.assign(graph, { iterations: numberOfIterations, settings: sensibleSettings });
      refresh();
    }

    function onLabelThreshold(event: Event) {
      renderer?.setSetting("labelRenderedSizeThreshold", +(<HTMLInputElement>event.target).value);
    }

    function applyNodeFilter(_event: Event) {
      console.log(renderer);
    }

    function getArtefactNodes(nodes: any[]) {
      return nodes
        .filter((n: any) => n._type == 'artefact')
        .filter((n: any) => checkedTypes[n.artefact.type]);
    }
</script>

<div class="flex gap-4 items-start">

  <!-- ── Left sidebar ── -->
  <aside class="w-64 shrink-0 flex flex-col gap-2 overflow-y-auto max-h-[680px]">
    {#if currentNode}
      <button
        class="text-xs text-muted-foreground hover:text-foreground self-start flex items-center gap-1 cursor-pointer"
        onclick={() => currentNode = null}
      >
        ← Back
      </button>
      <ArtifactInfo graph={graph} node={currentNode} />
    {:else if children}
      {@render children()}
    {/if}

    <div class="mt-auto pt-2 border-t">
      <Accordion>
        <AccordionItem header="All artefacts">
          <ul class="text-sm space-y-0.5">
            {#each getArtefactNodes(document.nodes) as node}
              <li>{node.artefact.name}</li>
            {/each}
          </ul>
        </AccordionItem>
      </Accordion>
    </div>
  </aside>

  <!-- ── Right: controls + canvas ── -->
  <div class="flex-1 min-w-0 flex flex-col gap-2">

    <!-- Type filter -->
    <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
      <strong class="text-sm">Types:</strong>
      {#each types as {type}}
        <label style="color: {colorMap[type]}" class="flex items-center gap-1 text-sm cursor-pointer">
          <input type="checkbox" bind:checked={checkedTypes[type]} onchange={() => refresh()}>
          {type}
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

</div>
