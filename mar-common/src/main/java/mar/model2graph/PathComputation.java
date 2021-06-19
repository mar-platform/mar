package mar.model2graph;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;
import edu.umd.cs.findbugs.annotations.NonNull;
import mar.paths.ListofPaths;
import mar.paths.PathFactory;

public interface PathComputation {
	
	ListofPaths getListOfPaths(@NonNull Resource r);
	
	@NonNull
	public IMetaFilter getFilter();
	
	@Nonnull
	public PathFactory getPathFactory();
}
