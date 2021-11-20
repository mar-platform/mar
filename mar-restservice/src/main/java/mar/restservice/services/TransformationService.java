package mar.restservice.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.emfatic.core.EmfaticResource;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import mar.modelling.loader.ILoader;
import mar.restservice.services.SearchOptions.ModelType;
import mar.validation.AnalyserRegistry;
import mar.validation.AnalysisDB.Status;
import mar.validation.AnalysisResult;
import mar.validation.ResourceAnalyser.Factory;

/**
 * This service provides a fixed set of transformations
 * that can be useful.
 * 
 * @author jesus
 */
public class TransformationService {

	@CheckForNull
	private final AnalysisService analysis;
	
	/**
	 * For the moment this depends on the analysis service to make sure that a
	 * given input model doesn't crash the process. However, in the future
	 * both the model load and the transformation should be executed in 
	 * a specific sandbox.
	 * 
	 * @param service An analysis service. It is optional, if null we load without any guarantees.
	 */
	public TransformationService(@CheckForNull AnalysisService service) {
		this.analysis = service;
	}
	
	public String transformEcoreToEmfatic(File f, ModelType type) throws IOException {
		if (analysis != null) {
			AnalysisResult r = analysis.analyse(f, type);
			if (r.getStatus() == Status.CRASHED)
				return null;
		}
		
		Factory factory = AnalyserRegistry.INSTANCE.getFactory(type.name());
		ILoader loader = factory.newLoader();
		Resource res = loader.toEMF(f);
		
		return transformEcoreToEmfatic(res);
	}
	
	public String transformEcoreToEmfatic(Resource r) throws IOException {
		EmfaticResource resource = new EmfaticResource(URI.createURI("in-memory"));
		resource.getContents().addAll(r.getContents());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();		
		resource.save(bos, null);
		return bos.toString();
	}
}
