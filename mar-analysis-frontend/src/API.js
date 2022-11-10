
const API = {
    labelPropagation : function() {
        return `http://localhost:8080/clustering/label-propagation`;
    },
    duplicationGraph : function() {
        return `http://localhost:8080/duplication-graph`;
    },
    searchProject : function(value) {
        return `http://localhost:8080/search-project?value=${value}`;        
    },
    projectGraph : function(id) {
        return `http://localhost:8080/project-graph?projectId=${id}`;        
    },
    graphFromSql : function(sql) {
        return `http://localhost:8080/graph-from-sql?sql=${sql}`;        
    }
}

export default API;