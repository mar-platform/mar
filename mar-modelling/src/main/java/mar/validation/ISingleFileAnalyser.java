package mar.validation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.ingestion.IngestedMetadata;

public interface ISingleFileAnalyser {

	@Nonnull
	AnalysisResult analyse(@Nonnull IFileInfo f);

	public interface Remote extends ISingleFileAnalyser, AutoCloseable {
	}
	
	/* proteted */ default void addIngestedMetadata(IFileInfo origin, @CheckForNull AnalysisMetadataDocument document) {
		if (document == null) 
			return;
		if (! (origin instanceof IngestedMetadata))
			return;
				
		IngestedMetadata metadata = (IngestedMetadata) origin;
		
		document.setURL(metadata.getURL());
		document.setTopics(metadata.getTopics());
		document.setExplicitName(metadata.getExplicitName());
	}

}
