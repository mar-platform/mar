package mar.analysis.xtext;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.models.xtext.XtextLoader;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;

public class XtextAnalyser extends SingleEMFFileAnalyser {

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
		
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		try {
			XtextLoader loader = new XtextLoader();
			return loader.load(f.getFullFile());
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

}
