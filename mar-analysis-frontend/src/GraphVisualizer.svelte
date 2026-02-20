<script lang="ts">
    import Sigma from "sigma";
    import type { Coordinates, EdgeDisplayData, NodeDisplayData } from "sigma/types";
    import type Graph from "graphology";
    import UndirectedGraph from "graphology";

    import FA2Layout from "graphology-layout-forceatlas2/worker";
    import forceAtlas2 from "graphology-layout-forceatlas2";
    import random from 'graphology-layout/random';

    import ArtifactInfo from './ArtefactInfo.svelte'
    import { Accordion, AccordionItem } from "$lib/components/ui/accordion";
    import { Button } from "$lib/components/ui/button";
    import { Input } from "$lib/components/ui/input";

    let { types, document }: { types: any; document: any } = $props();

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

    function createNetwork(document: any) {
      graph = new UndirectedGraph();
      document.nodes.forEach((node: any) => {
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
          x: 0,
          y: 0,
          impl: node,
          nodeType: type,
          label: name,
          color: colorMap[type] || '#b34f47'
        });
      });

      document.edges.forEach((edge: any) => {
        graph.addEdge(edge.source, edge.target, {
          edgeType: edge.type
        });
      });

      random.assign(graph);

      const sensibleSettings = forceAtlas2.inferSettings(graph);
      forceAtlas2.assign(graph, {
        iterations: numberOfIterations,
        settings: sensibleSettings
      });

      renderer = new Sigma(graph, container);
      renderer.on("clickNode", (e) => {
        currentNode = graph.getNodeAttributes(e.node).impl;
      });

      renderer.setSetting("nodeReducer", (nodeId, data) => {
        const res: Partial<NodeDisplayData> = { ...data };

        if (!checkedTypes[data.nodeType]) {
          res.hidden = true;
        }

        if (!showUnconnectedNodes && graph.degree(nodeId) == 0) {
          res.hidden = true;
        }

        return res;
      });

      renderer.setSetting("edgeReducer", (edge, data) => {
        const res: Partial<EdgeDisplayData> = { ...data };
        const src: any = graph.source(edge);
        const tgt: any = graph.target(edge);
        const srcType = graph.getNodeAttribute(src, "nodeType");
        const tgtType = graph.getNodeAttribute(tgt, "nodeType");

        if (!(checkedTypes[srcType] && checkedTypes[tgtType])) {
          res.hidden = true;
        }

        return res;
      });
    }

    function setLabelThreshold(value: string) {
      renderer?.setSetting("labelRenderedSizeThreshold", +value);
    }

    function onLabelTreshold(event: Event) {
      setLabelThreshold((<HTMLInputElement>event.target).value)
    }

    function refresh() {
      if (renderer != null)
        renderer.refresh();
    }

    function getArtefactNodes(nodes: any[]) {
      return nodes.filter((n: any) => n._type == 'artefact')
                  .filter((n: any) => checkedTypes[n.artefact.type]);
    }

    function redoLayout() {
      const sensibleSettings = forceAtlas2.inferSettings(graph);
      forceAtlas2.assign(graph, {
        iterations: numberOfIterations,
        settings: sensibleSettings
      });
      refresh();
    }

    function applyNodeFilter(event: Event) {
      console.log(renderer);
    }
</script>

<main class="m-2.5">
  <div class="mt-2.5 pl-0 ml-2.5">
    <!-- Artefact Types Row -->
    <div class="flex flex-wrap items-center gap-4 mb-2.5">
      <strong class="w-40">Artefact Types</strong>
      {#each types as {type, checked}, idx}
        <label style="color: {colorMap[type]}" class="flex items-center gap-1">
          <input type="checkbox" bind:checked={checkedTypes[type]} onchange={() => refresh()}>
          {type}
        </label>
      {/each}
    </div>

    <!-- Show Unconnected Nodes Row -->
    <div class="flex items-center gap-4 mb-2.5">
      <strong class="w-40">Show Unconnected Nodes</strong>
      <label class="flex items-center gap-1">
        <input type="checkbox" bind:checked={showUnconnectedNodes} onchange={() => refresh()}>
      </label>
    </div>

    <!-- Iterations Row -->
    <div class="flex items-center gap-4 mb-2.5">
      <strong class="w-40">Iterations:</strong>
      <Input
        class="w-24"
        type="number"
        name="layout-iterations"
        id="layout-iterations"
        placeholder="Number of layout iterations"
        bind:value={numberOfIterations}
      />
      <Button onclick={redoLayout}>Layout</Button>
    </div>

    <!-- Label Threshold Row -->
    <div class="flex items-center gap-4 mb-2.5">
      <strong class="w-40">Label Node Size Toggle:</strong>
      <input id="labels-threshold" type="range" min="0" max="15" step="0.5" oninput={onLabelTreshold}/>
    </div>

    <!-- Node Filter Row -->
    <div class="flex items-center gap-4 mb-2.5">
      <strong class="w-40">Label Node Filter:</strong>
      <Input
        class="w-48"
        type="text"
        name="layout-node-filter"
        id="layout-node-filter"
        placeholder="Node name filter"
        bind:value={nodeNameFilter}
      />
      <Button onclick={applyNodeFilter}>Filter</Button>
    </div>
  </div>

  <div
    class="h-[600px] border border-gray-400 mt-4 resize overflow-auto"
    bind:this={container}
  ></div>

  <Accordion>
    <AccordionItem header="All artefacts">
      <ul class="list-disc pl-5">
        {#each getArtefactNodes(document.nodes) as node}
          <li>{node.artefact.name}</li>
        {/each}
      </ul>
    </AccordionItem>
    <AccordionItem active header="Selected artefact information">
      <div>
        {#if currentNode != undefined}
          <ArtifactInfo graph={graph} node={currentNode} />
        {/if}
      </div>
    </AccordionItem>
  </Accordion>
</main>
