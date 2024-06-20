
const API = {
    labelPropagation : function() {
        return `http://localhost:8080/clustering/label-propagation`;
    },
    duplicationGraph : function() {
        return `http://localhost:8080/duplication-graph`;
    },
    interProjectGraph : function() {
        return `http://localhost:8080/interproject-graph`;
    },
    searchProject : function(value) {
        return `http://localhost:8080/search-project?value=${value}`;        
    },
    projectGraph : function(id) {
        return `http://localhost:8080/project-graph?projectId=${id}`;        
    },
    graphFromSql : function(sql) {
        return `http://localhost:8080/graph-from-sql?sql=${sql}`;        
    },
    stats : function() {
        return `http://localhost:8080/stats`;
    },
}

export default API;