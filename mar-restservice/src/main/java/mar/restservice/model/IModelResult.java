package mar.restservice.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.validation.AnalysisMetadataDocument;

/**
 * Represents a model obtained as a search result.
 * 
 * @author jesus
 */
public interface IModelResult {

	@Nonnull
	public String getName();

	@Nonnull
	public String getId();

	@CheckForNull
	public String getURL();
	
	public void setMetadata(@Nonnull AnalysisMetadataDocument metadata);
	
}
