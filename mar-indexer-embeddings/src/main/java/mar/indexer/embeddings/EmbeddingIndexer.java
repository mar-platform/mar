package mar.indexer.embeddings;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicLong;
import io.github.jbellis.jvector.disk.OnDiskGraphIndex;
import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.OnHeapGraphIndex;
import io.github.jbellis.jvector.graph.RandomAccessVectorValues;
import io.github.jbellis.jvector.util.PhysicalCoreExecutor;
import io.github.jbellis.jvector.vector.VectorEncoding;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;

public class EmbeddingIndexer {

	static final int WORDE_DIMENSIONS = 300;
	
	private EmbeddingStrategy embedding;

	public EmbeddingIndexer(EmbeddingStrategy embedding) throws IOException {
		this.embedding = embedding;
	}
	
	public void indexModels(File indexFileName, List<? extends Embeddable> models) throws IOException {
		ModelEmbeddingListAccess ravv = new ModelEmbeddingListAccess(models, embedding);
		/*
		GraphIndexBuilder<float[]> indexBuilder = new GraphIndexBuilder<float[]>(
				ravv,
				VectorEncoding.FLOAT32,
				//VectorSimilarityFunction.COSINE, 32, 100, 1.5f, 1.4f
				//VectorSimilarityFunction.COSINE, 16, 50, 0.75f, 1.0f
				//VectorSimilarityFunction.DOT_PRODUCT, 32, 100, 0.5f, 2.0f
				
				//VectorSimilarityFunction.COSINE, 32, 100, 0.5f, 2.0f
				VectorSimilarityFunction.DOT_PRODUCT, 32, 100, 0.5f, 2.0f
			);
		*/
		
		GraphIndexBuilder<float[]> indexBuilder = new GraphIndexBuilder<float[]>(
				ravv,
				VectorEncoding.FLOAT32,
				//VectorSimilarityFunction.DOT_PRODUCT, 32, 100, 0.5f, 2.0f
				VectorSimilarityFunction.DOT_PRODUCT, 100, 100, 1.5f, 1.0f
				//VectorSimilarityFunction.DOT_PRODUCT, 8, 5, 1.5f, 1.5f
			);
	
		
		OnHeapGraphIndex<float[]> onHeapGraph;
		if (false) {
			long start = System.currentTimeMillis();
			long last  = start;
			
			int size = ravv.size();
			for(int i = 0; i < size; i++) {
				if (i % 1000 == 1) {
					last = showStats(start, last, size, i);
				}
					
				indexBuilder.addGraphNode(i, ravv);
			}				
		} else if (true) {
			long start = System.currentTimeMillis();
			long last  = start;
			int size = ravv.size();

			AtomicInteger counter = new AtomicInteger(0);
			AtomicLong lastTime = new AtomicLong(last);
			
			ForkJoinPool simdExecutor = PhysicalCoreExecutor.pool();
	        simdExecutor.submit(() -> {
	            IntStream.range(0, size).parallel().forEach(i -> {
	            	indexBuilder.addGraphNode(i, ravv);
	            	
	            	int inserted = counter.incrementAndGet();
					if (i % 1000 == 1) {
						long r = showStats(start, lastTime.get(), size, inserted);
						lastTime.set(r);
					}
	            });
	        }).join();

			System.out.println("Cleaning up");
			indexBuilder.cleanup();
			onHeapGraph = indexBuilder.getGraph();

		} else {
			onHeapGraph = indexBuilder.build();
		}
		
		System.out.println("Writing to disk");
		
		try (DataOutputStream outputFile = new DataOutputStream(new FileOutputStream(indexFileName))) {

            OnDiskGraphIndex.write(onHeapGraph, ravv, outputFile);
            //onDiskGraph = new CachingGraphIndex(new OnDiskGraphIndex(ReaderSupplierFactory.open(graphPath), 0));

            //testRecallInternal(onHeapGraph, ravv, queryVectors, groundTruth, null);
            //testRecallInternal(onDiskGraph, null, queryVectors, groundTruth, compressedVectors);
        }
		
		
		
	}

	private long showStats(long start, long last, int size, int i) {
		long end = System.currentTimeMillis();
		long totalTime = end - start;
		long remaining = (totalTime * size / i) - totalTime;
		
		System.out.println("Indexing node: " + i + 
				" " + String.format("%.2f", (i * 100.0 / size)) + "%" +
				" " + String.format("%.2f", (end - last) / (1000.0)) + " secs" +
				" " + String.format("%.2f", (totalTime) / (1000.0 * 60)) + " mins." +
				" . Remaining " + String.format("%.2f", (remaining) / (1000.0 * 60)) + " mins.");
		
		
		last = end;
		return last;
	}

	private static class ModelEmbeddingListAccess implements RandomAccessVectorValues<float[]> {

		private final List<? extends Embeddable> models;
		private Map<Integer, float[]> results = new HashMap<>();
		private int embeddingSize;
		
		public ModelEmbeddingListAccess(List<? extends Embeddable> models, EmbeddingStrategy embedding) throws IOException {
			this.models = models;
			this.embeddingSize = embedding.size();

			for (int i = 0; i < models.size(); i++) {
				Embeddable m = models.get(i);
				if (m.isAlreadyEmbedded()) {
					results.put(m.getSeqId() - 1, m.getVector());
				} else {
					float[] e = embedding.toNormalizedVector(m);
					if (e == null)
						throw new IllegalStateException();
					results.put(m.getSeqId() - 1, e);
				}
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
			Embeddable m = models.get(targetOrd);
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
