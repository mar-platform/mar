package mar.analysis.megamodel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.connectivity.ConnectivityInspector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class ComponentGraph {

	@JsonProperty
	private List<SingleComponentGraph> subgraphs = new ArrayList<SingleComponentGraph>();
	
	public ComponentGraph(RelationshipsGraph graph) {
		ConnectivityInspector<Node, Edge> inspector = new ConnectivityInspector<Node, Edge>(graph.getGraph());
		List<Set<Node>> connectedSets = inspector.connectedSets();

		for (Set<Node> set : connectedSets) {
			String proposedName = set.iterator().next().getId();
			
			SingleComponentGraph subgraph = new SingleComponentGraph(proposedName, set.size());			
			List<Edge> toBeAdded = new ArrayList<RelationshipsGraph.Edge>();
			for (Node node : set) {
				subgraph.addNode(node);
				toBeAdded.addAll(graph.getGraph().incomingEdgesOf(node));
			}
			
			for (Edge edge : toBeAdded) {
				subgraph.addEdge(edge.getSourceId(), edge.getTargetId(), edge.getTypes());
			}
			
			subgraphs.add(subgraph);
		}			

		subgraphs.sort((c1, c2) -> -1 * Integer.compare(c1.nodeCount, c2.nodeCount));;
	}
	
	
	public List<SingleComponentGraph> getSubgraphs() {
		return subgraphs;
	}
	
	public static class SingleComponentGraph extends RelationshipsGraph {

		@JsonProperty("name")
		private String name;
		@JsonProperty("nodeCount")
		private int nodeCount;

		public SingleComponentGraph(String proposedName, int nodeCount) {
			this.name = proposedName;
			this.nodeCount = nodeCount;
		}
		
	}

}
