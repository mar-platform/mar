<!-- See https://github.com/sveltejs/svelte-repl/blob/master/src/CodeMirror.svelte -->


<script>
    import CodeMirror from './codemirror.js'
    import { onMount } from 'svelte';

    export let value

    let container;
    //var container = document.getElementById("codemirror-text-area");

	onMount(async () => {
        console.log(container)

        var myCodeMirror = CodeMirror.fromTextArea(container, {
            mode:  "javascript",
            lineWrapping: true,
            lineNumbers: true
        });

        // Propagate the changes explicitly because bind:value doesn't work both ways
        myCodeMirror.on("change", instance => {
            value = instance.getValue();
        });
    });
</script>



<!-- <textarea
bind:value
class="source"
style="min-width: 100%; height: 350px" /> -->

<div>
    <textarea bind:value id="codemirror-text-area" bind:this={container} />
</div>