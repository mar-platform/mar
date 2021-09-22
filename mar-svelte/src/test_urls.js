window.MAR = {
    toSearchURL: function(modelType, syntax) {
        return 'http://localhost:8080/search-full?type=' + modelType + '&syntax=' + syntax;
    },
    toTextSearchURL: function() {
        return 'http://localhost:8080/search-text';
    },
    toImageURL: function(modelId, type, pos = 0) {
        return 'http://localhost:8080/render/diagram?id=' + modelId + '&type=' + type + '&pos=' + pos;
    },
    toStatusURL: function() {
        return 'http://localhost:8080/status'
    }
};	