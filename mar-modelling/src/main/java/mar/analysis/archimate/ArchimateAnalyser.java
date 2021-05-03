package mar.analysis.archimate;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.models.archimate.ArchimateLoader;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;
import mar.validation.server.AnalysisClient;

public class ArchimateAnalyser extends SingleEMFFileAnalyser {

	public static final String ID = "archimate";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
	
		}
		
		@Override
		public ArchimateAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new ArchimateAnalyser();
		}				
		
		@Override
		public ISingleFileAnalyser newRemoteAnalyser(@CheckForNull OptionMap options) {
			return new AnalysisClient(ID, options);
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		ArchimateLoader loader = new ArchimateLoader();
		return loader.load(f.getFullFile());
	}

}
