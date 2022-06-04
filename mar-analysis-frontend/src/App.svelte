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

  let isOpen = false;
  let selection : string = 'graph'
</script>

<Nav pills>
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
  <GraphExploration />
{:else if selection == 'clusters'}
  <ClusterExploration />
{/if}