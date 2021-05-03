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
};	