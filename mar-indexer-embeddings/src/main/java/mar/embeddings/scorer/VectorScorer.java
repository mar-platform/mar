package mar.embeddings.scorer;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import mar.embeddings.EMFQuery;
import mar.embeddings.JVectorDatabase;
import mar.embeddings.JVectorDatabase.QueryResult;
import mar.indexer.embeddings.EmbeddingStrategy;
import mar.indexer.embeddings.WordExtractor;
import mar.indexer.embeddings.WordExtractor.NameExtractor;

public class VectorScorer {
	private final EmbeddingStrategy strategy;
	private final JVectorDatabase db;
	private WordExtractor extractor;

	public VectorScorer(JVectorDatabase db, EmbeddingStrategy strategy, WordExtractor extractor) {
		this.db = db;
		this.strategy = strategy;
		this.extractor = extractor;
	}
	
	public List<QueryResult> score(Resource r) {				
		EMFQuery emfQuery = new EMFQuery.Generic(r, strategy, extractor);
		return db.search(emfQuery, 500);		
	}
	
}
