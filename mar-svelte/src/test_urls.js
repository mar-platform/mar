window.MAR = {
    toSearchURL: function(modelType, syntax) {
        return 'http://localhost:8080/search-full?type=' + modelType + '&syntax=' + syntax;
    },
    toTextSearchURL: function() {
        return 'http://localhost:8080/search-text';
    },
    toImageURL: function(modelId) {
        return 'http://localhost:8080/render?id=' + modelId;
    },
    toStatusURL: function() {
        return 'http://localhost:8080/status'
    },
    toConversationURL: function(sessionId) {
        const postfix = sessionId == null ? '' : '?sessionId=' + sessionId;
        return 'http://localhost:8080/v1/chatbot/conversation' + postfix;
    }    
};	