package mar.model2graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;

import mar.paths.ListofPaths;
import mar.paths.NodeType;
import mar.paths.Path;
import mar.paths.PathNode;

public class Model2GraphAllpaths extends AbstractModel2Graph {
	
	@Nonnull
	private int defaultMaxLength;
	@Nonnull
	private Function<Integer, Integer> nodeSizeToMaxLength = (v) -> defaultMaxLength;
	
	public Model2GraphAllpaths(int defaultMaxLength) {
		this.defaultMaxLength = defaultMaxLength;
	}
	
	public Model2GraphAllpaths(int defaultMaxLength, Function<Integer, Integer> nodeSizeToMaxLength) {
		this(defaultMaxLength);
		this.nodeSizeToMaxLength = nodeSizeToMaxLength;
	}
	
	protected int getMaxLength(@Nonnull Graph<Node, Edge> g) {
		Integer v = nodeSizeToMaxLength.apply(g.vertexSet().size());
		return v == null ? defaultMaxLength : v;
	}
	
	@Override
	public ListofPaths getListOfPaths(Resource r) {		
		Graph<Node, Edge> g = createParallelGraph(r);
		Set<Node> nodes = g.vertexSet();
		Set<Node> node2paths = new HashSet<Node>();
		AllDirectedPaths<Node,Edge> adp = new AllDirectedPaths<Node,Edge>(g);
		for (Node node : nodes) {
			if (node.isTerminal() || noAttributesNode(node, g))
				node2paths.add(node);
		}
		List<GraphPath<Node, Edge>> gprahs = adp.getAllPaths(node2paths, node2paths, true, getMaxLength(g));
		List<Path> lps = new LinkedList<Path>();
		
		for (GraphPath<Node, Edge> graphPath : gprahs) {
			
			if(graphPath.getEdgeList().size() == 0) {
				Node node = graphPath.getStartVertex();
				if (node.isTerminal()) {
					// path to his class
					Set<Edge> edges_n1 = g.edgesOf(node);
					for (Edge edge : edges_n1) {
						if (g.getEdgeSource(edge).equals(node)) {
							PathNode pn1 = null;
							if (node.getElement() instanceof String)
								pn1 = new PathNode(node.toString(),NodeType.ATTRIBUTE_VALUE_STRING);
							else
								pn1 = new PathNode(node.toString(),NodeType.ATTRIBUTE_VALUE_OTHER);
							
							PathNode pn2 = new PathNode(edge.getLabel(), NodeType.ATTRIBUTE);
							PathNode pn3 = new PathNode(g.getEdgeTarget(edge).toString(), NodeType.CLASS);
							List<PathNode> lspn = new LinkedList<PathNode>();
							lspn.add(pn1);lspn.add(pn2);lspn.add(pn3);
							Path p = new Path(lspn);
							lps.add(p);
						}
					}	
				} else {
					PathNode pn1 = new PathNode(node.toString(),NodeType.CLASS);
					List<PathNode> lspn = new LinkedList<PathNode>();
					lspn.add(pn1);
					Path p = new Path(lspn);
					lps.add(p);		
				}
			} else {
				
				if (graphPath.getEndVertex().equals(graphPath.getStartVertex()))
					continue;
				
				//A<->O<->B, ignore A -> B or B -> A
				if(graphPath.getEdgeList().size() == 2 
						&& graphPath.getStartVertex().isTerminal()
						&& graphPath.getEndVertex().isTerminal()) {
					
					if (!(graphPath.getStartVertex().getElement() instanceof String) 
							&& graphPath.getEndVertex().getElement() instanceof String)
						continue;
					
					if ((graphPath.getStartVertex().getElement() instanceof String) 
							&& (graphPath.getEndVertex().getElement() instanceof String)) {
						//compare
						String s1 = graphPath.getStartVertex().toString();
						String e1 = graphPath.getEdgeList().get(0).getLabel();
						String s2 = graphPath.getStartVertex().toString();
						String e2 = graphPath.getEdgeList().get(1).getLabel();
						
						if (s1.compareTo(s2)>0)
							continue;
						if (s1.compareTo(s2)==0 && e1.compareTo(e2)>0)
							continue;
						
					}
					
					if (!(graphPath.getStartVertex().getElement() instanceof String) 
							&& !(graphPath.getEndVertex().getElement() instanceof String)) {
						//compare
						String s1 = graphPath.getStartVertex().toString();
						String e1 = graphPath.getEdgeList().get(0).getLabel();
						String s2 = graphPath.getStartVertex().toString();
						String e2 = graphPath.getEdgeList().get(1).getLabel();
						
						if (s1.compareTo(s2)>0)
							continue;
						if (s1.compareTo(s2)==0 && e1.compareTo(e2)>0)
							continue;
					}
	
				}
				
				//normal path
				
				List<PathNode> lspn = new LinkedList<PathNode>();
				PathNode lastnode = null;
				for (Edge edge : graphPath.getEdgeList()) {
					PathNode node1 = null;
					PathNode node2 = null;
					PathNode node3 = null;
					if (g.getEdgeSource(edge).isTerminal()) {
						if (g.getEdgeSource(edge).getElement() instanceof String)
							node1 = new PathNode(g.getEdgeSource(edge).toString(), NodeType.ATTRIBUTE_VALUE_STRING);
						else
							node1 = new PathNode(g.getEdgeSource(edge).toString(), NodeType.ATTRIBUTE_VALUE_OTHER);
						node3 = new PathNode(edge.getLabel(), NodeType.ATTRIBUTE);
						node2 = new PathNode(g.getEdgeTarget(edge).toString(), NodeType.CLASS);
					} else if (g.getEdgeTarget(edge).isTerminal()) {
						node1 = new PathNode(g.getEdgeSource(edge).toString(), NodeType.CLASS);
						node3 = new PathNode(edge.getLabel(), NodeType.ATTRIBUTE);
						
						if (g.getEdgeTarget(edge).getElement() instanceof String)
							node2 = new PathNode(g.getEdgeTarget(edge).toString(), NodeType.ATTRIBUTE_VALUE_STRING);
						else
							node2 = new PathNode(g.getEdgeTarget(edge).toString(), NodeType.ATTRIBUTE_VALUE_OTHER);
					} else {
						node1 = new PathNode(g.getEdgeSource(edge).toString(), NodeType.CLASS);
						node3 = new PathNode(edge.getLabel(), NodeType.REFERENCE);
						node2 = new PathNode(g.getEdgeTarget(edge).toString(), NodeType.CLASS);
					}
					
					lspn.add(node1);
					lspn.add(node3);
					lastnode = node2;
				}
				
				lspn.add(lastnode);
				Path p = new Path(lspn);
				lps.add(p);
				
			}
		}
		
		return newPathSet(lps);
	}
}
