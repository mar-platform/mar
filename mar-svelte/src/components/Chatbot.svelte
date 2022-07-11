<script>
    export let results = [];

    let messageInput;
    let messages = []

    const MessageType = {
	    HUMAN: "human",
	    BOT: "bot",
	}


    async function handleSubmit(event) {
        const input = messageInput.trim();
        if (input.length == 0)
            return;
            

        messages = [...messages, new Message(MessageType.HUMAN, input)];
        messageInput = "";

        let url = MAR.toChatbotConversationURL();
        const res = await fetch(url, {
                method: "POST",
                body: input,
        });			
            
        const answer = await res.json();
        let message = null;
        let key = null;
        console.log(answer)
        switch (answer.type) {
            case "message":
                message = answer.message;
                key = answer.key;
                break;
            case "result_list":
                message = answer.message;
                key = answer.key;
                results = answer.items;
                break;
            default:
                message = "Something weird happened internally. Please try again."
                break;
        }

        if (message != null)
            messages = [...messages, new Message(MessageType.BOT, message)];

        if (key!= null){
            url = url +"&key="+key
            const res = await fetch(url, {
                method: "POST",
                body: input,
        });	
        const answer2 = await res.json();
        console.log(answer2)
        }
    }

    class Message {
        constructor(type, text) {
            this.type = type;
            this.text = text;
        }
    }
</script>

<style>
    .message-box {
        min-height: 200px;
        max-height: 200px;
        background-color: whitesmoke;
        margin-bottom: 10px;
        overflow: auto;
    }

    .bot-message {
        text-align: right;
        background-color: turquoise;
    }


    .human-message {
        margin-left: auto;
        margin-right: 0;
        background-color: cadetblue;
    }
</style>

<div class="container">
    <form
        action="#xxx"
        on:submit|preventDefault={handleSubmit}
        enctype="multipart/form-data"
        autocomplete="off"
    >
        <div class="row">
            <!-- I don't why I have to add the width: 100% here, but... -->
            <div
                class="alert alert-info"
                style="width: 100%; padding: 4px; padding-left: 8px"
                role="alert"
            >
                Write to the bot!
            </div>
        </div>

        <div class="row">
            <div class="container message-box">
                {#each messages as message}
                    <div class="row">
                        <div class="{message.type}-message">{message.text}</div>
                    </div>
                {/each}
            </div>
        </div>

        <div class="row">
            <div class="input-group mb-3">
                <input
                    bind:value={messageInput}
                    type="text"
                    id="messageInput"
                    class="form-control"
                    placeholder="Tell me what you need"
                    aria-label="Relevant keywords for your model"
                />
                <input class="btn btn-secondary" type="submit" value="Submit!" />
            </div>
        </div>
    </form>
</div>
