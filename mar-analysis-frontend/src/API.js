
const API = {
    labelPropagation : function() {
        return `http://localhost:8080/clustering/label-propagation`;
    },
    searchProject : function(value) {
        return `http://localhost:8080/search-project?value=${value}`;        
    }
}

export default API;