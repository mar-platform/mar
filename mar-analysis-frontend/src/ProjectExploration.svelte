<script lang="ts">
  import { Form, FormGroup, FormText, Input, Label } from 'sveltestrap';
  import API from './API';
  import GraphVisualizer from './GraphVisualizer.svelte';  

  let projects = [];
  let document;

  const searchOnChange = (e: Event) => {
      const changeValue = (e.target as HTMLInputElement).value;
      fetch(API.searchProject(changeValue)).
          then(res => res.json()).
          then(doc => projects = doc);
  };

  const selectProject = (p : string) => {
      fetch(API.projectGraph(p)).
        then(res => res.json()).
        then(res => document = res);
  };
</script>

<main>
    <div style="width: 100%; overflow: hidden;">
      <div style="width: 600px; float: left;">       
        <Form autocomplete="off">  
            <FormGroup>
              <Label for="name">Project name</Label>
              <Input
                type="text"
                name="name"
                id="name"
                placeholder="Write a project name"
                on:input={searchOnChange}
            />
            </FormGroup>
        </Form>

        <ul>
        {#each projects as project (project.id)}
            <li><a href="#/" on:click={e => selectProject(project.id)}>{project.id}</a></li>    
        {/each}
        </ul>
      </div>
      <div id="container" style="margin-left: 620px; width: calc(100wh - 600px)"> 
        <GraphVisualizer document={document} /> 
      </div>
    </div>  

  </main>

