<script>
    import { createEventDispatcher } from "svelte";
    import { Stretch } from 'svelte-loading-spinners'
    import SearchFacets from './SearchFacets.svelte'


    const dispatch = createEventDispatcher();

    let errorText="";

    function notifyEvent(modelType) {
        dispatch("select", { modelType: modelType });
    }

    let loading = false
    function notifyLoading(isLoading) {
        dispatch("loading", { isLoading: isLoading} );
        loading = isLoading
    }

    function errors(error) {
        loading = false;
        errorText="An error is occured : \n"
        errorText+=error;
        window.$('.notifier-error').toggleClass('pop');
    }

    
    /* Interface */
    export let results = []

    export let searchText = "";

    async function handleSubmit(event) {
        let url = MAR.toTextSearchURL();
        if (searchText != null) {
            console.log(searchText);
            notifyLoading(true)
            const res = await fetch(url, {
                method: "POST",
                body: searchText,
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
    }
    
</script>

<div class="container">
<form
    action="#xxx"
    on:submit|preventDefault={handleSubmit}
    enctype="multipart/form-data">
    <div class="row" >
        <!-- I don't why I have to add the width: 100% here, but... -->
        <div class="alert alert-info" style="width: 100%; padding: 4px; padding-left: 8px"  role="alert">
            Enter search keywords
        </div>
    </div>
    
    <div class="row">
        <div class="input-group mb-3">
            <input
                bind:value={searchText}
                type="text"
                id="searchText"
                class="form-control"
                placeholder="Search for any type of model"
                aria-label="Relevant keywords for your model" />
        </div>
    </div>
    <div class="row justify-content-end">
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
</div>

<div class="notifier notifier-error">
	<div class="notifier-content">
		{errorText}
		<p class="green notifier-dismiss">Dismiss automatically</p>
	</div>
</div>
