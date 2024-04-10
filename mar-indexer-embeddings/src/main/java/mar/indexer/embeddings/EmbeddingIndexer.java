package mar.indexer.embeddings;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import io.github.jbellis.jvector.disk.OnDiskGraphIndex;
import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.OnHeapGraphIndex;
import io.github.jbellis.jvector.graph.RandomAccessVectorValues;
import io.github.jbellis.jvector.vector.VectorEncoding;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import io.github.jbellis.jvector.vector.VectorUtil;

public class EmbeddingIndexer {

	static final int WORDE_DIMENSIONS = 300;
	
	private EmbeddingStrategy embedding;

	public EmbeddingIndexer(EmbeddingStrategy embedding) throws IOException {
		this.embedding = embedding;
	}
	
	public void indexModels(File indexFileName, List<WordedModel> models) throws IOException {
		ModelEmbeddingListAccess ravv = new ModelEmbeddingListAccess(models, embedding);
		GraphIndexBuilder<float[]> indexBuilder = new GraphIndexBuilder<float[]>(
				ravv,
				VectorEncoding.FLOAT32,
				//VectorSimilarityFunction.COSINE, 32, 100, 1.5f, 1.4f
				//VectorSimilarityFunction.COSINE, 16, 50, 0.75f, 1.0f
				//VectorSimilarityFunction.DOT_PRODUCT, 32, 100, 0.5f, 2.0f
				
				//VectorSimilarityFunction.COSINE, 32, 100, 0.5f, 2.0f
				VectorSimilarityFunction.DOT_PRODUCT, 32, 100, 0.5f, 2.0f
				
				
			);
		
		
		
		OnHeapGraphIndex<float[]> onHeapGraph = indexBuilder.build();
		//graph.save(new DataOutputStream(new FileOutputStream(indexFileName)));
		
		try (DataOutputStream outputFile = new DataOutputStream(new FileOutputStream(indexFileName))) {

            OnDiskGraphIndex.write(onHeapGraph, ravv, outputFile);
            //onDiskGraph = new CachingGraphIndex(new OnDiskGraphIndex(ReaderSupplierFactory.open(graphPath), 0));

            //testRecallInternal(onHeapGraph, ravv, queryVectors, groundTruth, null);
            //testRecallInternal(onDiskGraph, null, queryVectors, groundTruth, compressedVectors);
        }
		
		
		
	}

	private static class ModelEmbeddingListAccess implements RandomAccessVectorValues<float[]> {

		private final List<WordedModel> models;
		private Map<Integer, float[]> results = new HashMap<>();
		private int embeddingSize;
		
		public ModelEmbeddingListAccess(List<WordedModel> models, EmbeddingStrategy embedding) throws IOException {
			this.models = models;
			this.embeddingSize = embedding.size();

			for (int i = 0; i < models.size(); i++) {
				WordedModel m = models.get(i);
				float[] e = embedding.toNormalizedVector(m);
				if (e == null)
					throw new IllegalStateException();
				results.put(m.getSeqId() - 1, e);
			}
		}

		@Override
		public int size() {
			return models.size();
		}

		@Override
		public int dimension() {
			return embeddingSize;
		}


		@Override
		public float[] vectorValue(int targetOrd) {
			WordedModel m = models.get(targetOrd);
			Preconditions.checkState(m.getSeqId() - 1 == targetOrd);
			
			float[] r = results.get(targetOrd);
			Preconditions.checkNotNull(r);

			return r;
		}

		@Override
		public boolean isValueShared() {
			return false;
		}

		@Override
		public RandomAccessVectorValues<float[]> copy() {
			return this;
		}
	}	
	
}
