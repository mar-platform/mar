package mar.analysis.uml;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import mar.modelling.loader.ILoader;

public class UMLLoader implements ILoader {
	
	@Override
	public Resource toEMF(File file) throws IOException {
		ResourceSetImpl impl = new ResourceSetImpl();		
		impl.getResourceFactoryRegistry().getExtensionToFactoryMap().put("uml", new UMLResourceFactoryImpl());
		impl.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new UMLResourceFactoryImpl());
		impl.getPackageRegistry().put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);

		return impl.getResource(URI.createFileURI(file.getAbsolutePath()), true);
	}

}
