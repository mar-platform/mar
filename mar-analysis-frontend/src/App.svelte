<script lang="ts">
  // Examples:
  // - Layout: https://codesandbox.io/s/ekjy6
  // - Layout: https://www.npmjs.com/package/graphology-layout-forceatlas2
  import Sigma from "sigma";
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

  async function call() {
    //fetch(`https://localhost:8443/graph`)
    fetch(`http://localhost:8080/graph`)
      .then(apiResponse => apiResponse.json())
      .then(document => {
        createNetwork(document);
      })
  }

  function createNetwork(document) {
    const colorMap = {
      'transformation' : '#5ac477',
      'metamodel' : '#5a8bc4'
    }
    const graph: Graph = new Graph();

    let nodeIds : Map<string, unknown> = new Map<string, number>();

    document.nodes.forEach(node => {
      nodeIds.set(node.id, node);
      graph.addNode(node.id, {
        x: 0,
        y: 0,
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
      iterations: 50,
      settings: sensibleSettings
    });

    let renderer = new Sigma(graph, container);
    renderer.on("clickNode", (e) => {
      currentNode = nodeIds.get(e.node);
    });
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
