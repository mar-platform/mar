package ml2.mar.webserver.controllers;

import java.util.List;

import mar.analysis.megamodel.model.ComponentGraph;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.backend.megamodel.RawRepositoryDB.RawFile;
import mar.analysis.backend.megamodel.RawRepositoryDB.RawProject;
import mar.analysis.backend.megamodel.stats.CombinedStats;
import mar.analysis.megamodel.model.RelationshipsGraph;

@RestController
@RequestMapping(path = "/modelgraph")
public class MegamodelController {

	private MegamodelDB db;
	private RawRepositoryDB raw;
	private TransformationRelationshipsAnalysis analysis;
	private ObjectMapper objectMapper;

	private ComponentGraph componentGraphCache = null;
	private RelationshipsGraph megamodelCache = null;
	private RelationshipsGraph interProjectGraphCache = null;

	private List<String> allProjectsCache = null;
	private List<String> allComponentsCache = null;

	public MegamodelController(TransformationRelationshipsAnalysis analysis, RawRepositoryDB raw, MegamodelDB db, ObjectMapper objectMapper) {
		this.analysis = analysis;
		this.raw = raw;
		this.db = db;
		this.objectMapper = objectMapper;

		// Cache the graphs on startup to avoid expensive recomputation on each request
		componentGraphCache = analysis.getComponentGraph();
		megamodelCache = analysis.getMegamodelGraph();
		interProjectGraphCache = analysis.getInterProjectGraph();

		allProjectsCache = db.allProjects().stream().sorted((p1, p2) -> p1.getId().compareTo(p2.getId())).map(p -> p.getId()).toList();
		allComponentsCache = componentGraphCache.getSubgraphs().stream().map(c -> c.getName()).toList();
	}

	@GetMapping(value = "/stats", produces="application/json")
    public String stats() throws JsonProcessingException {
        return objectMapper.writeValueAsString(new CombinedStats(raw.getStats(), db.getStats()));
    }

	@GetMapping(value = "/artefacts", produces="application/json")
    public ResponseEntity<RawFile> getArtefactInfo(@RequestParam("q") String artefactId) throws JsonProcessingException {
        RawFile artefact = raw.getArtefactInfo(artefactId);
        if (artefact == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(artefact);
    }

	@GetMapping(value = "/projects", produces="application/json")
    public ResponseEntity<RawProject> getProjectInfo(@RequestParam("q") String projectId) throws JsonProcessingException {
        RawProject project = raw.getProjectInfo(projectId);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }

	@GetMapping(value = "/graph", produces="application/json")
    public String index() throws JsonProcessingException {
        return objectMapper.writeValueAsString(analysis.getRelationships());    	
    }

	/*@GetMapping(value = "/duplication-graph", produces="application/json")
    public String duplicationGraph() throws JsonProcessingException {
        return objectMapper.writeValueAsString(analysis.getDuplicationGraph());
    }*/

	@GetMapping(value = "/megamodel-graph", produces="application/json")
    public String megamodelGraph() throws JsonProcessingException {
        return objectMapper.writeValueAsString(megamodelCache);
    }
	
	@GetMapping(value = "/interproject-graph", produces="application/json")
    public String interProjectGraph() throws JsonProcessingException {
        return objectMapper.writeValueAsString(interProjectGraphCache);
    }

	@GetMapping(value = "/all-components", produces="application/json")
	public List<String> allComponents() throws JsonProcessingException {
		return allComponentsCache;
	}

	@GetMapping(value = "/component-graph", produces="application/json")
    public ResponseEntity<RelationshipsGraph> componentGraph(@RequestParam("componentId") String componentId) throws JsonProcessingException {
		var componentGraph = componentGraphCache.getSubgraphs().stream().filter(s -> s.getName().equals(componentId)).findFirst().orElse(null);
		if (componentGraph == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(componentGraph);
    }
	
	@GetMapping(value = "/project-graph", produces="application/json")
	public RelationshipsGraph getProjectGraph(@RequestParam("projectId") String projectId) {
		return analysis.getProjectRelationship(projectId);
	}

	@GetMapping(value = "/all-projects", produces="application/json")
    public List<String> allProjects() throws JsonProcessingException {
		return allProjectsCache;
	}

	/*@GetMapping(value = "/graph-from-sql", produces="application/json")
	public RelationshipsGraph getGraphFromSQL(@RequestParam("sql") String sql) {
		return analysis.getRelationshipsFromSQL(sql);
	}*/
	
	/*@GetMapping(value = "/search-project", produces="application/json")
    public List<Project> searchProject(@RequestParam("value") String value) throws JsonProcessingException {
		if (value.length() < 3)
			return Collections.emptyList();
		return db.searchProjects(value);
	}*/
	
	/*@GetMapping(value = "/clustering/label-propagation", produces="application/json")
    public String clusteringLabelPropagation() throws JsonProcessingException {
        Graph<Node, Edge> graph = analysis.getRelationships().getGraph();
        Graph<Node, Edge> undirected = toUndirected(graph);
        
        List<List<Artefact>> results = new ArrayList<>();
        
        StringBuffer buffer = new StringBuffer();
		LabelPropagationClustering<Node, Edge> clustering = new LabelPropagationClustering<>(undirected);
		for (Set<Node> set : clustering.getClustering().getClusters()) {
			List<Artefact> cluster = new ArrayList<Artefact>();
			results.add(cluster);
			buffer.append("Cluster: \n");
			for (Node node : set) {
				if (node instanceof RelationshipsGraph.ArtefactNode) {
					ArtefactNode an = (RelationshipsGraph.ArtefactNode) node;
					buffer.append("    " + an.getArtefact().getId() + "\n");
					cluster.add(an.getArtefact());
				}
			}
		}
        
		System.out.println(buffer.toString());
		return objectMapper.writeValueAsString(results);  	
    }

	private Graph<Node, Edge> toUndirected(Graph<Node, Edge> graph) {
		Graph<Node, Edge> undirected = new DefaultUndirectedGraph<>(Edge.class);
		for (Node node : graph.vertexSet()) {
			undirected.addVertex(node);
		}
		for (Edge edge : graph.edgeSet()) {
			undirected.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
		}
		return undirected;
	}*/
	
}
