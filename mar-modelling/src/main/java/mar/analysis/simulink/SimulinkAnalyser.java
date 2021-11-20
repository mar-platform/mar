package mar.analysis.simulink;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.modelling.loader.ILoader;
import mar.models.simulink.SimulinkLoader;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;

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
		public String getId() {
			return ID;
		}

		@Override
		public ILoader newLoader() {
			return new SimulinkLoader();
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		SimulinkLoader loader = new SimulinkLoader();
		return loader.toEMF(f.getFullFile());
	}

}
