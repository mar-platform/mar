package mar.restservice.services;

import java.io.File;

import javax.annotation.Nonnull;

import mar.restservice.services.SearchOptions.ModelType;
import mar.validation.AnalyserRegistry;
import mar.validation.AnalysisResult;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ISingleFileAnalyser.Remote;
import mar.validation.ResourceAnalyser.Factory;
import mar.validation.server.AnalysisClient;

/**
 * A service related to analysing a model resource, providing
 * information from different angles. This includes:
 * 
 * <ul>
 * 	<li>Load a given model. This is a check that the model is not completely broken.</li>
 *  <li>Validate the model. </li>
 *  <li>Compute stats</li>
 *  <li>Detect smells</li>
 * </ul>
 * 
 * @author jesus
 */
public class AnalysisService {

	private AnalysisClient analyser = new AnalysisClient();
	
	public AnalysisResult analyse(File f, ModelType type) {
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(type.name());
		if (factory == null) {
			throw new IllegalStateException("No analyser for " + type.name());
		}
		// Not sure if this should be done here, all the time
		factory.configureEnvironment();
		
		AnalysisResult result = analyser.analyse(new NormalFileInfo(f), type.name());
		return result;
	}
	
	private static class NormalFileInfo implements IFileInfo {

		private final File file;

		public NormalFileInfo(@Nonnull File f) {
			this.file = f;
		}

		@Override
		public String getModelId() {
			return "analysed-by-service";
		}

		@Override
		public File getRelativeFile() {
			return file;
		}

		@Override
		public File getFullFile() {
			return file;
		}
		
	}
	
}
