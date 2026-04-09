<script lang="ts">
	import GraphRepresentation from "$lib/components/graphs/GraphRepresentation.svelte";
	import { globalState } from "$lib/stores/globalState.svelte";
	import type { PageProps } from "./$types";

    let { data }: PageProps = $props();

    async function setGraphMode() {
        await data.promise;
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
