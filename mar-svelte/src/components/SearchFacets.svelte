<script>
  import { ItemHelper, intCompare } from './../lib/common.js'
  import JSZip from 'jszip';
  import Index  from "./../routes/Index.svelte";
  import FileSaver from 'file-saver';
  import Slider from './bootstrap/Slider.svelte'
  export let fav=false;
  let favList=[];
  let favTab=[];
  export let resultsButton=[]
  let desc = false;
  let change;
  let param;
  let facetsButton;
  let actualTab=0;
  let lengthFavList;
  let save; // to know if the search is the same

  function removeItemWithSlice(index) {
  return [...favTab.slice(0, index), ...favTab.slice(index + 1)]
  }

  function actualPage(index) { // the other research appeared
    actualTab=index;
    actualTab=actualTab;
    fav=true;
    resultsButton=favTab[index].result;
    facetsButton=favTab[index].categories;
    
  }
  function deletePage(index) { // the other research appeared
    fav=false;
    favList.splice(favTab[actualTab].start,favTab[actualTab].end); // we delete the element if it exist
    //change start of the element
    for (let i=actualTab+1;i<favTab.length;i++){
      favTab[i].start-=favTab[actualTab].end;
    }
    favTab=removeItemWithSlice(actualTab);
    actualTab=favTab.length-1;
    if(favTab.length != 0 ){ // if there's any tab in favorite the button is still in deleted
      fav=true;
      resultsButton=favTab[actualTab].result; // we change the result page if there's still one available
    }
    console.log(favList)
  }

  function changeParam() {
    param=document.getElementById("param").style.display;
    if(document.getElementById("param").style.display==""){
      document.getElementById("param").style.display="block";
    }
		else if(document.getElementById("param").style.display=="none"){
      document.getElementById("param").style.display="block";
    }
    else if(document.getElementById("param").style.display=="block"){
      document.getElementById("param").style.display="none";
    }
  }

  function changeDesc() {
		desc = !desc;
  }

  function toggle(favItems) {
		fav = !fav;
    lengthFavList=favList.length;
    if(lengthFavList=== undefined){
      lengthFavList=0;
    }
    if(fav){
      change=0;
      for(let i=0;i<favTab.length;i++){ // if the page is already in fav you can't fav
        if(favTab[i].name===search_items[0].name.toString()){
          change=1;
          break;
        }
      } 
      if(change==0){
        actualTab=favTab.length;
        favTab = [...favTab, { start: lengthFavList, end: favItems.length, result : favItems , id :favTab.length, name : search_items[0].name.toString(), categories : facets.categories}];
        for (let i=0;i<favItems.length;i++){
            favList[lengthFavList+i]=favItems[i].urlhumanName;
        }
        resultsButton=favTab[actualTab].result;
        facetsButton=favTab[actualTab].categories;
      }
    }
    else{
        favList.splice(favTab[actualTab].start,favTab[actualTab].end); // we delete the element if it exist
        //change start of the element
        for (let i=actualTab+1;i<favTab.length;i++){
          favTab[i].start-=favTab[actualTab].end;
        }
        favTab=removeItemWithSlice(actualTab);
        //actualTab=favTab.length-1;
        //if(favTab.length != 0 ){ // if there's any tab in favorite the button is still in deleted
          //fav=true;
          //resultsButton=favTab[actualTab].result; // we change the result page if there's still one available
        //}
      }
	}

  const zip = new JSZip();
  let bashFile ="#!/bin/sh";

  function bashThis(elemBash) {
    bashFile ="#!/bin/sh";
    for (let i=0;i<elemBash.length;i++){
      bashFile+="\nwget "+elemBash[i];
    }
    zip.file("myModels.sh",bashFile);
    zip.generateAsync({ type: 'blob' }).then(function (content) {
          FileSaver.saveAs(content, 'myModels.zip');
      });
    
  }

  let maxSmell = 100;
  let sortType

  class Facets {
    constructor(items) {
      let elements = items.map(item => ItemHelper.numElements(item) )
      let smells = items.map(item => ItemHelper.smellSize(item) )      

      this.defaultMinSmells = Math.min(...smells);
      this.defaultMaxSmells = Math.max(...smells);
      this.minSmells = this.defaultMinSmells;
      this.maxSmells = this.defaultMaxSmells;

      this.defaultMinElements = Math.min(...elements);
      this.defaultMaxElements = Math.max(...elements);
      this.minElements = this.defaultMinElements;
      this.maxElements = this.defaultMaxElements;

      this.topics = [...new Set(items.flatMap(item => ItemHelper.topics(item).filter(i => i.length != 0)))]
      this.categories = [...new Set(items.map(item => ItemHelper.category(item)).filter(c => c != null))]
      this.modelTypes = [...new Set(items.map(item => ItemHelper.modelType(item)).filter(c => c != null))]
      this.origins = [...new Set(items.map(item => ItemHelper.origin(item)).filter(c => c != null))]
      
      this.selectedTopics = []
      this.selectedCategories = []
      this.selectedModelTypes = []
      this.selectedOrigins = []
      
      // this.sortByRelevance();
      this.sortBySimilarity();
    }    

    
    sortByRelevance() {
      sortType = "relevance"
      this.sortStrategy = (i1, i2) => -1 * intCompare(i1.mrankScore, i2.mrankScore)
    }

    sortBySimilarity() {
      sortType = "similarity"
      this.sortStrategy = (i1, i2) => -1 * intCompare(i1.score, i2.score)
    }

    sortByPopularity() {
      // this.sortStrategy = (i1, i2) => intCompare(ItemHelper.popularity(i1), ItemHelper.popularity(i2))
    } 
    
    sortByQuality() {
      // this.sortStrategy = (i1, i2) => intCompare(ItemHelper.quality(i1), ItemHelper.quality(i2))
    }

    addTopic(topic) {
      var index = this.selectedTopics.indexOf(topic);
      if (index > -1) {
        this.selectedTopics.splice(index, 1);
      } else {
        this.selectedTopics.push(topic)
      }
    }

    addCategory(category) {
      var index = this.selectedCategories.indexOf(category);
      if (index > -1) {
        this.selectedCategories.splice(index, 1);
      } else {
        this.selectedCategories.push(category)
      }
    }

    addModelType(topic) {
      var index = this.selectedModelTypes.indexOf(topic);
      if (index > -1) {
        this.selectedModelTypes.splice(index, 1);
      } else {
        this.selectedModelTypes.push(topic)
      }
    }


    addOrigin(topic) {
      var index = this.selectedOrigins.indexOf(topic);
      if (index > -1) {
        this.selectedOrigins.splice(index, 1);
      } else {
        this.selectedOrigins.push(topic)
      }
    }

    filter(items) {
      return items.filter(item => this.isSelectedItem(item)).sort(this.sortStrategy);
    }

    isSelectedItem(item) {
      let metadata = item.metadata
      if (metadata == undefined)
      	 return true; // We show everything, even it doesn't have metadata to avoid hiding results due
      if (metadata.numElements != undefined && (metadata.numElements < this.minElements || metadata.numElements > this.maxElements)) {
        return false;
      }

      let smells_length = ItemHelper.smellSize(item)
      if (smells_length < this.minSmells || smells_length > this.maxSmells) {
        return false;
      }

      // We check if the item contains one of the selected topics. 
      if (this.selectedTopics.length > 0 && metadata.topics != undefined) {
        let topic1, topic2
        let found = false;
        outer:
        for (topic1 of this.selectedTopics) {
          for (topic2 of metadata.topics) {
            if (topic1 == topic2) {
              found = true;
              break outer;
            }
          }          
        }

        if (! found)
          return false
      }

      if (this.selectedCategories.length > 0 && metadata.category != undefined) {
        let found = this.selectedCategories.includes(metadata.category);
        if (! found) 
          return false;
      }

      const origin = ItemHelper.origin(item);
      if (this.selectedOrigins.length > 0 && origin != undefined) {
        let found = this.selectedOrigins.includes(origin);
        if (! found) 
          return false;
      }

      const modelType = ItemHelper.modelType(item);
      if (this.selectedModelTypes.length > 0 && modelType != undefined) {
        let found = this.selectedModelTypes.includes(modelType);
        if (! found) 
          return false;
      }

      return true;
    }
  }

  export let search_items
  export let facets
  let new_items = [] /* This trick is to make sure that we only update the facet object when a new result is computed */

  $: if (search_items != new_items) {
    if(facets != undefined){
      for(let i=0;i<facets.categories.length;i++){  // For every new search checkboxs are reinitialised 
        if(document.getElementById(facets.categories[i].toString()).checked==true){
          document.getElementById(facets.categories[i].toString()).checked = false;
        }
      }
    }
    new_items = search_items
    facets = new Facets(search_items)
    if(JSON.stringify(search_items) != JSON.stringify(save)){ // when you change or not your page the buttons change or not
      fav=false; 
      change=0; // to see if a changement is occured
      for(let i=0;i<favTab.length;i++){
        if(favTab[i].name===search_items[0].name.toString()){
          actualTab=i;
          change=1;
          fav=true;
          break;
        }
      }
      if(change===0){
        actualTab=undefined;
      }
    }
    actualTab=actualTab;
    save=search_items; // to compare with the next search
    resultsButton=resultsButton.slice(0,0); // to reinit
    facets=facets
  }
