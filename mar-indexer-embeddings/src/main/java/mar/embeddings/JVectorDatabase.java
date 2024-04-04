package mar.embeddings;

import java.io.File;
import java.io.IOException;

import io.github.jbellis.jvector.disk.CachingGraphIndex;
import io.github.jbellis.jvector.disk.OnDiskGraphIndex;
import io.github.jbellis.jvector.disk.SimpleMappedReaderSupplier;
import io.github.jbellis.jvector.graph.GraphIndex.View;
import io.github.jbellis.jvector.graph.GraphIndex;
import io.github.jbellis.jvector.graph.GraphSearcher;
import io.github.jbellis.jvector.graph.NodeSimilarity.ExactScoreFunction;
import io.github.jbellis.jvector.graph.SearchResult;
import io.github.jbellis.jvector.graph.SearchResult.NodeScore;
import io.github.jbellis.jvector.util.Bits;
import io.github.jbellis.jvector.vector.VectorSimilarityFunction;
import mar.embeddings.IndexedDB.IndexedModel;
import mar.embeddings.IndexedDB.Mode;

public class JVectorDatabase {

	private GraphIndex<float[]> onDiskGraph;
	private GraphSearcher<float[]> searcher;
	private IndexedDB indexDb;

	// possibly also pass the sqlite file which have the index data
	public JVectorDatabase(File jvectorDb, File sqliteDb) throws IOException {
        //this.onDiskGraph = new CachingGraphIndex(new OnDiskGraphIndex<float[]>(new SimpleMappedReaderSupplier(f.toPath()), 0));
		this.indexDb = new IndexedDB(sqliteDb, Mode.READ);
		
		this.onDiskGraph = new OnDiskGraphIndex<float[]>(new SimpleMappedReaderSupplier(jvectorDb.toPath()), 0);
        this.searcher = new GraphSearcher.Builder<float[]>(onDiskGraph.getView()).build();
        
		/*
		try (DataOutputStream outputFile = new DataOutputStream(new FileOutputStream(graphPath.toFile()))){

            OnDiskGraphIndex.write(onHeapGraph, ravv, outputFile);
            onDiskGraph = new CachingGraphIndex(new OnDiskGraphIndex(ReaderSupplierFactory.open(graphPath), 0));

            testRecallInternal(onHeapGraph, ravv, queryVectors, groundTruth, null);
            testRecallInternal(onDiskGraph, null, queryVectors, groundTruth, compressedVectors);
        } finally {
            if (onDiskGraph!= null) {
                onDiskGraph.close();
            }
            Files.deleteIfExists(graphPath);
        }
        */		
	}
	
	public void search(Query query) {
        SearchResult r = this.searcher.search(
        		new Score(VectorSimilarityFunction.COSINE, onDiskGraph.getView(), query.queryVector()), 
        		null, 100, Bits.ALL);
        System.out.println(r.getNodes());
        for (NodeScore nodeScore : r.getNodes()) {
        	IndexedModel m = indexDb.getById(nodeScore.node + 1);
			if (m != null) {
				System.out.println(m.getModelId() + " - " + nodeScore.node + " - " + nodeScore.score);
			} else {
				System.out.println("not found");
			}
		}
	}
	
	public static interface Query {
		public float[] queryVector();
	}
	
	public void doSearch(float[] queryVector) {
		// https://github.com/jbellis/jvector/blob/main/jvector-examples/src/main/java/io/github/jbellis/jvector/example/SiftSmall.java
        /*
		SearchScoreProvider ssp;
        if (compressedVectors == null) {
            var sf = ScoreFunction.ExactScoreFunction.from(queryVector, VectorSimilarityFunction.EUCLIDEAN, ravv);
            ssp = new SearchScoreProvider(sf, null);
        }
        else {
            ScoreFunction.ApproximateScoreFunction sf = compressedVectors.precomputedScoreFunctionFor(queryVector, VectorSimilarityFunction.EUCLIDEAN);
            var rr = ScoreFunction.ExactScoreFunction.from(queryVector, VectorSimilarityFunction.EUCLIDEAN, (GraphIndex.ViewWithVectors) view);
            ssp = new SearchScoreProvider(sf, rr);
        }
        */

        //sf = ExactScoreFunction.from(queryVector, VectorSimilarityFunction.COSINE, null);
        //ssp = new SearchScoreProvider(sf, null);

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
