<script>
    import { Button, FormGroup, Input, Label } from "sveltestrap";
    import API from "./API";
    import GraphVisualizer from "./GraphVisualizer.svelte";

    let sqlQuery;
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

<FormGroup>
    <Label for="query">SQL Query</Label>
    <Input type="textarea" name="text" id="query" bind:value={sqlQuery} />
    <Button on:click={e => submitQuery(sqlQuery)}>Submit</Button>
</FormGroup>

<div id="container" style="margin-left: 620px; width: calc(100wh - 600px)"> 
    <GraphVisualizer  document={document} /> 
</div>

