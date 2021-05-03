package mar.model2graph;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractPathComputation implements PathComputation {

	@NonNull
	protected IMetaFilter filter = MetaFilter.getNoFilter();

	public AbstractPathComputation withFilter(@NonNull IMetaFilter filter) {
		this.filter = filter;
		return this;
	}
	
	@NonNull
	public IMetaFilter getFilter() {
		return filter;
	}
	
	
	
}
