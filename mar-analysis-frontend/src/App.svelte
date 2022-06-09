<head>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css">
</head>

<script lang="ts">
  import {
    Nav,
    NavItem,
    Dropdown,
    DropdownItem,
    DropdownToggle,
    DropdownMenu,
    NavLink,
  } from "sveltestrap";

  import ClusterExploration from "./ClusterExploration.svelte";
  import GraphExploration from './GraphExploration.svelte';
  import InterProjectExploration from "./InterProjectExploration.svelte";
  import ProjectExploration from "./ProjectExploration.svelte";

  let selection : string = 'stats';
  let graphSelection : string = 'project';
</script>

<style>
  .content {
    margin-top: 10px;
    margin-left: 20px;
  }
</style>

<Nav pills>
  <NavItem>
    <NavLink active={selection == 'stats'} on:click={e => selection = 'stats'} href="#" >Stats</NavLink>
  </NavItem>
  <NavItem>
    <NavLink active={selection == 'graph'} on:click={e => selection = 'graph'} href="#" >Graph exploration</NavLink>
  </NavItem>
  <NavItem>
    <NavLink active={selection == 'clusters'} on:click={e => selection = 'clusters'} href="#" >Clusters</NavLink>
  </NavItem>
  <NavItem>
    <NavLink active={selection == 'sql'} on:click={e => selection = 'sql'} href="#" >Database exploration</NavLink>
  </NavItem>
  <!--
  <Dropdown nav {isOpen} toggle={() => (isOpen = !isOpen)}>
    <DropdownToggle nav caret>Dropdown</DropdownToggle>
    <DropdownMenu>
      <DropdownItem header>Header</DropdownItem>
      <DropdownItem disabled>Action</DropdownItem>
      <DropdownItem>Another Action</DropdownItem>
      <DropdownItem divider />
      <DropdownItem>Another Action</DropdownItem>
    </DropdownMenu>
  </Dropdown>
  <NavItem>
    <NavLink href="#">Link</NavLink>
  </NavItem>
  <NavItem>
    <NavLink href="#">Another Link</NavLink>
  </NavItem>
  <NavItem>
    <NavLink disabled href="#">Disabled Link</NavLink>
  </NavItem>
  -->
</Nav>

{#if selection == 'graph'} 
  <Nav pills>
    <NavItem>
      <NavLink active={graphSelection == 'all_graph'} on:click={e => graphSelection = 'all_graph'} href="#" >All</NavLink>
    </NavItem>
    <NavItem>
      <NavLink active={graphSelection == 'inter_project'} on:click={e => graphSelection = 'inter_project'} href="#" >Inter-project</NavLink>
    </NavItem>
    <NavItem>
      <NavLink active={graphSelection == 'project'} on:click={e => graphSelection = 'project'} href="#" >Project</NavLink>
    </NavItem>
  </Nav>
  <div class="content">
    {#if graphSelection == 'all_graph'} 
      <GraphExploration />
    {:else if graphSelection == 'inter_project'}
      <InterProjectExploration />
    {:else if graphSelection == 'project'}
      <ProjectExploration />
    {/if}
  </div>
{:else if selection == 'stats'}
<div class="content">
  SomeStatsHere
</div>
{:else if selection == 'clusters'}
<div class="content">
  <ClusterExploration />
</div>
{/if}