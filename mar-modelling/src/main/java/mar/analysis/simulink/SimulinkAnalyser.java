package mar.analysis.simulink;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.models.simulink.SimulinkLoader;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;
import mar.validation.server.AnalysisClient;

public class SimulinkAnalyser extends SingleEMFFileAnalyser {

	public static final String ID = "simulink";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
	
		}
		
		@Override
		public SimulinkAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new SimulinkAnalyser();
		}				
		
		@Override
		public ISingleFileAnalyser newRemoteAnalyser(@CheckForNull OptionMap options) {
			return new AnalysisClient(ID, options);
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		SimulinkLoader loader = new SimulinkLoader();
		return loader.load(f.getFullFile());
	}

}
