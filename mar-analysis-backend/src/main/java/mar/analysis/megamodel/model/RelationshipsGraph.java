package mar.analysis.megamodel.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Preconditions;

public class RelationshipsGraph {
	
	private Graph<Node, Edge> impl;
	private Map<String, Node> idToNode = new HashMap<>();

	public RelationshipsGraph() {
		impl = new DefaultDirectedGraph<>(Edge.class);	
	}
	
	public void addNode(@Nonnull Node node) {
		Preconditions.checkNotNull(node);
		impl.addVertex(node);
		idToNode.put(node.id, node);
	}

	public Node getNode(String id) {
		return Preconditions.checkNotNull(idToNode.get(id));
	}
	
	public void addEdge(String src, String tgt, Relationship type) {
		Node srcNode = getNode(src);
		Node tgtNode = getNode(tgt);
		impl.addEdge(srcNode, tgtNode, new Edge(type));
	}	
	
	@JsonProperty(value = "nodes")
	public Collection<? extends Node> getNodes() {
		return idToNode.values();
	}
	
	@JsonProperty(value = "edges")
	public Collection<? extends Edge> getEdges() {
		return impl.edgeSet();
	}
	
	@JsonTypeName("node")
	public static class Node {

		@Nonnull
		@JsonProperty
		private final String id;
		@Nonnull
		@JsonProperty
		private final Artefact artefact;
		@CheckForNull
		private Object element;

		public Node(@Nonnull String id, @Nonnull Artefact artefact) {
			this.id = Preconditions.checkNotNull(id);
			this.artefact = artefact;
		}
		
		public Node(@Nonnull String id, @Nonnull Artefact artefact, Object element) {
			this(id, artefact);
			this.element = element;
		}
		
		@Nonnull
		public Artefact getArtefact() {
			return artefact;
		}
		
		@CheckForNull
		public Object getElement() {
			return element;
		}
	}
	
	@JsonTypeName("edge")
	public static class Edge extends DefaultEdge {
		private static final long serialVersionUID = -4014898730169220426L;
				
		@Nonnull
		@JsonProperty
		private Relationship type;

		public Edge(Relationship type) {
			this.type = type;
		}
		
		@JsonProperty(value = "source")
		public String getSourceId() {
			return ((Node) super.getSource()).id;
		}
		
		@JsonProperty(value = "target")
		public String getTargetId() {
			return ((Node) super.getTarget()).id;
		}
		
		@JsonProperty(value = "type")
		public String getTypeKind() {
			return type.getKind();
		}
		
		
		@Nonnull
		public Relationship getType() {
			return type;
		}

	}


}
