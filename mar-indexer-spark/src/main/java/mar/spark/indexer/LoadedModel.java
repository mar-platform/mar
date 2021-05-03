package mar.spark.indexer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

public class LoadedModel {
	@CheckForNull Resource resource;
	@Nonnull
	private ModelOrigin origin;

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
}