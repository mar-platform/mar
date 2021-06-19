package mar;

import java.util.HashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.model2graph.AbstractPathComputation;
import mar.model2graph.MetaFilter;
import mar.model2graph.Model2GraphAllpaths;
import mar.model2graph.PathComputation;
import mar.paths.PathFactory;
import mar.paths.stemming.UMLPathFactory;
import mar.restservice.HBaseNeuralScorer;
import mar.restservice.HBaseScorerFinal;
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
