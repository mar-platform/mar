<script lang="ts">
    import { Button } from "$lib/components/ui/button";
    import { Label } from "$lib/components/ui/label";
    import API from "./API";
    import { artefactTypes } from "./GraphNodeTypes";
    import GraphVisualizer from "./GraphVisualizer.svelte";

    let sqlQuery = $state("select source, target, r.type from relationships r join artefacts a on r.source = a.id where r.type = 'typed-by' and a.category = 'transformation'");
    let document = $state<any>(undefined);

    function submitQuery(query: string) {
        if (!query) return;
        fetch(API.graphFromSql(query))
            .then(apiResponse => apiResponse.json())
            .then(doc => document = doc);
    }
</script>

<!-- Query form (always visible above the graph) -->
<div class="mb-3 flex gap-2 items-end">
    <div class="flex-1">
        <Label for="query">SQL Query</Label>
        <textarea
          id="query"
          name="text"
          class="flex min-h-[60px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
          bind:value={sqlQuery}
        ></textarea>
    </div>
    <Button onclick={() => submitQuery(sqlQuery)}>Submit</Button>
</div>

{#if document}
  <!-- SQL schema lives in the sidebar; replaced by artefact info on node click -->
  <GraphVisualizer {document} types={artefactTypes}>
    <pre class="text-xs bg-muted p-3 rounded-md overflow-x-auto">CREATE TABLE projects (
    id   varchar(255) PRIMARY KEY,
    url  text NOT NULL);
CREATE TABLE artefacts (
    id        varchar(255) PRIMARY KEY,
    type      varchar(255) NOT NULL,
    category  varchar(255) NOT NULL,
    name      varchar(255) NOT NULL,
    project_id varchar(255));
CREATE TABLE relationships (
    source  varchar(255) NOT NULL,
    target  varchar(255) NOT NULL,
    type    varchar(255) NOT NULL);</pre>
  </GraphVisualizer>
{/if}
