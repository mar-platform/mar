package mar.analysis.xtext;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.modelling.loader.ILoader;
import mar.models.xtext.XtextLoader;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;

public class XtextAnalyser extends SingleEcoreFileAnalyser {

	public static final String ID = "xtext";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
	
		}
		
		@Override
		public XtextAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new XtextAnalyser();
		}				
		
		@Override
		public String getId() {
			return ID;
		}

		@Override
		public ILoader newLoader() {
			return new XtextLoader();
		}
		
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		try {
			XtextLoader loader = new XtextLoader();
			return loader.toEMF(f.getFullFile());
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

}
