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
	public static MarConfiguration getHbaseConfiguration(@NonNull String model)  {	
		HashMap<String, PathComputation> pathsComputations= new HashMap<>();
		
		//AbstractPathComputation model2graph = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.WSandCCTokenizerSWStemming());
		AbstractPathComputation model2graph = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.EcoreTokenizer());
		model2graph.withFilter(MetaFilter.getEcoreFilterNames()); //change
		
		AbstractPathComputation model2graph_sm = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.WSandCCTokenizerSWStemming());
		model2graph_sm.withFilter(MetaFilter.getUMLStateMachineFilter()); //change
		
		AbstractPathComputation model2graph_uml = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.DefaultPathFactory());
		model2graph_sm.withFilter(MetaFilter.getUMLEasyFilter()); //change

		AbstractPathComputation model2graph_bpmn = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.DefaultPathFactory());
		model2graph_bpmn.withFilter(MetaFilter.getNoFilter()); //change

		pathsComputations.put("ecore", model2graph);
		
		pathsComputations.put("uml_sm", model2graph_sm);
		pathsComputations.put("uml", model2graph_uml);				
		pathsComputations.put("bpmn2", model2graph_bpmn);
		// TODO: Get this information from the configuration		
		
		PathComputation pc = pathsComputations.get(model);
		
		if (pc == null)
			pc = new Model2GraphAllpaths(3).withPathFactory(new PathFactory.DefaultPathFactory());
				
		HBaseScorerFinal hsf = new HBaseScorerFinal(pc, model);
		//HBaseNeuralScorer hns = new HBaseNeuralScorer(hsf);
				
		return new MarConfiguration(model2graph, hsf);
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
