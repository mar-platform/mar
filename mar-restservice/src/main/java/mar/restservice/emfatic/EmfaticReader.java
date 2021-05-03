package mar.restservice.emfatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.emfatic.core.EmfaticResource;

public class EmfaticReader {

	public EmfaticReader() {
				
	}
	
	public Resource read(String text) throws IOException {
		EmfaticResource resource = new EmfaticResource(URI.createURI("in-memory"));
		resource.load(new ByteArrayInputStream(text.getBytes()), null);
		return resource;
	}
	
}
