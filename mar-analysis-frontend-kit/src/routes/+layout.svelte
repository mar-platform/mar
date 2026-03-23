<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import Navbar from '$lib/components/basic/Navbar.svelte';
	import { ModeWatcher } from "mode-watcher";

	let { children } = $props();
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<title>MDE Artefact Inspector</title>
</svelte:head>

<ModeWatcher />
<div class="bg-page-background min-h-svh w-full flex flex-col">
	<Navbar />
	<div class="m-5 flex-1">
		{@render children()}
	</div>
</div>
