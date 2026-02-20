<script lang="ts">
  import { Tabs, TabsList, TabsTrigger, TabsContent } from "$lib/components/ui/tabs";

  import ClusterExploration from "./ClusterExploration.svelte";
  import DuplicationGraphExploration from "./DuplicationGraphExploration.svelte";
  import GraphExploration from './GraphExploration.svelte';
  import InterProjectExploration from "./InterProjectExploration.svelte";
  import ProjectExploration from "./ProjectExploration.svelte";
  import SqlExplorer from "./SqlExplorer.svelte";
  import Stats from "./Stats.svelte";

  let selection = $state('stats');
  let graphSelection = $state('project');
</script>

<div class="p-2">
  <Tabs bind:value={selection}>
    <TabsList>
      <TabsTrigger active={selection === 'stats'} onclick={() => selection = 'stats'}>Stats</TabsTrigger>
      <TabsTrigger active={selection === 'graph'} onclick={() => selection = 'graph'}>Graph exploration</TabsTrigger>
      <TabsTrigger active={selection === 'clusters'} onclick={() => selection = 'clusters'}>Clusters</TabsTrigger>
      <TabsTrigger active={selection === 'sql'} onclick={() => selection = 'sql'}>Database exploration</TabsTrigger>
    </TabsList>
  </Tabs>

  {#if selection === 'graph'}
    <Tabs bind:value={graphSelection} class="mt-2">
      <TabsList>
        <TabsTrigger active={graphSelection === 'all_graph'} onclick={() => graphSelection = 'all_graph'}>All</TabsTrigger>
        <TabsTrigger active={graphSelection === 'duplication-graph'} onclick={() => graphSelection = 'duplication-graph'}>Duplication</TabsTrigger>
        <TabsTrigger active={graphSelection === 'inter_project'} onclick={() => graphSelection = 'inter_project'}>Inter-project</TabsTrigger>
        <TabsTrigger active={graphSelection === 'project'} onclick={() => graphSelection = 'project'}>Project</TabsTrigger>
      </TabsList>
    </Tabs>
    <div class="mt-2 ml-5">
      {#if graphSelection === 'all_graph'}
        <GraphExploration />
      {:else if graphSelection === 'duplication-graph'}
        <DuplicationGraphExploration />
      {:else if graphSelection === 'inter_project'}
        <InterProjectExploration />
      {:else if graphSelection === 'project'}
        <ProjectExploration />
      {/if}
    </div>
  {:else if selection === 'stats'}
    <div class="mt-2 ml-5">
      <Stats />
    </div>
  {:else if selection === 'clusters'}
    <div class="mt-2 ml-5">
      <ClusterExploration />
    </div>
  {:else if selection === 'sql'}
    <div class="mt-2 ml-5">
      <SqlExplorer />
    </div>
  {/if}
</div>
