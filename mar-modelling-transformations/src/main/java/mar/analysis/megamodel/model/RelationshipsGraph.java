package mar.analysis.megamodel.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Preconditions;

public class RelationshipsGraph {
	
	private Set<Project> projects = new HashSet<>();
	private Graph<Node, Edge> impl;
	private Map<String, Node> idToNode = new HashMap<>();

	public RelationshipsGraph() {
		impl = new DefaultDirectedGraph<>(Edge.class);	
	}
	
	public void addProject(@Nonnull Project project) {
		this.projects.add(project);
	}
	
	public Set<? extends Project> getProjects() {
		return projects;
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
	
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, 
        include = As.PROPERTY, 
        property = "_type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ArtefactNode.class, name = "artefact"),
        @JsonSubTypes.Type(value = VirtualNode.class, name = "virtual"),
    })
	public static abstract class Node {

		@Nonnull
		@JsonProperty
		private final String id;

		public Node(@Nonnull String id) {
			this.id = Preconditions.checkNotNull(id);
		}
		
		public String getId() {
			return id;
		}
	}

	@JsonTypeName("artefact")
	public static class ArtefactNode extends Node {
		@Nonnull
		@JsonProperty
		private final Artefact artefact;

		public ArtefactNode(@Nonnull String id, @Nonnull Artefact artefact) {
			super(id);
			this.artefact = artefact;
		}
		
		@Nonnull
		public Artefact getArtefact() {
			return artefact;
		}
	}
	

	@JsonTypeName("virtual")
	public static class VirtualNode extends Node {

		@JsonProperty
		private final String kind;

		public VirtualNode(@Nonnull String id, String kind) {
			super(id);
			this.kind = kind;
		}
		
		public String getKind() {
			return kind;
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

	
	public Graph<Node, Edge> getGraph() {
		return impl;
	}

}
