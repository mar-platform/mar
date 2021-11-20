package mar.analysis.pnml;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.modelling.loader.ILoader;
import mar.models.pnml.PnmlLoader;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;

public class PnmlAnalyser extends SingleEMFFileAnalyser {

	public static final String ID = "pnml";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
	
		}
		
		@Override
		public PnmlAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new PnmlAnalyser();
		}				
		
		@Override
		public String getId() {
			return ID;
		}

		@Override
		public ILoader newLoader() {
			return new PnmlLoader();
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		PnmlLoader loader = new PnmlLoader();
		return loader.toEMF(f.getFullFile());
	}

}
