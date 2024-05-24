package mar.embeddings.scorer;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.nd4j.common.base.Preconditions;

import com.indeed.util.mmap.MMapBuffer;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.github.jbellis.jvector.disk.OnDiskGraphIndex;
import io.github.jbellis.jvector.disk.SimpleMappedReaderSupplier;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.NodeSimilarity;
import io.github.jbellis.jvector.graph.SearchResult;
import io.github.jbellis.jvector.graph.SearchResult.NodeScore;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import mar.embeddings.JVectorDatabase;
import mar.embeddings.JVectorDatabase.Score;
import mar.embeddings.PathIndexesDB;
import mar.embeddings.PathIndexesDB.Mode;
import mar.indexer.embeddings.EmbeddingStrategy;
import mar.indexer.embeddings.IndexedPath;

public class PathRetriever {

	private OnDiskGraphIndex<float[]> onDiskGraph;
	private GraphSearcher<float[]> searcher;

	private PathIndexesDB pathDb;
	private EmbeddingStrategy strategy;
	
	private int maxPathGroups = 1;
	private float minSimilarity = 0.98f;

	public PathRetriever(File indexFolder, String modelType, EmbeddingStrategy strategy) throws IOException {
		File jvectorDb = JVectorDatabase.getJVectorDbFile(indexFolder, modelType);	
		File pathIndexesDB = JVectorDatabase.getJVectorPathIndexDbFile(indexFolder, modelType);
			
		this.strategy = strategy;
		this.pathDb = new PathIndexesDB(pathIndexesDB, Mode.READ);
		
		
		//this.onDiskGraph = new OnDiskGraphIndex<float[]>(new SimpleMappedReaderSupplier(jvectorDb.toPath()), 0);
		this.onDiskGraph = new OnDiskGraphIndex<float[]>(new MMapReaderSupplier(jvectorDb.toPath()), 0);
	    this.searcher = new GraphSearcher.Builder<float[]>(onDiskGraph.getView()).build();
	}
	
	public PathRetriever withMaxPathGroups(int maxSimilarPaths) {
		this.maxPathGroups = maxSimilarPaths;
		return this;
	}
	
	public PathRetriever withMinSimilarity(float minSimilarity) {
		Preconditions.checkArgument(minSimilarity >= 0 && minSimilarity <= 1);
		this.minSimilarity  = minSimilarity;
		return this;
	}
	
	
	public List<SimilarPath> retrieve(String[] pathParts) {
		final int numResults = 100;
		//final int numResults = 5;
		
		float[] queryVector = strategy.toNormalizedVector(new IndexedPath(-1, pathParts));
        SearchResult r = this.searcher.search(
        		new Score(VectorSimilarityFunction.DOT_PRODUCT, onDiskGraph.getView(), queryVector), 
        		null, numResults, minSimilarity, Bits.ALL);

        int pathGroupCount = 0;
        List<SimilarPath> paths = new ArrayList<>(r.getNodes().length);
        for (NodeScore nodeScore : r.getNodes()) {
        	if (pathGroupCount == maxPathGroups)
        		break;
        	pathGroupCount++;
        	
			int node = nodeScore.node + 1;
			int[] result = pathDb.getPaths(node);
			Preconditions.checkNotNull(result);
			
			
			//System.out.println(node + ":" + String.join(",", pathParts) + " - " + nodeScore.score);
			//for(int i = 0; i < result.length; i++) {
			//	System.out.println("  - " + result[i]);
			//}
			paths.add(new SimilarPath(nodeScore.score, result));
		}
        
        return paths;
	}
	
	public static class SimilarPath {
		public SimilarPath(float score, int[] result) {
			this.score = score;
			this.pathIds = result;
		}
		private float score;
		private int[] pathIds;
		
		public float getScore() {
			return score;
		}
		
		public int[] getPathIds() {
			return pathIds;
		}
	}
	
	/*
	public static class Reranker implements NodeSimilarity.Reranker {

		@Override
		public float similarityTo(int node2) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	*/
}
