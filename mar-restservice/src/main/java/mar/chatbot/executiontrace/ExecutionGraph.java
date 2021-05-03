package mar.chatbot.executiontrace;

import java.util.HashSet;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;

import mar.chatbot.elements.ElementsSet;
import mar.chatbot.elements.IElement;
import mar.chatbot.elements.SetType;

public class ExecutionGraph {
	
	private final Graph<ExecutionNode,ExecutionEdge> graph;
	private int toAdd = 0;
	private final ExecutionNode initial;
	private ExecutionNode current;
	
	public ExecutionGraph() {
		graph = new SimpleDirectedGraph<ExecutionNode,ExecutionEdge>(ExecutionEdge.class);
		ExecutionNode initial = new ExecutionNode(toAdd, null);
		toAdd = toAdd + 1;
		graph.addVertex(initial);
		current = initial;
		this.initial = initial;
	}
	
	public void addStep(Map<String, Double> results, IElement query) {
		ExecutionNode node = new ExecutionNode(toAdd, results);
		ExecutionEdge edge = new ExecutionEdge(query);
		graph.addVertex(node);
		graph.addEdge(current, node, edge);
		current = node;
		toAdd = toAdd + 1;
	}
	
	public ElementsSet getGlobalQuery() {
		HashSet<IElement> set = new HashSet<IElement>();
		DijkstraShortestPath<ExecutionNode, ExecutionEdge> shpath = new DijkstraShortestPath<ExecutionNode, 
				ExecutionEdge>(graph);
		GraphPath<ExecutionNode, ExecutionEdge> gp = shpath.getPath(initial, current);
		for (ExecutionEdge e : gp.getEdgeList()) {
			set.add(e.getQuery());
		}
		return new ElementsSet(set, SetType.AND);
	}
	
	private static class ExecutionNode {
		private int id;
		private Map<String, Double> results;
		
		public ExecutionNode(int id, Map<String, Double> results) {
			super();
			this.id = id;
			this.results = results;
		}

		public int getId() {
			return id;
		}

		public Map<String, Double> getResults() {
			return results;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExecutionNode other = (ExecutionNode) obj;
			if (id != other.id)
				return false;
			return true;
		}
	}
	
	private static class ExecutionEdge {
		
		private IElement query;

		public ExecutionEdge(IElement query) {
			super();
			this.query = query;
		}

		public IElement getQuery() {
			return query;
		}		
	}
	
}
