package mar;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.model2graph.PathComputation;
import mar.restservice.IScorer;

public class MarConfiguration {

	@NonNull
	private PathComputation pathComputation;
	
	@NonNull
	private IScorer scorer;
	
	public MarConfiguration(@NonNull PathComputation pathComputation, @NonNull IScorer scorer) {
		this.pathComputation = pathComputation;
		this.scorer = scorer;
	}
		
	@NonNull
	public PathComputation getPathComputation() {
		return pathComputation;
	}
	
	@NonNull
	public IScorer getScorer() {
		return scorer;
	}	
}
