package mar.spark.indexer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

public class LoadedModel {
	@CheckForNull
	public final Resource resource;
	@Nonnull
	public final ModelOrigin origin;

	public LoadedModel(@CheckForNull Resource r, ModelOrigin origin) {
		this.resource = r;
		this.origin = origin;
	}

	public boolean isSuccess() {
		return resource != null;
	}
	
	@Nonnull
	public ModelOrigin getOrigin() {
		return origin;
	}
	
	public Resource getResource() {
		return resource;
	}
}