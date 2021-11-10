package mar.validation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.ingestion.IngestedMetadata;

public interface ISingleFileAnalyser {

	@Nonnull
	AnalysisResult analyse(@Nonnull IFileInfo f);

	public interface Remote extends ISingleFileAnalyser, AutoCloseable {
	}

}
