package ml2.mar.webserver.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Project;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.ArtefactNode;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

@RestController
public class MegamodelController {

	@Autowired
	private MegamodelDB db;
	@Autowired
	private TransformationRelationshipsAnalysis analysis;
	@Autowired
	private ObjectMapper objectMapper;

	@GetMapping(value = "/graph", produces="application/json")
	@CrossOrigin(origins = "http://localhost:3000")
    public String index() throws JsonProcessingException {
        return objectMapper.writeValueAsString(analysis.getRelationships());    	
    }

	@GetMapping(value = "/search-project", produces="application/json")
	@CrossOrigin(origins = "http://localhost:3000")
    public List<Project> searchProject(@RequestParam String value) throws JsonProcessingException {
		if (value.length() < 3)
			return Collections.emptyList();
		return db.searchProjects(value);
	}
	
	@GetMapping(value = "/clustering/label-propagation", produces="application/json")
	@CrossOrigin(origins = "http://localhost:3000")
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
	}
	
}
