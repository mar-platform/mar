window.MAR = {
    toSearchURL: function(modelType, syntax) {
        return '/search-full?type=' + modelType + '&syntax=' + syntax;
    },
    toTextSearchURL: function() {
        return '/search-text';
    },
    toImageURL: function(modelId, type, pos = 0) {
        return '/render/diagram?id=' + modelId + '&type=' + type + '&pos=' + pos;
    }, 
    toStatusURL: function() {
        return '/status'
    }
};	

$.getScript("https://www.googletagmanager.com/gtag/js?id=G-JCTEZJPPM9", function() {
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());
  
    gtag('config', 'G-JCTEZJPPM9');      
});
