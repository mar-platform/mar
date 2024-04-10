package mar.restservice.scoring;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;

import mar.embeddings.JVectorDatabase;
import mar.embeddings.JVectorDatabase.QueryResult;
import mar.embeddings.scorer.VectorScorer;
import mar.indexer.embeddings.EmbeddingStrategy;
import mar.indexer.embeddings.WordExtractor;
import mar.restservice.IScorer;
import mar.restservice.Profiler;

public class JVectorScorer implements IScorer {

	private VectorScorer impl;

	public JVectorScorer(Path jvectorVectors, Path sqliteIndex, EmbeddingStrategy embeddingStrategy, WordExtractor extractor) throws IOException {
		JVectorDatabase db = new JVectorDatabase(jvectorVectors.toFile(), sqliteIndex.toFile());
		this.impl = new VectorScorer(db, embeddingStrategy, extractor);
	}
	
	@Override
	public Map<String, Double> score(Resource r, Profiler profiler) throws IOException {
		profiler.start();
			List<QueryResult> queryResults = impl.score(r);
		profiler.stop("score");
		
		Map<String, Double> results = new HashMap<>();
		for(QueryResult qr : queryResults) {
			results.put(qr.modelId, (double) qr.score);
		}
		
		return results;
	}

	@Override
	public Set<String> getStopWords(String model) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, List<Double>> partitionedScore(Resource r, Profiler profiler) throws IOException {
		throw new UnsupportedOperationException();
	}
}
