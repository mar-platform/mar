<script lang="ts">
  import { Input } from "$lib/components/ui/input";
  import { Label } from "$lib/components/ui/label";
  import API from './API';
  import { artefactTypes } from './GraphNodeTypes';
  import GraphVisualizer from './GraphVisualizer.svelte';
  import { onMount } from 'svelte';

  let allProjects: any[] = [];
  let projects = $state<any[]>([]);
  let document = $state<any>(undefined);

  onMount(() => {
      fetch(API.getProjects()).
        then(res => res.json()).
        then(doc => {
          projects = doc;
          allProjects = doc;
        });
  });

  const searchOnChange = (e: Event) => {
      const changeValue = (e.target as HTMLInputElement).value;
      if (changeValue == '') {
          projects = allProjects;
          return;
      } else {
        fetch(API.searchProject(changeValue)).
          then(res => res.json()).
          then(doc => projects = doc);
      }
  };

  const selectProject = (p: string) => {
      fetch(API.projectGraph(p)).
        then(res => res.json()).
        then(res => document = res);
  };
</script>

<main>
    <div class="flex w-full overflow-hidden">
      <div class="w-[600px] shrink-0">
        <form autocomplete="off">
            <div class="mb-4">
              <Label for="name">Project name</Label>
              <Input
                type="text"
                name="name"
                id="name"
                placeholder="Write a project name"
                oninput={searchOnChange}
              />
            </div>
        </form>

        <ul class="list-disc pl-5">
        {#each projects as project (project.id)}
            <li><a href="#/" class="text-primary underline" onclick={(e) => selectProject(project.id)}>{project.id}</a></li>
        {/each}
        </ul>
      </div>
      <div class="ml-5 flex-1">
        {#if document}
          <GraphVisualizer document={document} types={artefactTypes} />
        {/if}
      </div>
    </div>
</main>
