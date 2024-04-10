package mar.restservice.scoring;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;

import mar.restservice.IScorer;
import mar.restservice.Profiler;

public class SqliteWithJVectorScorer implements IScorer {

	private final SqliteScorer scorer1;
	private final JVectorScorer scorer2;

	public SqliteWithJVectorScorer(SqliteScorer sqliteScorer, JVectorScorer jvectorScorer) {
		this.scorer1 = sqliteScorer;
		this.scorer2 = jvectorScorer;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> score(Resource r, Profiler profiler) throws IOException {	
		Map<String, Double> scores1 = scorer1.sortedScore(r);
		Map<String, Double> scores2 = scorer2.sortedScore(r);
		
		double maxScore1 = scores1.entrySet().iterator().next().getValue();
		double maxScore2 = scores2.entrySet().iterator().next().getValue();
		
		double lambda = 0.65;
		
		Map<String, Double> results = new HashMap<>(scores1.size());
		
		for (Entry<String, Double> entry1 : scores1.entrySet()) {
			String modelId = entry1.getKey();
			double score1 = entry1.getValue() / maxScore1;
			
			Double score2 = scores2.get(modelId);
			if (score2 == null) {
				score2 = 0.0d;
			} else {
				score2 = score2 / maxScore2;				
			}
			
			double newScore = lambda * score1 + (1 - lambda) * score2;
			results.put(modelId, newScore);			
		}
		
		for (Entry<String, Double> entry2 : scores2.entrySet()) {
			String modelId = entry2.getKey();
			
			Double score1 = scores1.get(modelId);
			if (score1 == null) {
				double score2 = entry2.getValue();
				double newScore = lambda * 0.0d + (1 - lambda) * score2;
				results.put(modelId, newScore);
			}
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
