package mar.embeddings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.jbellis.jvector.disk.OnDiskGraphIndex;
import io.github.jbellis.jvector.disk.SimpleMappedReaderSupplier;
import io.github.jbellis.jvector.graph.GraphIndex;
import io.github.jbellis.jvector.graph.GraphIndex.View;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.NodeSimilarity.ExactScoreFunction;
import io.github.jbellis.jvector.graph.SearchResult;
import io.github.jbellis.jvector.graph.SearchResult.NodeScore;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import mar.embeddings.IndexedDB.Mode;

public class JVectorDatabase {

	private GraphIndex<float[]> onDiskGraph;
	private GraphSearcher<float[]> searcher;
	private IndexedDB indexDb;

	// possibly also pass the sqlite file which have the index data
	public JVectorDatabase(File jvectorDb, File sqliteDb) throws IOException {
		this.indexDb = new IndexedDB(sqliteDb, Mode.READ);
		
		this.onDiskGraph = new OnDiskGraphIndex<float[]>(new SimpleMappedReaderSupplier(jvectorDb.toPath()), 0);
        this.searcher = new GraphSearcher.Builder<float[]>(onDiskGraph.getView()).build();	
	}
	
	public List<QueryResult> search(Query query, int numResults) {
        SearchResult r = this.searcher.search(
        		new Score(VectorSimilarityFunction.COSINE, onDiskGraph.getView(), query.queryVector()), 
        		null, numResults, Bits.ALL);
        
        List<QueryResult> result = new ArrayList<>();
        for (NodeScore nodeScore : r.getNodes()) {
        	String modelId = indexDb.getModelId(nodeScore.node + 1);
			if (modelId != null) {
				result.add(new QueryResult(modelId, nodeScore.score));
			} else {
				throw new IllegalStateException("Node not found " + nodeScore.node);
			}
		}
        return result;
	}
	
	public static class QueryResult {
		public final String modelId;
		public final float score;
		
		public QueryResult(String modelId, float score) {
			this.modelId = modelId;
			this.score = score;
		}
		
	}
	
	public static interface Query {
		public float[] queryVector();
	}
		
	public static class Score implements ExactScoreFunction {

		private VectorSimilarityFunction f;
		private View<float[]> view;
		private float[] queryVector;

		public Score(VectorSimilarityFunction f, View<float[]> view, float[] queryVector) {
			this.f = f;
			this.view = view;
			this.queryVector = queryVector;
		}
		
		@Override
		public float similarityTo(int node2) {
			float[] v2 = view.getVector(node2);
			return f.compare(this.queryVector, v2);
		}
		
	}
	
}