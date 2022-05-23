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




  function errors(error) {
    loading = false;
    alert("An error is occured :"+error);
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
  export let searchModelType = 'ecore';

  function selectModelType(e) {
    var selected = modelTypes[e.detail.modelType];
    if (selected === null) selected = modelTypes["all"];
    modelType = selected;
    console.log("Selected model type: " + modelType);
  }

  let searchText = null;
  // Try to get config from mongo
/*async function loadingTime(event) { //when the time is too long an error occured
  function p1() {
        return new Promise(async (resolve) => {
            setTimeout(() => {
                resolve(10);
                if(loading==true){
                  errors();
                }
            }, 4000)
        });
    }

    async function handleSubmit(event) {
    let modelType = searchModelType;
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
				
      });
      // console.log(res);
      const json = await res.json();
      notifyLoading(false)
      console.log(json);
      results = json;
    }

    return await Promise.race([handleSubmit(), p1()]);
}*/
  async function handleSubmit(event) {
    let modelType = searchModelType;
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
    }).catch(error => {
      errors(error);
    });

    if(res!= undefined){
      const json = await res.json();
      notifyLoading(false)
      console.log(json);
      results = json;
    }
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
