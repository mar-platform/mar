package mar.neural.search.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.Graph;

import com.google.gson.Gson;

import mar.model2graph.AbstractModel2Graph;
import mar.model2graph.MetaFilter;
import mar.paths.ListofPaths;




public class GraphModel extends AbstractModel2Graph{

	Graph<Node, Edge> graph;
	List<String> tags;
	
	public GraphModel(Resource r,List<String> tags, MetaFilter mf) {
		this.withFilter(mf);
		graph = createParallelGraph(r);
		this.tags = tags;
	}
	
	public static class Graph2JsonV2 {
		private LinkedList<Integer> nodes;
		private LinkedList<Edge2Json> edges;
		private HashMap<Integer, String> nodeTypes;
		private HashMap<Integer, List<String>> nodeAttributes;
		private HashMap<Integer, List<String>> nodeTypeAttributes;
		private List<String> tags;
		
		
		
		public Graph2JsonV2(LinkedList<Integer> nodes, LinkedList<Edge2Json> edges, HashMap<Integer, String> nodeTypes,
				HashMap<Integer, List<String>> nodeAttributes, HashMap<Integer, List<String>> nodeTypeAttributes,
				List<String> tags) {
			super();
			this.nodes = nodes;
			this.edges = edges;
			this.nodeTypes = nodeTypes;
			this.nodeAttributes = nodeAttributes;
			this.setNodeTypeAttributes(nodeTypeAttributes);
			this.setTags(tags);
		}
		public LinkedList<Integer> getNodes() {
			return nodes;
		}
		public void setNodes(LinkedList<Integer> nodes) {
			this.nodes = nodes;
		}
		public LinkedList<Edge2Json> getEdges() {
			return edges;
		}
		public void setEdges(LinkedList<Edge2Json> edges) {
			this.edges = edges;
		}
		public HashMap<Integer, String> getNodeTypes() {
			return nodeTypes;
		}
		public void setNodeTypes(HashMap<Integer, String> nodeTypes) {
			this.nodeTypes = nodeTypes;
		}
		public HashMap<Integer, List<String>> getNodeAttributes() {
			return nodeAttributes;
		}
		public void setNodeAttributes(HashMap<Integer, List<String>> nodeAttributes) {
			this.nodeAttributes = nodeAttributes;
		}
		public HashMap<Integer, List<String>> getNodeTypeAttributes() {
			return nodeTypeAttributes;
		}
		public void setNodeTypeAttributes(HashMap<Integer, List<String>> nodeTypeAttributes) {
			this.nodeTypeAttributes = nodeTypeAttributes;
		}
		public List<String> getTags() {
			return tags;
		}
		public void setTags(List<String> tags) {
			this.tags = tags;
		}
		
		
	}
	
	protected static class Edge2Json {
		private int source;
		private int target;
		private String name;
		
		public Edge2Json(int source, int target, String name) {
			super();
			this.source = source;
			this.target = target;
			this.name = name;
		}

		public int getSource() {
			return source;
		}

		public void setSource(int source) {
			this.source = source;
		}

		public int getTarget() {
			return target;
		}

		public void setTarget(int target) {
			this.target = target;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
	
	public String generateJson() {
		Gson gson = new Gson();
		return gson.toJson(generateGraph2JsonV2(graph));
		
	}
	
	private static List<Node> getAttributes(Node n, Graph<Node, Edge> g) {
		List<Node> ln = new LinkedList<>();
		if (n.isTerminal())
			return ln;
		for (Edge edge : g.edgesOf(n)) {
			Node n2 = g.getEdgeTarget(edge);

			if (n2.isTerminal())
				ln.add(n2);

		}
		
		return ln;
		
	}
	
	
	public Graph2JsonV2 generateGraph2JsonV2(Graph<Node, Edge> graph) {
		LinkedList<Integer> nodes = new LinkedList<>();
		LinkedList<Edge2Json> edges = new LinkedList<Edge2Json>();
		HashMap<Integer, String> nodeTypes = new HashMap<>();
		HashMap<Integer, List<String>> nodeAttributes = new HashMap<Integer, List<String>>();
		HashMap<Integer, List<String>> nodeTypeAttributes = new HashMap<Integer, List<String>>();
		
		HashMap<Node,Integer> mapNode = new HashMap<Node, Integer>();
		
		//list of ignores nodes EObject, etc. No attributes nodes
		LinkedList<Node> ignore = new LinkedList<>();
		
		int i = 0;
		for (Node n : graph.vertexSet()) {
			if (n.isTerminal())
				continue; 
			if (getAttributes(n,graph).isEmpty()) {
				ignore.add(n);
				continue;
			}
			
			nodes.add(i);
			
			nodeTypes.put(i, n.toString());
			
			List<Node> la = getAttributes(n,graph);
			la.sort((n1,n2)->{
				String s1 = graph.edgesOf(n1).iterator().next().getLabel();
				String s2 = graph.edgesOf(n2).iterator().next().getLabel();
				return s1.compareTo(s2);
			});
			
			List<String> latt = la.stream().map(f -> f.toString()).collect(Collectors.toList());
			List<String> attrTypes = la.stream().map(f -> graph.edgesOf(f).iterator().next().getLabel()).collect(Collectors.toList());
			
			nodeAttributes.put(i, latt);
			nodeTypeAttributes.put(i, attrTypes);
			
			mapNode.put(n, i);
			i = i + 1;
		}
		
		for (Node n : graph.vertexSet()) {
			if (n.isTerminal() || ignore.contains(n))
				continue;
			
			
			for (Edge e : graph.edgesOf(n)) {
				if (!graph.getEdgeSource(e).equals(n))
					continue;
				if (graph.getEdgeTarget(e).isTerminal())
					continue;
				if (ignore.contains(graph.getEdgeTarget(e)))
					continue;
				

				Edge2Json e2j = new Edge2Json(mapNode.get(n), mapNode.get(graph.getEdgeTarget(e)), e.getLabel());
				edges.add(e2j);
			}
		}
		
		return new Graph2JsonV2(nodes, edges, nodeTypes, nodeAttributes, nodeTypeAttributes, this.tags);
		
		
	}
	
	public ListofPaths getListOfPaths(Resource r) {
		// TODO mar-main change classes
		return null;
	}

}
