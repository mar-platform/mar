package mar.model2graph;

import java.util.function.Predicate;

import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.Graph;

import mar.paths.ListofPaths;

public class Model2GraphPredicate extends AbstractModel2Graph {
	
	@SuppressWarnings("rawtypes")
	private Predicate<Graph> predicate;
	private PathComputation pcTrue;
	private PathComputation pcFalse;
	
	
	public Model2GraphPredicate(Predicate<Graph> predicate, PathComputation pcTrue, PathComputation pcFalse) {
		super();
		this.predicate = predicate;
		this.pcTrue = pcTrue;
		this.pcFalse = pcFalse;
	}


	@Override
	public ListofPaths getListOfPaths(Resource r) {
		Graph<Node,Edge> graph = createParallelGraph(r);
		
		if (predicate.test(graph))
			return pcTrue.getListOfPaths(r);
		else
			return pcFalse.getListOfPaths(r);

	}
	
	
	


}
