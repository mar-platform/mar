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
      fetch(API.getProjects())
        .then(res => res.json())
        .then(doc => { projects = doc; allProjects = doc; });
  });

  $effect(() => {
    console.log(projects);
  });

  const searchOnChange = (e: Event) => {
      const value = (e.target as HTMLInputElement).value;
      if (value === '') {
          projects = allProjects;
      } else {
          fetch(API.searchProject(value))
            .then(res => res.json())
            .then(doc => projects = doc);
      }
  };

  const selectProject = (p: string) => {
      fetch(API.projectGraph(p))
        .then(res => res.json())
        .then(res => document = res);
  };
</script>

{#snippet projectList()}
  <div class="flex flex-col gap-2">
    <form autocomplete="off">
      <Label for="name">Project name</Label>
      <Input
        type="text"
        name="name"
        id="name"
        placeholder="Write a project name"
        oninput={searchOnChange}
      />
    </form>
    <ul class="text-sm space-y-0.5 overflow-y-auto max-h-96">
      {#each projects as project (project.id)}
        <li>
          <a
            href="#/"
            class="text-primary underline hover:no-underline"
            onclick={() => selectProject(project.id)}
          >{project.id}</a>
        </li>
      {/each}
    </ul>
  </div>
{/snippet}

{#if document}
  <!-- Project selected: sidebar holds project list (hidden when a node is clicked) -->
  <GraphVisualizer {document} types={artefactTypes}>
    {@render projectList()}
  </GraphVisualizer>
{:else}
  <!-- No project yet: show search/list standalone -->
  <div class="max-w-xs">
    {@render projectList()}
  </div>
{/if}
