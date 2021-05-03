package mar.paths;

import java.util.HashMap;
import java.util.function.Predicate;

import org.jgrapht.Graph;

import mar.model2graph.AbstractPathComputation;
import mar.model2graph.MetaFilter;
import mar.model2graph.Model2GraphAllpaths;
import mar.model2graph.Model2GraphPredicate;
import mar.model2graph.PathComputation;

public class DefaultPathsComputations {
	
	private HashMap<String, AbstractPathComputation> pathsComputations= new HashMap<>();
	private static DefaultPathsComputations dpc;
	
	private DefaultPathsComputations() {
		
		HashMap<String, PathComputation> pathsComputations= new HashMap<>();
		
		//ecore
		{
			AbstractPathComputation model2graphTrue = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.EcoreTokenizer());
			model2graphTrue.withFilter(MetaFilter.getEcoreFilter());
			AbstractPathComputation model2graphFalse = new Model2GraphAllpaths(3).withPathFactory(new PathFactory.EcoreTokenizer());
			model2graphFalse.withFilter(MetaFilter.getEcoreFilter());
			
			@SuppressWarnings("rawtypes")
			Predicate<Graph> p = (g) -> g.vertexSet().size() < 2000;
			Model2GraphPredicate m2gp = new Model2GraphPredicate(p, model2graphTrue, model2graphFalse);
			pathsComputations.put("ecore", m2gp);
		}


		
		AbstractPathComputation model2graph_sm = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.WSandCCTokenizerSWStemming());
		model2graph_sm.withFilter(MetaFilter.getUMLStateMachineFilter()); 
		
		AbstractPathComputation model2graph_uml = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.DefaultPathFactory());
		model2graph_sm.withFilter(MetaFilter.getUMLEasyFilter()); 

		AbstractPathComputation model2graph_bpmn = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.DefaultPathFactory());
		model2graph_bpmn.withFilter(MetaFilter.getNoFilter()); 
		
		
		pathsComputations.put("uml_sm", model2graph_sm);
		pathsComputations.put("uml", model2graph_uml);				
		pathsComputations.put("bpmn2", model2graph_bpmn);
	}
	
	public static AbstractPathComputation getPathComputation(String model) {
		if (dpc == null) {
			dpc = new DefaultPathsComputations();
		}
		
		return dpc.pathsComputations.get(model);
	}
}
