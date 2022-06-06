
<script>
  import { ItemHelper } from './../lib/common.js'
  export let item, facets //,compare

  let saveItem;
	let heightImg = 128;
	
	function handleMouseOver(e) {
		heightImg = 350;
	}
	function handleMouseOut(e) {
    heightImg = 128;
	}
  //on:mouseover={handleMouseOver} on:mouseout={handleMouseOut}
  function clickImg(e) {
    if(heightImg==128){
      heightImg=350;
    }
    else{
      heightImg = 128;
    }
	}
  function handleClick(elem,type) {
    if(type=="category"){
      facets.addCategory(elem);
    }
    if(type=="topic"){
      facets.addTopic(elem);
    }
    if(type=="origin"){
      facets.addOrigin(elem);
    }
    if(type=="model"){
      facets.addModelType(elem);
    }
    facets = facets;
    if(document.getElementById(elem.toString()).checked == false){
      document.getElementById(elem.toString()).checked = true;
    }
    else{
      document.getElementById(elem.toString()).checked = false;
    }
	}

</script>
{#if item.name!= undefined}
<main id="marElem"> 
    <div  class="list-group-item list-group-item-action flex-column align-items-start">
      <div class="row">
        {#if item.description == "true1"}
          <p> search number 1 :</p>
          <div class="col-0">
            <!-- svelte-ignore missing-declaration because MAR is defined in index.html -->
            <a href="https://cdn.pixabay.com/photo/2012/04/18/19/01/check-37583_960_720.png" target="_blank"><img  src="https://cdn.pixabay.com/photo/2012/04/18/19/01/check-37583_960_720.png" alt="Correct" width={heightImg} height={heightImg} ></a>
            
            <!--  <p><img  src="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" alt="See diagram" width={heightImg} height={heightImg} on:click|preventDefault={() => { clickImg();} }></p>
            <a href="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" target="_blank">Source</a>-->
          </div>
        {/if}
        {#if item.description == "false1"}
        <p> search number 1 :</p>
        <div class="col-0">
          <a href="https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png" target="_blank"><img  src="https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png" alt="False" width={heightImg} height={heightImg} ></a>
        </div>
        {/if}
        <div class="col">
          <!--{#if compare === false} -->
            <h6 class="mb-1"><a href="{item.url}" target="_blank">{item.name}</a></h6>
          <!--{:else}
            <h6 class="mb-1"><a href="{item.url}" target="_blank">{item.name.slice(0,10)}</a></h6>
          {/if}-->
          {#if item.description != "false1" && item.description != "false2" && item.description != "true2" && item.description != "true1"}
            <p class="mb-1"><small>{item.description}</small></p>
          {/if}
          <p class="align-items-right item-information-bar">
            {#if item.metadata } 
                {#if item.metadata.category.toString()!=""}
                  {#if document.getElementById(item.metadata.category.toString())!=undefined}
                    {#if document.getElementById(item.metadata.category.toString()).checked == false}
                      <button class="btn btn-dark btn-sm" on:click={handleClick(item.metadata.category,"category")}>{item.metadata.category}</button>
                    {:else}
                      <button class="btn btn-outline-success btn-sm" on:click={handleClick(item.metadata.category,"category")}>{item.metadata.category}</button>
                    {/if}
                  {/if}
              {/if}
              {#each item.metadata.topics as topic}
               <!-- We add a clickable button for the topic -->
               {#if topic.toString()!="" }
                {#if document.getElementById(topic.toString())!=undefined}
                    {#if document.getElementById(topic.toString()).checked == false}
                      <button class="btn btn-info btn-sm" on:click={handleClick(topic,"topic")}>{topic}</button>
                    {:else}
                      <button class="btn btn-outline-success btn-sm" on:click={handleClick(topic,"topic")}>{topic}</button>
                    {/if} 
                  {/if} 
                {/if} 
                <!--<span class="badge badge-light item-topic" >{topic}</span>-->
              {/each}
               <!-- We add a clickable button for the origin -->
               {#if item.origin.toString()!="" }
                {#if document.getElementById(item.origin.toString())!=undefined}
                    {#if document.getElementById(item.origin.toString()).checked == false}
                      <button class="btn btn-primary btn-sm" on:click={handleClick(item.origin,"origin")}>{item.origin}</button>
                    {:else}
                      <button class="btn btn-outline-success btn-sm" on:click={handleClick(item.origin,"origin")}>{item.origin}</button>
                    {/if} 
                  {/if} 
                {/if} 
               <!-- We add a clickable button for the Model -->
               {#if item.modelType.toString()!="" }
                {#if document.getElementById(item.modelType.toString())!=undefined}
                    {#if document.getElementById(item.modelType.toString()).checked == false}
                      <button class="btn btn-light btn-sm" on:click={handleClick(item.modelType,"model")}>{item.modelType}</button>
                    {:else}
                      <button class="btn btn-outline-success btn-sm" on:click={handleClick(item.modelType,"model")}>{item.modelType}</button>
                    {/if} 
                  {/if} 
                {/if} 
              <span class="badge badge-info">{item.metadata.numElements} elements</span>
              <span class="badge badge-warning">{ItemHelper.smellSize(item)} smells</span>
              <!-- <span class="badge badge-info"><img width="12" height="12" src="img/iconfinder_warning_61020.png" alt="Smells">{smell_size(item)}</span> -->
            {/if}              
          </p>  
        </div>
        <div class="col-0">
          <p> Score : {item.score.toFixed(2)}</p>
          <!-- svelte-ignore missing-declaration because MAR is defined in index.html -->
          {#if item.description != "false1" && item.description != "false2" && item.description != "true2" && item.description != "true1"}
              <a href="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" target="_blank"><img src="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" alt="See diagram" width={heightImg} height={heightImg} on:mouseover={handleMouseOver} on:mouseout={handleMouseOut}></a>
          {:else}
              <a href="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" target="_blank"><img  src="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" alt="See diagram" width={heightImg} height={heightImg}></a>
          {/if}
          <!--  <p><img  src="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" alt="See diagram" width={heightImg} height={heightImg} on:click|preventDefault={() => { clickImg();} }></p>
          <a href="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" target="_blank">Source</a>-->
        </div>
        {#if item.description == "true2"}
          <p> search number 2 :</p>
          <div class="col-0">
            <!-- svelte-ignore missing-declaration because MAR is defined in index.html -->
            <a href="https://cdn.pixabay.com/photo/2012/04/18/19/01/check-37583_960_720.png" target="_blank"><img  src="https://cdn.pixabay.com/photo/2012/04/18/19/01/check-37583_960_720.png" alt="Correct" width={heightImg} height={heightImg} ></a>
          </div>
        {/if}
        {#if item.description == "false2"}
        <p> search number 2 :</p>
        <div class="col-0">
          <!-- svelte-ignore missing-declaration because MAR is defined in index.html -->
          <a href="https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png" target="_blank"><img  src="https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png" alt="False" width={heightImg} height={heightImg} ></a>
        </div>
        {/if}
      </div>  

        <!-- here logo of where it comes -->
        
        <!-- URLHumanName -->
        
        <!-- <button type="button" class="btn btn-primary">
          <img src="img/iconfinder_warning_61020.png" alt="Smells"> <span class="badge badge-info">{smell_size(item)}</span>
          <span class="sr-only">Smells</span>
        </button> -->

      </div>
</main>
{/if}
<!--
{#if item.description == "true"}
<main id="marElem"> 
  <div  class="list-group-item list-group-item-action flex-column align-items-start">
    <div class="row">
      <div class="col">
        <p class="align-items-right item-information-bar">
         
        </p>  
      </div>
      <div class="col-0">
        <p> Score : 0</p>
        <!-- svelte-ignore missing-declaration because MAR is defined in index.html --><!--
        <a href="https://cdn.pixabay.com/photo/2012/04/18/19/01/check-37583_960_720.png" target="_blank"><img  src="https://cdn.pixabay.com/photo/2012/04/18/19/01/check-37583_960_720.png" alt="Correct" width={heightImg} height={heightImg} ></a>
        
        <!--  <p><img  src="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" alt="See diagram" width={heightImg} height={heightImg} on:click|preventDefault={() => { clickImg();} }></p>
        <a href="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" target="_blank">Source</a>-->
      <!--</div>
    </div>  
  </div>
</main>
{/if}

{#if item.description== "false"}
<main id="marElem"> 
  <div  class="list-group-item list-group-item-action flex-column align-items-start">
    <div class="row">
      <div class="col">
        <p class="align-items-right item-information-bar">
         
        </p>  
      </div>
      <div class="col-0">
        <p> Score : 0</p>
        <!-- svelte-ignore missing-declaration because MAR is defined in index.html --><!--
        <a href="https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png" target="_blank"><img  src="https://cdn.pixabay.com/photo/2017/02/12/21/29/false-2061132_960_720.png" alt="False" width={heightImg} height={heightImg} ></a>
        
        <!--  <p><img  src="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" alt="See diagram" width={heightImg} height={heightImg} on:click|preventDefault={() => { clickImg();} }></p>
        <a href="{MAR.toImageURL(item.id, ItemHelper.modelType(item))}" target="_blank">Source</a>-->
      <!--</div>
    </div>  
  </div>
</main>
{/if} -->
