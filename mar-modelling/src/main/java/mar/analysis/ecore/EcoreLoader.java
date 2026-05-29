package mar.analysis.ecore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
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
	
	public Resource toEMF(String content) throws IOException {
        ResourceSet rs = new ResourceSetImpl();
        Resource resource = rs.createResource(URI.createURI("uri"));
        resource.load(IOUtils.toInputStream(content, Charset.defaultCharset()), null);
        return resource;
		
	}

}
