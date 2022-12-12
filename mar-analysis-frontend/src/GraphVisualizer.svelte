<script lang="ts">
    // Examples:
    // - Layout: https://codesandbox.io/s/ekjy6
    // - Layout: https://www.npmjs.com/package/graphology-layout-forceatlas2
    import Sigma from "sigma";
    import type { Coordinates, EdgeDisplayData, NodeDisplayData } from "sigma/types";
    import type Graph from "graphology";
    import UndirectedGraph from "graphology";
  
    import FA2Layout from "graphology-layout-forceatlas2/worker";
    import forceAtlas2 from "graphology-layout-forceatlas2";
    import random from 'graphology-layout/random';
  
    import ArtifactInfo from './ArtefactInfo.svelte'

    import { Accordion, AccordionItem, Badge, Button, Col, Container, FormGroup, Input, Label, Row } from 'sveltestrap';

    export let document;
    $: if (document != undefined && container != undefined) {
        // Make sure that we start again everything from scratch on each rebind of document
        currentNode = null;
        if (renderer != null)
          renderer.kill();

        createNetwork(document);
    } 

    let container;
    let graph : Graph;
    let currentNode;
    let renderer;
    let numberOfIterations = 20;
    let showUnconnectedNodes = false;

    // TODO: Recover this from the web
    const types = [
      { type: 'duplication', checked : true,   color: '#ffe100' },
      { type: 'qvto', checked : true,   color: '#5ac477' },
      { type: 'ocl', checked : true,   color: '#8a8477' },
      { type: 'ecore', checked : true,  color: '#5a8bc4' },
      { type: 'xtext', checked : true,  color: '#6e1ae5'},
      { type: 'emfatic', checked : true, color: '#fb04d8' },
      { type: 'epsilon', checked : true,  color: '#b33636' },
      /*
      { type: 'evl', checked : true,  color: '#b33636' },
      { type: 'etl', checked : true, color: '#b33636' },
      { type: 'eol', checked : true, color: '#b33636' },
      { type: 'egl', checked : true, color: '#b33636' },
      */
      { type: 'acceleo', checked : true, color: '#36b336' },
      { type: 'atl', checked : true, color: '#2a4e6a' },
      { type: 'sirius', checked : true, color: '#3b5c4e' },
      { type: 'henshin', checked : true, color: '#3baa9e' },
    ]
  
    const colorMap = types.reduce(function(map, obj) { 
        map[obj.type] = obj.color; 
        return map; 
    }, {});  
  
    function createNetwork(document) {
      const colorCategoryMap = {
        'transformation' : '#5ac477',
        'metamodel' : '#5a8bc4'
      }  
  
      graph = new UndirectedGraph();
      document.nodes.forEach(node => {
        let type : string;
        let name : string;
        if (node._type == 'artefact') {
            type = node.artefact.type;
            name = node.artefact.name;
        } else if (node._type == 'virtual') {
            type = node.kind;
            name = node.id;
        } else {
            type = 'error';
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
      
      document.edges.forEach(edge => {
        // console.log("Edge: ", edge.source, edge.target);
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
        iterations: numberOfIterations, /* 75 */
        settings: sensibleSettings
      });
  
      renderer = new Sigma(graph, container);
      renderer.on("clickNode", (e) => {
        currentNode = graph.getNodeAttributes(e.node).impl;
      });
  
      renderer.setSetting("nodeReducer", (nodeId, data) => {
        const res: Partial<NodeDisplayData> = { ...data };
  
        if (checkedTypes[data.nodeType]) {
          // res.color = colorMap[data.nodeType];
        } else {
          //res.color = "#f6f6f6";
          // res.color = "#ffffff";
          res.hidden = true;
        }

        if (!showUnconnectedNodes && graph.degree(nodeId) == 0) {
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
      console.log("refresh", renderer);
      if (renderer != null)
        renderer.refresh();
    }
  
    let checkedTypes = { }
    for (let i = 0; i < types.length; i++) {
      checkedTypes[types[i].type] = true;
    }
  
    function getArtefactNodes(nodes) {
      return nodes.filter(n => n._type == 'artefact')
                  //.filter(n => showUnconnectedNodes ? true : graph.degree(n) > 0)      
                  .filter(n => checkedTypes[n.artefact.type]);
    }

    function redoLayout() {
      const sensibleSettings = forceAtlas2.inferSettings(graph);
      forceAtlas2.assign(graph, {
        iterations: numberOfIterations,
        settings: sensibleSettings
      });
      refresh();
    }
  </script>
  
<style>
  #view {
    margin: 10px;
  }

  #container {
    /* width: 800px; */
    height: 600px; 
    border: 1px solid gray;

    resize:both;
    overflow:auto; /* something other than visible */
  }
</style>


<main id="view">
  <div>
    Artefact types
    {#each types as {type, checked}, idx }
    <label style="color: {colorMap[type]}">
      <input type=checkbox bind:checked={checkedTypes[type]} on:change={(e) => refresh()}>
      {type}
    </label>
    {/each}
  </div>
  <Container style="margin-top: 10px; padding-left: 0px">
    <Row width="800px">
      <Col xs="auto">
        <label>
          <input type=checkbox bind:checked={showUnconnectedNodes} on:change={(e) => refresh()}>
          Show unconnected nodes
        </label>        
      </Col>  
      <Col xs="auto">
        <Label>Iterations: </Label>
      </Col>
      <Col xs="auto">
        <FormGroup>
          <Input 
            style="width: 100px"
            type="number"
            name="layout-iterations"
            id="layout-iterations"
            placeholder="Number of layout iterations"
            value={numberOfIterations}
          />
        </FormGroup>
      </Col>
      <Col xs="auto">
        <Button on:click={redoLayout}>Layout</Button>
      </Col>
    </Row>
  </Container>
  
  <div id="container" bind:this={container}> 
  </div>
  
  <Accordion stayOpen>
    <AccordionItem header="All artefacts">
      <ul>
        {#each getArtefactNodes(document.nodes) as node}
          <li>
            {node.artefact.name}
          </li>
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


  <!-- Two columns-->
  <!--  
    <main>
      <div>
        Artefact types
        {#each types as {type, checked}, idx }
        <label style="color: {colorMap[type]}">
          <input type=checkbox bind:checked={checkedTypes[type]} on:change={(e) => refresh()}>
          {type}
        </label>
        {/each}
      </div>
      
      <div style="width: 100%; overflow: hidden;">
        <div style="width: 600px; float: left;">       
          {#if currentNode != undefined}
          <ArtifactInfo graph={graph} node={currentNode} />
          {/if}
        </div>
        <div id="container" bind:this={container} style="margin-left: 620px; width: calc(100wh - 600px)"> 
        </div>
      </div>  
    </main>
  -->
    