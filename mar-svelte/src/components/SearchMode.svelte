<script>
    import { text } from "svelte/internal";

    export var selected = 'text';

    function buttonStyle(selected, type) {
        var active = (selected == type) ? 'active' : '';
        return `btn btn-secondary mr-2 ${active}`
    }

    let textStyle, syntaxStyle, uploadStyle, chatbotStyle

    $: textStyle    = buttonStyle(selected, 'text');
    $: exampleStyle = buttonStyle(selected, 'example');
    $: chatbotStyle = buttonStyle(selected, 'chatbot');
</script>

<main>
    <div class="alert alert-info" style="padding: 4px; padding-left: 8px"  role="alert">
        Select a search mode
    </div>
    <div class="btn-group btn-group-sm" role="group" aria-label="Basic example">
        <!-- <span class="badge badge-light">Search mode</span>			 -->
        <button type="button" class="{textStyle}"    on:click={() => selected = 'text'}>Text search</button>
        <button type="button" class="{exampleStyle}" on:click={() => selected = 'example'}>By-example</button>
        <button type="button" class="{chatbotStyle}" on:click={() => selected = 'chatbot'}>Chatbot</button>				
    </div>
</main>