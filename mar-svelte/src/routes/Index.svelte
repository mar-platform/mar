<script>
    import SearchMode 			from "../components/SearchMode.svelte";
	import ModelTypeSelection 	from "../components/ModelTypeSelection.svelte";

	import StepSymbol 			from "../components/StepSymbol.svelte";

	import SearchBox 			from "../components/SearchBox.svelte";

	import UploadXMI 			from "../components/UploadXMI.svelte";
	import EcoreEditor 			from "../components/EcoreEditor.svelte";

	import SearchFacets 		from '../components/SearchFacets.svelte';

	import ResultItem 			from "../components/ResultItem.svelte";
	
	export let results = [];
	let resultsButton = [];
	let shown_resultsButton = [];
	let shown_results = [];

	let selectedModelType = null;
	let searchMode = "text";

	$: if (searchMode != "syntax") {
		selectedModelType = null;
	}

	let facets
	$: if (facets != undefined ) {
		if(resultsButton.length == 0){
			shown_resultsButton=shown_resultsButton.slice(0,0); 
		}
		shown_results = facets.filter(results)
	}

	$: if (resultsButton.length != 0) {
		shown_resultsButton = facets.filter(resultsButton)
	}
</script>		

<style>
	/* main {
		padding: 0;
		margin: 0 auto;
	} */

	.mar-step-group {
		margin-top: 20px;
	}
</style>

<div class="row justify-content-start">
	<div class="col-4">
		<!-- Step 1-->
		<div class="row justify-content-start mar-step-group">
			<div class="col-md-auto">
				<StepSymbol number={1} />
			</div>
			<div class="col">
				<SearchMode bind:selected={searchMode} />
			</div>
		</div>

		<!-- Step 2 -->
		<div class="row justify-content-start mar-step-group">
			<div class="col-md-auto">
				<StepSymbol number={2} />
			</div>
			<div class="col">
				{#if searchMode == 'text'}
					<SearchBox bind:results={results} />
				{:else if searchMode == 'example'}
					<ModelTypeSelection
						bind:selected={selectedModelType} />
				{:else if searchMode == 'chatbot'}
					<div class="alert alert-warning">We are working on this! Check out in a few weeks!</div>
				{/if}
			</div>
		</div>

		<!-- Step 3 (for concrete syntax) -->
		<div class="row justify-content-start mar-step-group">
			{#if selectedModelType == 'Ecore' || selectedModelType == 'Xtext'}
				<div class="col-md-auto">
					<StepSymbol number={3} />
				</div>
				<div class="col">
					<EcoreEditor bind:results={results} searchModelType={selectedModelType} />
				</div>
			{:else if selectedModelType == 'UML'}
				<div class="col-md-auto">
					<StepSymbol number={3} />
				</div>
				<div class="col">
					<UploadXMI modelType={selectedModelType} bind:results={results} />
				</div>
			{:else if selectedModelType != null }
				<div class="col-md-auto">
					<StepSymbol number={3} />
				</div>
				<div class="col">
					<UploadXMI modelType={selectedModelType} bind:results={results} />
				</div>						
			{/if}
		</div>
	</div>

	<div class="col-8">
		<div class="output">
			<div class="list-group">
				{#if results.length > 0}
					<SearchFacets search_items={results} bind:facets={facets} bind:resultsButton={resultsButton} />
				{/if}
				{#if shown_resultsButton.length > 0}
					<ul
						class="results"
						style="padding: 0; text-align: left;">
						{#each shown_resultsButton as item}
							<ResultItem bind:item bind:facets />
						{/each}
					</ul>
				{:else}
					{#if shown_results.length > 0}
						<ul
							class="results"
							style="padding: 0; text-align: left;">
							{#each shown_results as item}
								<ResultItem bind:item bind:facets />
							{/each}
						</ul>
					{:else}
						<!-- <div class="alert alert-warning" role="alert">
							No results
						</div> -->
					{/if}
				{/if}
			</div>
		</div>
	</div>
</div>
