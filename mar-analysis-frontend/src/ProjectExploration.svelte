<script lang="ts">
    import { Form, FormGroup, FormText, Input, Label } from 'sveltestrap';
import API from './API';
    let radioGroup;

    let container;

    let projects = [];

    const searchOnChange = (e: Event) => {
        const changeValue = (e.target as HTMLInputElement).value;
        fetch(API.searchProject(changeValue)).
            then(res => res.json()).
            then(doc => projects = doc);
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
            <li>{project.id}</li>    
        {/each}
        </ul>
      </div>
      <div id="container" bind:this={container} style="margin-left: 620px; width: calc(100wh - 600px)"> 
      </div>
    </div>  
  </main>