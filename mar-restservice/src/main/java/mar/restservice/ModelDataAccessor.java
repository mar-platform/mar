package mar.restservice;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.restservice.model.IModelResult;

public interface ModelDataAccessor extends Closeable {

	public void updateInformation(@Nonnull List<? extends IModelResult> models) throws IOException;

	@CheckForNull
	public String getMetadata(@Nonnull String id) throws IOException;

}