</script>

<main >
  <form>
      <div
        class="list-group-item list-group-item-action flex-column align-items-start"
      >
      <button class="btn btn-outline-success btn-sm" on:click|preventDefault={() => { changeParam();} }>
        Parameters
      </button>
      <div id="param">
        <div class="row">
          <div class="col">
            <Slider label="Min smells" minDefault={facets.defaultMinSmells} maxDefault={facets.defaultMaxSmells} bind:value={facets.minSmells} />
          </div>

          <div class="col">
            <Slider label="Max smells" minDefault={facets.defaultMinSmells} maxDefault={facets.defaultMaxSmells} bind:value={facets.maxSmells} />
          </div>
        </div>

        <div class="row">
          <div class="col">
            <Slider label="Min elements" minDefault={facets.defaultMinElements} maxDefault={facets.defaultMaxElements} bind:value={facets.minElements} />
          </div>

          <div class="col">
            <Slider label="Max elements" minDefault={facets.defaultMinElements} maxDefault={facets.defaultMaxElements} bind:value={facets.maxElements} />
          </div>
        </div>

        <div class="row">
          <div class="col">          
            <div class="button-group">
              <button
                type="button"
                class="btn btn-default btn-sm dropdown-toggle"
                data-toggle="dropdown"
                ><span class="glyphicon glyphicon-cog" />
                <span class="caret" />Model type</button>
              <ul class="dropdown-menu">
                {#each facets.modelTypes as item}
                  <li>
                    <!-- <a href="#/" class="small" data-value="{item}" tabIndex="-1"> -->
                    <!-- <input type="checkbox" style="margin-left: 5px" bind:group={facets.selectedTopics} />&nbsp;{item} -->
                    <input type="checkbox" style="margin-left: 5px"  on:click={() => { facets.addModelType(item); facets = facets} } />&nbsp;{item}
                    <!-- </a> -->
                  </li>
                {/each}              
              </ul>
            </div>
          </div>
    
          <div class="col">          
            <div class="button-group">
              <button
                type="button"
                class="btn btn-default btn-sm dropdown-toggle"
                data-toggle="dropdown"
                ><span class="glyphicon glyphicon-cog" />
                <span class="caret" />Origin</button>
              <ul class="dropdown-menu">
                {#each facets.origins as item}
                  <li>
                    <input type="checkbox" style="margin-left: 5px" on:click={() => { facets.addOrigin(item); facets = facets} } />&nbsp;{item}
                  </li>
                {/each}              
              </ul>
            </div>
          </div>

          <div class="col">
            <div class="button-group">
              <button
                type="button"
                class="btn btn-default btn-sm dropdown-toggle"
                data-toggle="dropdown"
                ><span class="glyphicon glyphicon-cog" />
                <span class="caret" />Category</button>
              <ul class="dropdown-menu">
                {#each facets.categories as item}
                  <li>
                    <input type="checkbox" style="margin-left: 5px" id={item.toString()} on:click={() => { facets.addCategory(item); facets = facets; } } />&nbsp;{item}
                  </li>
                {/each}              
              </ul>
            </div>
          </div>
          
          <div class="col">          
            <div class="button-group">
              <button
                type="button"
                class="btn btn-default btn-sm dropdown-toggle"
                data-toggle="dropdown"
                ><span class="glyphicon glyphicon-cog" />
                <span class="caret" />Topics</button>
              <ul class="dropdown-menu">
                {#each facets.topics as item}
                  <li>
                    <input type="checkbox" style="margin-left: 5px" on:click={() => { facets.addTopic(item); facets = facets} } />&nbsp;{item}
                  </li>
                {/each}              
              </ul>
            </div>
          </div>

          <div class="col">
            <div class="btn-group">
              <button class="btn btn-secondary btn-sm dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                Sorted by {sortType}
              </button>
              <div class="dropdown-menu">
                <!--
                <a class="dropdown-item" href="#/" on:click={() => { facets.sortByRelevance(); facets = facets }}>MRank relevance</a>
                -->
                <a class="dropdown-item" href="#/" on:click={() => { facets.sortBySimilarity(); facets = facets }}>Similarity</a>
                <a class="dropdown-item" href="#/" on:click={() => { facets.sortByQuality(); facets = facets }}>Quality</a>
                <a class="dropdown-item" href="#/" on:click={() => { facets.sortByPopularity(); facets = facets }}>Popularity</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    <button class="btn btn-outline-success btn-sm" on:click|preventDefault={() => { changeDesc();} }>
      Description
    </button>
    {#if desc == true}
      {#if resultsButton.length > 0}
        <p>Some informations about the research :</p>
        <p>There's {resultsButton.length.toString()} {#if resultsButton.length > 1} elements {/if} {#if resultsButton.length <= 1} element {/if} corresponding to your research.</p>
        <p>There is {facetsButton.length.toString()} {#if facetsButton.length > 1} categories {/if} {#if facetsButton.length <= 1} categorie {/if} corresponding to {#if facetsButton.length > 1} them {/if} {#if facetsButton.length <= 1} this {/if} : {facetsButton.toString()}.</p>
      {:else}
        <p>Some informations about the research :</p>
        <p>There's {search_items.length.toString()} {#if search_items.length > 1} elements {/if} {#if search_items.length <= 1} element {/if} corresponding to your research.</p>
        <p>There is {facets.categories.length.toString()} {#if facets.categories.length > 1} categories {/if} {#if facets.categories.length <= 1} categorie {/if} corresponding to {#if facets.categories.length > 1} them {/if} {#if facets.categories.length <= 1} this {/if} : {facets.categories.toString()}.</p>
      {/if}
    {/if}
      <div>
      {#if fav}
        <button class="btn btn-danger btn-sm" on:click|preventDefault={() => { toggle(search_items);} }>
          Remove to favorite
        </button>
      {:else}
        <button class="btn btn-danger btn-sm" on:click|preventDefault={() => {toggle(search_items);} }>
          Add to favorite
        </button>
      {/if}
      {#if favList.length != 0}
      <button class="btn btn-outline-success btn-sm" on:click|preventDefault={() => {bashThis(favList)}}>Download bash favories</button>
      {/if}
    </div>
  </div>
  {#each favTab as item,i}
  {#if i== actualTab}
    <button class="pageBtn"  on:click|preventDefault>
      page {item.name} (actual page)
    </button>
    <button class="pageBtn" on:click|preventDefault={() => deletePage(i)}>X</button>
  {/if}
  {#if i!= actualTab}
    <button class="pageBtn" on:click|preventDefault={() => actualPage(i)}>
      page {item.name}
    </button>
    <button class="pageBtn" on:click|preventDefault={() => deletePage(i)}>X</button>
  {/if}
  {/each}
  </form>
</main>
