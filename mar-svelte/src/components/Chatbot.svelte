<script>
    let message = 'Say hi!';
    let conversation = [];
    let sessionId = null;

    class ConversationItem {
        constructor(who, text) {
            this.who = who;
            this.text = text;
        }
    }

    async function handleSubmit(event) {
        const item = new ConversationItem('user', message);
        const writtenMsg = message;
        const url  = MAR.toConversationURL(sessionId);

        conversation = [...conversation, item];
        message = "";
        /*
        const res = await fetch(url, {
            method: "POST",
            body: writtenMsg,
        });

        const json = await res.json();
        sessionId = json.sessionId;
        item = new ConversationItem('bot', json.message);
        conversation = [...conversation, item];
        */
    }
</script>

<div class="bot-conversation">
    {#each conversation as item}
        <div>{item.text}</div>
    {/each}
</div>
<form
    action="#xxx"
    on:submit|preventDefault={handleSubmit}
    enctype="multipart/form-data"
>
    <input type="text" bind:value="{message}" />
    <input class="btn btn-secondary" type="submit" value="Submit" />
</form>

<style>
    .bot-conversation {
        min-height: 200px;
        background-color: azure;
    }
</style>
