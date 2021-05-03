<script>
    import { Stretch } from "svelte-loading-spinners";

    /* Interface */
    export let results = [];
    export let modelType = 'ecore';

    let loading = false;
    function notifyLoading(isLoading) {
        loading = isLoading;
    }

    /*
        // import Dropzone from "svelte-file-dropzone";

    import "../lib/dropzone.js";
    // https://www.dropzonejs.com
    //    var myDropzone = new Dropzone("div#dropzone", { url: "/file/post"});

    const jq = window.$;
    jq("div#dropzone").dropzone({ url: "/file/post" });

    let files = {
        accepted: [],
        rejected: [],
    };

    function handleFilesSelect(e) {
        console.log(e);
        const { acceptedFiles, fileRejections } = e.detail;
        files.accepted = [...files.accepted, ...acceptedFiles];
        files.rejected = [...files.rejected, ...fileRejections];
    }*/

    // Inspired by the explanation in https://dev.to/brunooliveira/uploading-a-file-svelte-form-and-springboot-backend-18m6
    let files

    async function handleSubmit(event) {       
        let syntax = "xmi";
        let url = MAR.toSearchURL(modelType, syntax);

        const formData = new FormData();
        formData.append('uploaded_file', files[0]);

        notifyLoading(true)
        const res = await fetch(url, {
            method: "POST",
            body: formData
        });

        console.log(res);
        const json = await res.json();
        notifyLoading(false)
        results = json;
    }
</script>

<main>
    <div
        class="alert alert-info"
        style="padding: 4px; padding-left: 8px"
        role="alert"
    >
        Upload an XMI file
    </div>
    <form
        action="#xxx"
        on:submit|preventDefault={handleSubmit}
        enctype="multipart/form-data"
    >
        <div class="col">
            <!---
            <Dropzone on:drop={handleFilesSelect} accept={['*']}>
                <p>Drop your model here</p>
            </Dropzone>
        -->
            <!-- <div id="dropzone"></div> -->
            <input type="file" id="modelFiles" name="filename" bind:files />
        </div>
        <div style="text-align: right; margin-top: 10px">
            {#if loading}
                <Stretch size="40" color="#FF3E00" unit="px" />
            {:else}
                <input
                    class="btn btn-secondary"
                    type="submit"
                    value="Submit!"
                />
            {/if}
        </div>
    </form>
</main>
