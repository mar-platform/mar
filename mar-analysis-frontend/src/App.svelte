<script lang="ts">
  // Examples:
  // - Layout: https://codesandbox.io/s/ekjy6
  // - Layout: https://www.npmjs.com/package/graphology-layout-forceatlas2
  import Sigma from "sigma";
  import type { Coordinates, EdgeDisplayData, NodeDisplayData } from "sigma/types";
  import Graph from "graphology"

  import FA2Layout from "graphology-layout-forceatlas2/worker";
  import forceAtlas2 from "graphology-layout-forceatlas2";
  import random from 'graphology-layout/random';

  //  import logo from './assets/svelte.png'
  //  import Counter from './lib/Counter.svelte'
  import { onMount } from "svelte";
  onMount(async () => {
    call();
     //createNetwork();
  });


  let container;
  let currentNode;
  let renderer;

  async function call() {
    //fetch(`https://localhost:8443/graph`)
    fetch(`http://localhost:8080/graph`)
      .then(apiResponse => apiResponse.json())
      .then(document => {
        createNetwork(document);
      })
  }

  function createNetwork(document) {
    const colorCategoryMap = {
      'transformation' : '#5ac477',
      'metamodel' : '#5a8bc4'
    }
    const colorMap = {
      'qvto' : '#5ac477',
      'ecore' : '#5a8bc4',
      'xtext' : '#6e1ae5',
      'emfatic' : '#fb04d8',
    }
    const graph: Graph = new Graph();

    let nodeIds : Map<string, unknown> = new Map<string, number>();

    document.nodes.forEach(node => {
      nodeIds.set(node.id, node);
      graph.addNode(node.id, {
        x: 0,
        y: 0,
        impl: node,
        nodeType: node.artefact.type,
        label: node.artefact.name,
        color: colorMap[node.artefact.type] || '#b34f47'
      });      
    });
    
    document.edges.forEach(edge => {
      graph.addEdge(edge.source, edge.target, {
        edgeType: edge.type
      });
    });

    //graph.nodes().forEach((node, i) => {
    //  const angle = (i * 2 * Math.PI) / graph.order;
    //  graph.setNodeAttribute(node, "x", 100 * Math.cos(angle));
    //  graph.setNodeAttribute(node, "y", 100 * Math.sin(angle));
    //});

    random.assign(graph);

    const sensibleSettings = forceAtlas2.inferSettings(graph);
      const fa2Layout = new FA2Layout(graph, {
      settings: sensibleSettings,
    });
    forceAtlas2.assign(graph, {
      iterations: 75,
      settings: sensibleSettings
    });

    renderer = new Sigma(graph, container);
    renderer.on("clickNode", (e) => {
      currentNode = nodeIds.get(e.node);
    });

    renderer.setSetting("nodeReducer", (nodeId, data) => {
      const res: Partial<NodeDisplayData> = { ...data };
      //const node: any = nodeIds.get(nodeId);
      //const node = data.impl;

      //console.log(node.nodeType)
      if (checkedTypes[data.nodeType]) {
        // res.color = colorMap[data.nodeType];
      } else {
        //res.color = "#f6f6f6";
        // res.color = "#ffffff";
        res.hidden = true;
      }
      
      return res;
    });

    renderer.setSetting("edgeReducer", (edge, data) => {
      const res: Partial<EdgeDisplayData> = { ...data };
      const src : any = graph.source(edge);
      const tgt : any = graph.target(edge);
      const srcType = graph.getNodeAttribute(src, "nodeType");
      const tgtType = graph.getNodeAttribute(tgt, "nodeType");

      if (! (checkedTypes[srcType] && checkedTypes[tgtType])) {
        res.hidden = true;
      }

      return res;
    });
  }

  function refresh() {
    if (renderer != null)
      renderer.refresh();
  }

  // TODO: Recover this from the web
  let types = [
    { type: 'qvto', checked : true },
    { type: 'ecore', checked : true },
    { type: 'xtext', checked : true },
    { type: 'emfatic', checked : true },
    { type: 'evl', checked : true },
    { type: 'etl', checked : true },
    { type: 'eol', checked : true },
    { type: 'egl', checked : true },
  ]

  let checkedTypes = { }
  for (let i = 0; i < types.length; i++) {
    checkedTypes[types[i].type] = true;
  }
</script>

<style>
  #container {
    width: 1024px;
    height: 800px;
    border: 1px solid gray;
  }
</style>

<main>
  <div>
    Artefact types
    {#each types as {type, checked}, idx }
    <label>
      <input type=checkbox bind:checked={checkedTypes[type]} on:change={(e) => refresh()}>
      {type}
    </label>
    {/each}
  </div>
  <div style="width: 100%; overflow: hidden;">
    <div style="width: 600px; float: left;"> 
      {#if currentNode != undefined}
        <div>Id: {currentNode.id}</div>
        <div>Type: {currentNode.artefact.type}</div>
        <div>Name: {currentNode.artefact.name}</div>
      {/if}
    </div>
    <div id="container" bind:this={container} style="margin-left: 620px; width: calc(100wh - 600px)"> 
    </div>
  </div>  
</main>
