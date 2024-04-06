package mar.embeddings.scorer;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import mar.embeddings.EMFQuery;
import mar.embeddings.JVectorDatabase;
import mar.embeddings.JVectorDatabase.QueryResult;
import mar.indexer.embeddings.EmbeddingStrategy;

public class VectorScorer {
	private final EmbeddingStrategy strategy;
	private final JVectorDatabase db;

	public VectorScorer(JVectorDatabase db, EmbeddingStrategy strategy) {
		this.db = db;
		this.strategy = strategy;
	}
	
	public List<QueryResult> score(Resource r) {
		EMFQuery emfQuery = new EMFQuery.Generic(r, strategy);
		return db.search(emfQuery, 500);		
	}
	
}