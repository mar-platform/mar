package mar.validation.server;

import java.io.IOException;
import java.util.function.Supplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.analysis.thrift.Result;
import mar.analysis.thrift.ValidateService;
import mar.analysis.thrift.ValidationJob;
import mar.ingestion.IngestedMetadata;
import mar.sandbox.SandboxClient;
import mar.validation.AnalysisDB.Status;
import mar.validation.AnalysisMetadataDocument;
import mar.validation.AnalysisResult;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.OptionMap;

public class AnalysisClient extends SandboxClient {
	
	@Nonnull
	private final OptionMap options;
	
	public AnalysisClient(@CheckForNull OptionMap options) {
		this.options = options;
	}

	public AnalysisClient() {
		this(null);
	}
	
	@Nonnull
	public AnalysisResult analyse(@Nonnull IFileInfo f, @Nonnull String type) {
		Invoker<AnalysisResult> invoker = (protocol) -> {
			ValidateService.Client client = new ValidateService.Client(protocol);
			
			ValidationJob job = new ValidationJob(f.getModelId(), f.getRelativePath(), f.getAbsolutePath(), type, options);
			Result jobResult = client.validate(job);
			
			AnalysisResult r = new AnalysisResult(f.getModelId(), Status.valueOf(jobResult.getStatus()));
			if (jobResult.isSetStats())
				jobResult.stats.forEach((k, v) -> r.withStats(k, v));
			if (jobResult.isSetMetadata())
				r.withMetadata(jobResult.metadata);			
			if (jobResult.isSetMetadata_json()) {
				AnalysisMetadataDocument document = AnalysisMetadataDocument.loadFromJSON(jobResult.metadata_json);
				addIngestedMetadata(f, document);
				try {
					r.withMetadataJSON(document);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
			return r;
		};
		
		Supplier<AnalysisResult> onTimeOut = () -> {
			return new AnalysisResult(f.getModelId(), Status.TIMEOUT);					
		};
		
		return invokeService(invoker, onTimeOut);
	}
	
	private void addIngestedMetadata(IFileInfo origin, @CheckForNull AnalysisMetadataDocument document) {
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
