package mar.spark.indexer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class ErrorModel extends LoadedModel implements IError {
	@CheckForNull
	private Exception exception;

	public ErrorModel(@Nonnull Exception e, ModelOrigin origin) {
		super(null, origin);
		this.exception = e;
	}
}