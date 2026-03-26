<script lang="ts">
	import GraphRepresentation from "$lib/components/graphs/GraphRepresentation.svelte";
	import { globalState } from "$lib/stores/globalState.svelte";
	import type { PageProps } from "./$types";

    let { data }: PageProps = $props();

    async function setGraphMode() {
        await data.promise;
        globalState.setGraphMode('INTER_PROJECT');
    }

    setGraphMode();
</script>

<div class="flex flex-1 h-[calc(100svh-152px-40px)] max-h-[calc(100svh-152px-40px)] gap-4">
    <GraphRepresentation />
</div>
