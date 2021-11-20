package mar.analysis.ecore;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import mar.modelling.loader.ILoader;

public class EcoreLoader implements ILoader {

	@Override
	public Resource toEMF(File file) throws IOException {
		ResourceSet rs = new ResourceSetImpl();
		return rs.getResource(URI.createFileURI(file.getAbsolutePath()), true);
	}

}
