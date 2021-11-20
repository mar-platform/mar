package mar.analysis.uml;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import mar.modelling.loader.ILoader;

public class UMLLoader implements ILoader {

	@Override
	public Resource toEMF(File file) throws IOException {
		UMLResourceFactoryImpl factory = new UMLResourceFactoryImpl();
		Resource r = factory.createResource(URI.createFileURI(file.getAbsolutePath()));
		try {
			r.load(null);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		return r;
	}

}
