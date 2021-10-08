package mar.restservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IScorer {

	@NonNull
	public default Map<String, Double> score(@NonNull Resource r) throws IOException {
		return score(r, new Profiler(), Collections.emptyList());
	}
	
	@NonNull
	public Map<String, Double> score(@NonNull Resource r, Profiler profiler, List<? extends String> ignored) throws IOException;
	
	@NonNull
	public default Map<String, Double> score(@NonNull Resource r, @NonNull List<? extends String> ignored) throws IOException {
		return score(r, new Profiler(), ignored);
	}
	
	@NonNull
	public default Map<String, Double> score(@NonNull Resource r, @NonNull Profiler profiler) throws IOException {
		return score(r, new Profiler(), Collections.emptyList());
	}
	

	public default Map<String, Double> sortedScore(@NonNull Resource r) throws IOException {
		Map<String, Double> scores = score(r);
		return scores.entrySet()
	                .stream()
	                .sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public default Map<String, Double> sortedScore(@NonNull Resource r, int max) throws IOException {
		Map<String, Double> scores = score(r);
		return scores.entrySet()
	                .stream()
	                .sorted((Map.Entry.<String, Double>comparingByValue().reversed())).limit(max)
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	@NonNull
	public Set<String> getStopWords(String model) throws IOException;
	
	@NonNull
	public default Map<String, List<Double>> partitionedScore(@NonNull Resource r) throws IOException {
		return partitionedScore(r, new Profiler());
	}
	
	public Map<String, List<Double>> partitionedScore(Resource r, Profiler profiler) throws IOException;;
}
