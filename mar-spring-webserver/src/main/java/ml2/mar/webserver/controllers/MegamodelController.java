package ml2.mar.webserver.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

@RestController
public class MegamodelController {

	@Autowired
	private TransformationRelationshipsAnalysis analysis;
	@Autowired
	private ObjectMapper objectMapper;

	@GetMapping(value = "/graph", produces="application/json")
	@CrossOrigin(origins = "http://localhost:3000")
    public String index() throws JsonProcessingException {
        return objectMapper.writeValueAsString(analysis.getRelationships());    	
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
				buffer.append("    " + node.getArtefact().getId() + "\n");
				cluster.add(node.getArtefact());
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
