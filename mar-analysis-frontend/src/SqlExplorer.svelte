<script lang="ts">
    import { Button } from "$lib/components/ui/button";
    import { Label } from "$lib/components/ui/label";
    import API from "./API";
    import { artefactTypes } from "./GraphNodeTypes";
    import GraphVisualizer from "./GraphVisualizer.svelte";

    let sqlQuery = $state("select source, target, r.type from relationships r join artefacts a on r.source = a.id where r.type = 'typed-by' and a.category = 'transformation'");
    let document = $state<any>(undefined);

    function submitQuery(query: string) {
        console.log(query);
        if (query == undefined)
            return;
        fetch(API.graphFromSql(query))
            .then(apiResponse => apiResponse.json())
            .then(doc => document = doc);
    }

    $effect(() => {
        console.log(document);
    });
</script>

<div class="mb-4">
    <Label for="query">SQL Query</Label>
    <textarea
      id="query"
      name="text"
      class="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
      bind:value={sqlQuery}
    ></textarea>
    <Button class="mt-2" onclick={() => submitQuery(sqlQuery)}>Submit</Button>
</div>

<div class="flex gap-5">
    <div>
        <pre class="text-xs bg-muted p-3 rounded-md">
CREATE TABLE projects (
    id            varchar(255) PRIMARY KEY,
    url           text NOT NULL);
CREATE TABLE artefacts (
    id            varchar(255) PRIMARY KEY,
    type          varchar(255) NOT NULL,
    category      varchar(255) NOT NULL,
    name          varchar(255) NOT NULL,
    project_id    varchar(255)
);
CREATE TABLE virtual_nodes (
    id            varchar(255) PRIMARY KEY,
    kind          varchar(255) NOT NULL);
CREATE TABLE relationships (
    source    varchar(255) NOT NULL,
    target    varchar(255) NOT NULL,
    type  varchar (255) NOT NULL);
        </pre>
    </div>
    <div class="h-[600px] w-[400px] flex-grow">
        {#if document}
          <GraphVisualizer document={document} types={artefactTypes} />
        {/if}
    </div>
</div>
