package mar.indexer.embeddings;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.github.jbellis.jvector.disk.OnDiskGraphIndex;
import io.github.jbellis.jvector.graph.GraphIndexBuilder;
import io.github.jbellis.jvector.graph.OnHeapGraphIndex;
import io.github.jbellis.jvector.graph.RandomAccessVectorValues;
import io.github.jbellis.jvector.vector.VectorEncoding;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import mar.paths.PathParser;
import mar.sqlite.SqliteIndexDatabase;

/**
 * This is an attempt to lazily compute the embeddings but doesn't seem to work well because it 
 * gets in the middle of the parallel processing (I think)
 * 
 * @author jesus
 *
 */
@Deprecated
public class PathEmbeddingIndexer {

	static final int WORDE_DIMENSIONS = 300;
	
	private EmbeddingStrategy embedding;

	public PathEmbeddingIndexer(EmbeddingStrategy embedding) throws IOException {
		this.embedding = embedding;
	}
	
	public void indexModels(SqliteIndexDatabase db, File indexFileName) throws IOException, SQLException {
		ModelEmbeddingListAccess ravv = new ModelEmbeddingListAccess(db, embedding);
		GraphIndexBuilder<float[]> indexBuilder = new GraphIndexBuilder<float[]>(
				ravv,
				VectorEncoding.FLOAT32,
				VectorSimilarityFunction.COSINE, 32, 100, 0.5f, 2.0f
			);

		
		long start = System.currentTimeMillis();
		long last  = start;
		
		int size = ravv.size();
		for(int i = 0; i < size; i++) {
			if (i % 1000 == 1) {
				long end = System.currentTimeMillis();
				long totalTime = end - start;
				long remaining = (totalTime * size / i) - totalTime;
				
				System.out.println("Indexing node: " + i + 
						" " + String.format("%.2f", (i / size) * 100.0) + "%" +
						" " + String.format("%.2f", (end - last) / (1000.0)) + " secs" +
						" " + String.format("%.2f", (totalTime) / (1000.0 * 60)) + " mins." +
						" . Remaining " + String.format("%.2f", (remaining) / (1000.0 * 60)) + " mins.");
				
				
				last = end;
			}
				
			indexBuilder.addGraphNode(i, ravv);
		}
		
		//OnHeapGraphIndex<float[]> onHeapGraph = indexBuilder.build();
		System.out.println("Cleaning up");
		indexBuilder.cleanup();
		OnHeapGraphIndex<float[]> onHeapGraph = indexBuilder.getGraph();
		
		System.out.println("Writing to disk");
		try (DataOutputStream outputFile = new DataOutputStream(new FileOutputStream(indexFileName))) {
            OnDiskGraphIndex.write(onHeapGraph, ravv, outputFile);
        }	
	}

	private static class ModelEmbeddingListAccess 
		extends CacheLoader<Integer, float[]> 
		implements RandomAccessVectorValues<float[]> {
		private int embeddingSize;

		private SqliteIndexDatabase db;
		private EmbeddingStrategy embedding;
		LoadingCache<Integer, float[]> cache;

		private int numPaths;
		
		public ModelEmbeddingListAccess(SqliteIndexDatabase db, EmbeddingStrategy embedding) throws IOException, SQLException {
			this.db = db;
			this.embedding = embedding;
			this.embeddingSize = embedding.size();
		    this.cache = CacheBuilder.newBuilder().
		    		//maximumSize(8129 * 1024).
		    		maximumSize(9570175).
		    		build(this);
		    
		    this.numPaths = db.getPathCount();
			System.out.println("Finished computing path embeddings: " + numPaths);
		}

		@Override
		public int size() {
			return numPaths;
		}
		 
		@Override
		public int dimension() {
			return embeddingSize;
		}

		@Override
		public float[] vectorValue(int targetOrd) {
			try {
				if (targetOrd >= 9570170 && targetOrd <= 9570175) {
					System.out.println("Vector " + targetOrd);
				}
				return cache.get(targetOrd + 1);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean isValueShared() {
			return false;
		}

		@Override
		public RandomAccessVectorValues<float[]> copy() {
			return this;
		}

		@Override
		public float[] load(Integer pathId) {
			try {
				String path = db.getPath(pathId);

				String[] words = PathParser.INSTANCE.toAttributeValues(path);
				Preconditions.checkState(pathId < Integer.MAX_VALUE);
				int seqId = (int) pathId;
				
				IndexedPath ipath = new IndexedPath(seqId, words);
				float[] vector = embedding.toNormalizedVector(ipath);	
				return vector;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

	}	
	
}
