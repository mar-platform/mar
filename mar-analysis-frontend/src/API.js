
const API = {
    labelPropagation : function() {
        return `http://localhost:8080/clustering/label-propagation`;
    },
    duplicationGraph : function() {
        return `http://localhost:8080/duplication-graph`;
    },
    megamodelGraph : function() {
        return `http://localhost:8080/megamodel-graph`;
    },
    interProjectGraph : function() {
        return `http://localhost:8080/interproject-graph`;
    },
    searchProject : function(value) {
        return `http://localhost:8080/search-project?value=${value}`;        
    },
    getProjects : function(value) {
        return `http://localhost:8080/all-projects`;        
    },    
    projectGraph : function(id) {
        return `http://localhost:8080/project-graph?projectId=${id}`;        
    },
    componentGraph : function() {
        return `http://localhost:8080/component-graph`;
    },
    graphFromSql : function(sql) {
        return `http://localhost:8080/graph-from-sql?sql=${sql}`;        
    },
    stats : function() {
        return `http://localhost:8080/stats`;
    },
}

export default API;