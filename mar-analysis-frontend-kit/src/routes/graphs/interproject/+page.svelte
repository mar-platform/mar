<script lang="ts">
	import GraphRepresentation from "$lib/components/graphs/GraphRepresentation.svelte";
	import { INITIAL_LABEL_THRESHOLD_MEDIUM } from "$lib/constants/values";
	import { globalState } from "$lib/stores/globalState.svelte";
	import type { PageProps } from "./$types";

    let { data }: PageProps = $props();

    async function setGraphMode() {
        await data.promise;
        // Set a high label threshold for megamodels to avoid clutter, but allow users to change it in the toolbar
        globalState.labelThreshold = INITIAL_LABEL_THRESHOLD_MEDIUM;
        globalState.setGraphMode('INTER_PROJECT');
    }

    setGraphMode();
</script>

<GraphRepresentation mode="INTER_PROJECT" />
