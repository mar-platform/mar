package mar.ingestion;

import java.util.List;

import javax.annotation.CheckForNull;

/**
 * Represents the metadata that may be provied by the ingestion
 * process.
 * 
 * @author jesus
 *
 */
public interface IngestedMetadata {

	@CheckForNull
	public String getURL();

	int getSizeBytes();

	List<? extends String> getTopics();
	
	public String getDescription();
	
	public String getExplicitName();
}
