
<script>
	import { onMount } from 'svelte';
    let url = MAR.toStatusURL();
	
    let models = undefined;
	onMount(async () => {
		const res = await fetch(url);
		const status = await res.json();
        models = status.models
	});
</script>

<style>
    h4, p, li {
        color: whitesmoke;        
    }

    h4 {
        font-weight: bold;
    }
</style>

<h4>Stats</h4>
{#if models == undefined }
	<p>Loading...</p>
{:else if models.length == 0}
    <p>There is something weird going on... No models found :-(</p>
{:else}
    <ul>
    {#each models as m}
        <li>{m.name} - {m.count}</li>
    {/each}
    </ul>    
{/if}

<h4>Raw models</h4>
<p>
    The following packages contain the raw models crawled by MAR. It should be ok to use these models for research purposes (and even better if you cite our MAR papers!).
    You can also checkout <a href="http://modelset.github.io">ModelSet</a> which is a curated and labelled dataset of models.
</p>
<ul>    
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-github-ecore.tar.gz">Ecore - GitHub</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-atlanmod-ecore.tar.gz">Ecore - Atlanmod</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-genmymodel-ecore.tar.gz">Ecore - GenMyModel</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-genmymodel-archimate.tar.gz">Archimate - GenMyModel</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-github-archimate.tar.gz">Archimate - GitHub</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-genmymodel-bpmn2.tar.gz">BPMN2 - GenMyModel</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-genmymodel-rds.tar.gz">RDS - GenMyModel</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-genmymodel-uml.tar.gz">UML - GenMyModel</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-github-pnml.tar.gz">PNML - GitHub</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-github-sculptor.tar.gz">Sculptor - GitHub</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-github-uml.tar.gz">UML - GitHub</a></li>
    <li><a href="http://sanchezcuadrado.es/files/raw/repo-github-xtext.tar.gz">Xtext - GitHub</a></li>
    <!--
    <li><a href=" repo-simulink-emf.tar.gz">Simulink</a></li>
    -->
</ul>
