package mar.analysis.backend.megamodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.DuplicationGraph;
import mar.analysis.megamodel.model.DuplicationGraph.ArtefactGroup;
import mar.analysis.megamodel.model.DuplicationRelationships;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class TransformationRelationshipsAnalysis {

	private static Relationship[] MAIN_RELATIONSHIP_TYPES = { Relationship.TYPED_BY, Relationship.IMPORT };
	
	private MegamodelDB db;

	public TransformationRelationshipsAnalysis(@Nonnull MegamodelDB db) {
		this.db = db;		
	}
	
	@Nonnull
	public RelationshipsGraph getRelationships() {
		RelationshipsGraph graph = new RelationshipsGraph();
		System.out.println("Getting artefacts...");
		this.db.getAllArtefacts().forEach((id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);
		});
		
		System.out.println("Getting edges...");
		db.getRelationshipsByType((src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		}, MAIN_RELATIONSHIP_TYPES);

		return graph;
	}

	/**
	 * The duplication graph aggregates all nodes in the same duplication group into the same
	 * node, and the relationships are redirected.
	 */
	@Nonnull
	public RelationshipsGraph getDuplicationGraph() {
		DuplicationGraph graph = new DuplicationGraph();
		
		DuplicationRelationships dup = db.getDuplicates();
		dup.forEachGroup((groupId, nodeIds) -> {
			ArtefactGroup node = new DuplicationGraph.ArtefactGroup(groupId, "duplication");
			node.addArtefacts(nodeIds);
			graph.addNode(node);			
		});
		
		
		db.getRelationshipsByType((src, tgt, type) -> {
			// The edge needs to be redirected to a duplication group
			if (type != Relationship.DUPLICATE) {
				String srcGroup = dup.getGroupOf(src);
				String tgtGroup = dup.getGroupOf(tgt);
					
				if (srcGroup != null && tgtGroup != null) {
					ArtefactGroup group1 = (ArtefactGroup) graph.getNode(srcGroup);
					ArtefactGroup group2 = (ArtefactGroup) graph.getNode(tgtGroup);
					graph.addEdge(group1.getId(), group2.getId(), type);
				} else if (srcGroup != null) {
					ArtefactGroup group = (ArtefactGroup) graph.getNode(srcGroup);
					addNodeIfNeeded(graph, tgt);
					graph.addEdge(group.getId(), tgt, type);
				} else if (tgtGroup != null) {
					ArtefactGroup group = (ArtefactGroup) graph.getNode(tgtGroup);
					addNodeIfNeeded(graph, src);
					graph.addEdge(src, group.getId(), type);		
				} else {
					// I think that this should
					// graph.addEdge(src, tgt, type);
				}
			}
		}, plus(MAIN_RELATIONSHIP_TYPES, Relationship.DUPLICATE));
		
		return graph;
	}
	
	private void addNodeIfNeeded(DuplicationGraph graph, String id) {
		if (! graph.hasNode(id)) {
			Artefact artefact = this.db.getAllArtefacts().get(id);
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);
		}
	}

	private static Relationship[] plus(Relationship[] rels, Relationship... other) {
		Relationship[] newRels = new Relationship[rels.length + other.length];
		int i = 0;
		for (; i < rels.length; i++) {
			newRels[i] = rels[i];
		}
		for (int j = 0; j < other.length; j++) {
			newRels[i + j] = other[j];
		}
		
		return newRels;
	}

	/**
	 * This also includes DUPLICATE nodes. It is probably not very useful.
	 */
	@Nonnull
	public RelationshipsGraph getFullRelationships() {
		RelationshipsGraph graph = new RelationshipsGraph();
		System.out.println("Getting artefacts...");
		this.db.getAllArtefacts().forEach((id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);
		});
		
		System.out.println("Getting edges...");
		db.getRelationshipsByType((src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		}, MAIN_RELATIONSHIP_TYPES);

		
		DuplicationRelationships dup = db.getDuplicates();		
		dup.forEachGroup((id, nodeIds) -> {
			Node node = new RelationshipsGraph.VirtualNode(id, "duplication");
			graph.addNode(node);
			nodeIds.forEach(nodeId -> graph.addEdge(id, nodeId, Relationship.DUPLICATE));
		});
		
		return graph;
	}
	
	public RelationshipsGraph getProjectRelationship(String projectId) {
		RelationshipsGraph graph = new RelationshipsGraph();
		this.db.getProjectArtefacts(projectId, (id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);			
		});
		
		// This traverse all relationships, which is inneficient. Not sure if a getRelationshipsByTypeAndProject could be better
		db.getRelationshipsByType((src, tgt, type) -> {
			if (graph.hasNode(src) && graph.hasNode(tgt))
				graph.addEdge(src, tgt, type);
		}, MAIN_RELATIONSHIP_TYPES);
		
		return graph;
	}
	
	@Nonnull
	public RelationshipsGraph getRelationshipsFromSQL(String query) {
		RelationshipsGraph graph = new RelationshipsGraph();
		System.out.println("Getting artefacts...");
	
		Map<String, Node> nodes = new HashMap<>();
		this.db.getAllArtefacts().forEach((id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			nodes.put(id, node);
		});
		
		Set<Node> touched = new HashSet<>();
		db.getRelationshipsFromSQL(query, (src, tgt, type) -> {
			Node srcNode = nodes.get(src);
			Node tgtNode = nodes.get(tgt);
			if (srcNode != null && tgtNode != null) {			
				if (! touched.contains(srcNode))
					graph.addNode(srcNode);
				else 
					touched.add(srcNode);
					
				if (! touched.contains(tgtNode))
					graph.addNode(tgtNode);
				else
					touched.add(tgtNode);
	
				graph.addEdge(src, tgt, type);
			} else {
				System.out.println("Invalid edge: " + src + " - " + tgt);
			}
		});
		
		System.out.println(graph);
		
		return graph;
	}	
}
