<script>
    import { Button, FormGroup, Input, Label } from "sveltestrap";
    import API from "./API";
    import GraphVisualizer from "./GraphVisualizer.svelte";

    let sqlQuery = "select source, target, r.type from relationships r join artefacts a on r.source = a.id where r.type = 'typed-by' and a.category = 'transformation'";
    let document;

    function submitQuery(sqlQuery) {
        console.log(sqlQuery);
        if (sqlQuery == undefined)
            return;
        fetch(API.graphFromSql(sqlQuery))
            .then(apiResponse => apiResponse.json())
            .then(doc => document = doc);
    }

    $: console.log(document);

    // select source, target, r.type from relationships r join artefacts a on r.source = a.id where r.type = 'typed-by' and a.category = 'transformation'

    // No graph:
    // Identify projects with different types of artefacts
    // select project_id, count(distinct type) as c from artefacts group by project_id order by c;

</script>

<style>
#container {
    display: flex;
    gap: 20px;
}

#graph {
    height: 600px;
    width: 400px;
    flex-grow: 1;
}
</style>

<FormGroup>
    <Label for="query">SQL Query</Label>
    <Input type="textarea" name="text" id="query" bind:value={sqlQuery} />
    <Button on:click={e => submitQuery(sqlQuery)}>Submit</Button>
</FormGroup>

<div id="container"> 
    <div>
        <pre>
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
    <div id="graph">
        <GraphVisualizer  document={document} /> 
    </div>
</div>

