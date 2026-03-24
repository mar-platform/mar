<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.png';
	import Navbar from '$lib/components/basic/Navbar.svelte';
	import { ModeWatcher } from "mode-watcher";
	import { Toaster } from 'svelte-sonner';
	import LucideCircleCheckBig from '@lucide/svelte/icons/circle-check-big';
	import LucideOctagonX from '@lucide/svelte/icons/octagon-x';
	import LucideInfo from '@lucide/svelte/icons/info';
	import LucideTriangleAlert from '@lucide/svelte/icons/triangle-alert';

	let { children } = $props();
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<title>MDE Artefact Inspector</title>
</svelte:head>

<ModeWatcher />
<Toaster
	position='top-right'
	closeButton
	duration={5000}
	expand
	richColors
	visibleToasts={5}
	toastOptions={{
		class: '',
	}}
>
	{#snippet successIcon()}
		<LucideCircleCheckBig id="toast-success" size=16 />
	{/snippet}
	{#snippet errorIcon()}
		<LucideOctagonX id="toast-error" size=16 />
	{/snippet}
	{#snippet infoIcon()}
		<LucideInfo id="toast-info" size=16 />
	{/snippet}
	{#snippet warningIcon()}
		<LucideTriangleAlert id="toast-warning" size=16 />
	{/snippet}
</Toaster>

<div class="min-h-svh w-full flex flex-col">
	<Navbar />
	<div class="m-5 mx-10 flex-1">
		{@render children()}
	</div>
</div>
