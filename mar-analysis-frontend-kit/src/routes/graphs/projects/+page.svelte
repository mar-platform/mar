<script lang="ts">
	import GraphRepresentation from "$lib/components/graphs/GraphRepresentation.svelte";
	import ProjectList from "$lib/components/projects/ProjectList.svelte";

</script>

<div class="flex flex-1 h-[calc(100svh-152px-40px)] max-h-[calc(100svh-152px-40px)] gap-4">
    <ProjectList />
    <GraphRepresentation />
</div>
