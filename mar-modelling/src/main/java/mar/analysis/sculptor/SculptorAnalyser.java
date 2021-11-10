package mar.analysis.sculptor;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.models.pnml.SculptorLoader;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;
import mar.validation.server.AnalysisClient;

public class SculptorAnalyser extends SingleEMFFileAnalyser {

	public static final String ID = "sculptor";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
	
		}
		
		@Override
		public SculptorAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new SculptorAnalyser();
		}				
		
		@Override
		public String getId() {
			return ID;
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		SculptorLoader loader = new SculptorLoader();
		return loader.load(f.getFullFile());
	}

}
