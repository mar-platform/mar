package mar.validation.bpmn;

import java.io.IOException;

import javax.annotation.CheckForNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import mar.modelling.loader.ILoader;
import mar.models.bpmn.BPMNLoader;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;

/**
 * Validator of GenMyModel BPMN models.
 * 
 * @author jesus
 *
 */
public class SingleGenMyModelBPMNValidator extends SingleEMFFileAnalyser {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(SingleGenMyModelBPMNValidator.class);

	public static final String ID = "genmymodel-bpmn";

	private final BPMNLoader loader = new BPMNLoader();
	
	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public String getId() {
			return ID;
		}
		
		@Override
		public void configureEnvironment() {
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		}
		
		@Override
		public ISingleFileAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new SingleGenMyModelBPMNValidator();
		}

		@Override
		public ILoader newLoader() {
			return new BPMNLoader();
		}				

	}
		
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		return loader.toEMF(f.getFullFile());
	}
}
