package mar.embeddings;

import org.eclipse.emf.ecore.resource.Resource;

import mar.embeddings.JVectorDatabase.Query;
import mar.indexer.embeddings.EmbeddingStrategy;

public abstract class EMFQuery implements Query {

	protected final Resource resource;
	protected final EmbeddingStrategy embedding;

	public EMFQuery(Resource r, EmbeddingStrategy embedding) {
		this.resource = r;
		this.embedding = embedding;
	}
	
	public static class Generic extends EMFQuery {
	
		public Generic(Resource r, EmbeddingStrategy embedding) {
			super(r, embedding);
		}

		@Override
		public float[] queryVector() {
			return embedding.toVector(resource);
		}
	}

}
