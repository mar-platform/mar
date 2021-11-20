package mar.analysis.lilypond;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.modelling.loader.ILoader;
import mar.models.elysium.LilypondLoader;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;

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
		public String getId() {
			return ID;
		}

		@Override
		public ILoader newLoader() {
			return new LilypondLoader();
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		LilypondLoader loader = new LilypondLoader();
		return loader.toEMF(f.getFullFile());
	}

}
