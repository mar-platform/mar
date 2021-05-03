package mar.spark.indexer;

@SuppressWarnings("serial")
public class ErrorModelPaths implements IModelPaths, IError {

	private IError error;

	public ErrorModelPaths(IError e) {
		this.error = e;
	}

}