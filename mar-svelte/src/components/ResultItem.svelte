
<script>
  import { ItemHelper } from './../lib/common.js'
  
  export let item  
</script>

<main>
    <div class="list-group-item list-group-item-action flex-column align-items-start">
      <div class="row">
        <div class="col">
          <h6 class="mb-1"><a href="{item.url}" target="_blank">{item.name}</a></h6>
          <p class="mb-1"><small>{item.description}</small></p>
          <p class="align-items-right item-information-bar">

            {#if item.metadata.category}
              <span class="badge badge-light item-category">{item.metadata.category}</span>
            {/if}
            {#each item.metadata.topics as topic}
              <span class="badge badge-light item-topic">{topic}</span>
            {/each}
            <span class="badge badge-info">{item.metadata.numElements} elements</span>
            <span class="badge badge-warning">{ItemHelper.smellSize(item)} smells</span>
            <!-- <span class="badge badge-info"><img width="12" height="12" src="img/iconfinder_warning_61020.png" alt="Smells">{smell_size(item)}</span> -->
          </p>  
        </div>
        <div class="col-0">
          <p><small>{item.score.toFixed(2)}</small></p>
          <!-- svelte-ignore missing-declaration because MAR is defined in index.html -->
          <p><a href="{MAR.toImageURL(item.id)}"><img src="img/iconfinder_Artboard_37_3952964.png" alt="See diagram" width=24 height=24></a></p>
          
        </div>
      </div>  

        <!-- here logo of where it comes -->
        
        <!-- URLHumanName -->
        
        <!-- <button type="button" class="btn btn-primary">
          <img src="img/iconfinder_warning_61020.png" alt="Smells"> <span class="badge badge-info">{smell_size(item)}</span>
          <span class="sr-only">Smells</span>
        </button> -->

      </div>
</main>
