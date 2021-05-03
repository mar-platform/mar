<script>
  import CodeMirror from "./codemirror/CodeMirror.svelte";
  import { createEventDispatcher } from "svelte";
  import { Stretch } from 'svelte-loading-spinners'

  const dispatch = createEventDispatcher();

  let loading = false
  function notifyLoading(isLoading) {
    dispatch("loading", { isLoading: isLoading} );
    loading = isLoading
  }

  let value = `package relational;
class Table { 
  attr String[1] name;
  val Column[*] columns;
}

class Column {
  attr String[1] name;        
}
    `;

  /* Interface */
  export let results = [];

  function selectModelType(e) {
    var selected = modelTypes[e.detail.modelType];
    if (selected === null) selected = modelTypes["all"];
    modelType = selected;
    console.log("Selected model type: " + modelType);
  }

  let searchText = null;

  async function handleSubmit(event) {
    let modelType = "ecore";
    let syntax = "emfatic";
    let url = MAR.toSearchURL(modelType, syntax);
    console.log(url);
    notifyLoading(true)
    const res = await fetch(url, {
      method: "POST",
      body: value,
      /*
				body: JSON.stringify({
					source: source,
					meta: meta
				})
				*/
    });

    // console.log(res);
    const json = await res.json();
    notifyLoading(false)
    console.log(json);
    results = json;
  }
</script>

<main>
  <div
    class="alert alert-info"
    style="padding: 4px; padding-left: 8px"
    role="alert">
    Write a model fragment to search
  </div>
  <form
    action="#xxx"
    on:submit|preventDefault={handleSubmit}
    enctype="multipart/form-data">
    
    <CodeMirror bind:value />
    <div style="text-align: right; margin-top: 10px">
      {#if loading} 
        <Stretch size="40" color="#FF3E00" unit="px"></Stretch>
      {:else}
        <input
            class="btn btn-secondary"
            type="submit"
            value="Submit!" />
      {/if}      
    </div>
  </form>
</main>
