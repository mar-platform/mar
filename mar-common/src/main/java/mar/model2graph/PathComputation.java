package mar.model2graph;

import org.eclipse.emf.ecore.resource.Resource;
import edu.umd.cs.findbugs.annotations.NonNull;
import mar.paths.ListofPaths;

public interface PathComputation {
	
	ListofPaths getListOfPaths(@NonNull Resource r);
}
