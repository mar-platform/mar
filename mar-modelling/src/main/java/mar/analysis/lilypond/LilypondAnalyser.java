package mar.analysis.lilypond;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.models.archimate.ArchimateLoader;
import mar.models.elysium.LilypondLoader;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;
import mar.validation.server.AnalysisClient;

public class LilypondAnalyser extends SingleEMFFileAnalyser {

	public static final String ID = "lilypond";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
	
		}
		
		@Override
		public LilypondAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new LilypondAnalyser();
		}				
		
		@Override
		public ISingleFileAnalyser newRemoteAnalyser(@CheckForNull OptionMap options) {
			return new AnalysisClient(ID, options);
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		LilypondLoader loader = new LilypondLoader();
		return loader.load(f.getFullFile());
	}

}
