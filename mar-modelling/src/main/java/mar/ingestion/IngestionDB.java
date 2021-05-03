package mar.ingestion;

import java.util.List;

/**
 * Represents a source of models.
 * 
 * @author jesus
 *
 */
public interface IngestionDB {

	public List<? extends IngestedModel> getModels();

}
