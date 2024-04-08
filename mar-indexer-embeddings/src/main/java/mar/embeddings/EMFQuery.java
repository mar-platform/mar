package mar.embeddings;

import org.eclipse.emf.ecore.resource.Resource;

import mar.embeddings.JVectorDatabase.Query;
import mar.indexer.embeddings.EmbeddingStrategy;
import mar.indexer.embeddings.WordExtractor;
import mar.indexer.embeddings.WordedModel;

public abstract class EMFQuery implements Query {

	protected final Resource resource;
	protected final EmbeddingStrategy embedding;
	protected final WordExtractor extractor;

	public EMFQuery(Resource r, EmbeddingStrategy embedding, WordExtractor extractor) {
		this.resource = r;
		this.embedding = embedding;
		this.extractor = extractor;
	}
	
	public static class Generic extends EMFQuery {
	
		public Generic(Resource r, EmbeddingStrategy embedding, WordExtractor extractor) {
			super(r, embedding, extractor);
		}

		@Override
		public float[] queryVector() {
			WordedModel worded = new WordedModel(resource, extractor);
			return embedding.toVector(worded);
		}
	}

}
