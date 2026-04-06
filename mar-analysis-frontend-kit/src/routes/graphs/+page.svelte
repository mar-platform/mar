<script lang="ts">
	import GraphRepresentation from "$lib/components/graphs/GraphRepresentation.svelte";
	import { globalState } from "$lib/stores/globalState.svelte";
	import type { PageProps } from "./$types";

    let { data }: PageProps = $props();

    async function setGraphMode() {
        await data.promise;
        globalState.setGraphMode('ALL');
    }

    setGraphMode();
</script>

<GraphRepresentation />
