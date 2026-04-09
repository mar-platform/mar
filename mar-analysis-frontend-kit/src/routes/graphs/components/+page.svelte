<script lang="ts">
	import GraphRepresentation from "$lib/components/graphs/GraphRepresentation.svelte";
	import { INITIAL_LABEL_SIZE, INITIAL_LABEL_THRESHOLD_MEDIUM, INITIAL_NODE_SIZE_MEDIUM } from "$lib/constants/values";
	import { globalState } from "$lib/stores/globalState.svelte";
	import type { PageProps } from "./$types";

    let { data }: PageProps = $props();

    async function setGraphMode() {
        await data.promise;
        // Set a high label threshold for megamodels to avoid clutter, but allow users to change it in the toolbar
        globalState.labelThreshold = INITIAL_LABEL_THRESHOLD_MEDIUM;
        globalState.nodeSize = INITIAL_NODE_SIZE_MEDIUM;
        globalState.labelSize = INITIAL_LABEL_SIZE;

        globalState.setGraphMode('COMPONENT');

        // If no parameter is provided, we can select the first component by default
        if (!data.q && globalState.components.length > 0) {
            globalState.selectComponent(globalState.components[0]);
            return;
        }

        // If the graph is loaded, try to select the component specified in the query parameter
        if (data.q) {
            // Verify that data.q is a valid component ID before selecting it
            const component = globalState.components.find(c => c === data.q);

            if (component) {
                globalState.selectComponent(component);
            } else {
                console.warn(`Component with ID ${data.q} not found.`);
                // If the specified component is not found, we select the first component as a fallback (if it exists)
                if (globalState.components.length > 0) {
                    globalState.selectComponent(globalState.components[0]);
                }
            }
        }
    }

    setGraphMode();
</script>

<GraphRepresentation mode="COMPONENT" />
