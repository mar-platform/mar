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
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);
		});
		
		this.db.getVirtualNodes((id, kind) -> {
			Node node = new RelationshipsGraph.VirtualNode(id, kind);
			graph.addNode(node);
		});
		
		
		System.out.println("Getting edges...");
		db.getRelationshipsByType((src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		}, Relationship.TYPED_BY, Relationship.IMPORT, Relationship.DUPLICATE);

		return graph;
	}
	
}
