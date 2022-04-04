package mar.analysis.backend.megamodel;

import javax.annotation.Nonnull;

import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class TransformationRelationshipsAnalysis {

	private MegamodelDB db;

	public TransformationRelationshipsAnalysis(@Nonnull MegamodelDB db) {
		this.db = db;		
	}
	
	@Nonnull
	public RelationshipsGraph getRelationships() {
		RelationshipsGraph graph = new RelationshipsGraph();
		System.out.println("Getting artefacts...");
		this.db.getAllArtefacts().forEach((id, artefact) -> {
			Node node = new RelationshipsGraph.Node(id, artefact);
			graph.addNode(node);
		});
		
		System.out.println("Getting edges...");
		db.getRelationshipsByType(Relationship.TYPED_BY, (src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		});

		db.getRelationshipsByType(Relationship.IMPORT, (src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		});
		
		db.getRelationshipsByType(Relationship.DUPLICATE, (src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		});

		return graph;
	}
	
}
