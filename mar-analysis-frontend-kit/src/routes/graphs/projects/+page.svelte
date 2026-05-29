<script lang="ts">
	import GraphRepresentation from "$lib/components/graphs/GraphRepresentation.svelte";
	import { INITIAL_LABEL_SIZE_LARGE, INITIAL_LABEL_THRESHOLD, INITIAL_NODE_SIZE_LARGE } from "$lib/constants/values";
	import { globalState } from "$lib/stores/globalState.svelte";
	import type { PageProps } from "./$types";

    let { data }: PageProps = $props();

    async function setGraphMode() {
        await data.promise;

        // Apply some properties so that project graphs (whick are usually smaller than megamodels) are more readable by default, but allow users to change them in the toolbar
        globalState.labelThreshold = INITIAL_LABEL_THRESHOLD;
        globalState.nodeSize = INITIAL_NODE_SIZE_LARGE;
        globalState.labelSize = INITIAL_LABEL_SIZE_LARGE;
        
        await globalState.setGraphMode('PROJECT');

        // If no parameter is provided, we can select the first project by default
        if (!data.q && globalState.projects.length > 0) {
            globalState.selectProject(globalState.projects[0]);
            return;
        }

        // If the graph is loaded, try to select the project specified in the query parameter
        if (data.q) {
            // Verify that data.q is a valid project ID before selecting it
            const project = globalState.projects.find(p => p === data.q);
            
            if (project) {
                globalState.selectProject(project);
            } else {
                console.warn(`Project with ID ${data.q} not found.`);
                // If the specified project is not found, we select the first project as a fallback (if it exists)
                if (globalState.projects.length > 0) {
                    globalState.selectProject(globalState.projects[0]);
                }
            }
        }
    }

    setGraphMode();
</script>

<GraphRepresentation mode="PROJECT" />
