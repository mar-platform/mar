package mar.analysis.emfatic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.emfatic.core.EmfaticResource;

import com.google.common.io.Files;

import mar.modelling.loader.ILoader;

public class EmfaticReader implements ILoader {

	public EmfaticReader() {
				
	}
	
	public Resource read(String text) throws IOException {
		EmfaticResource resource = new EmfaticResource(URI.createURI("in-memory"));
		resource.load(new ByteArrayInputStream(text.getBytes()), null);
		return resource;
	}

	@Override
	public Resource toEMF(File file) throws IOException {
		return read(Files.asCharSource(file, Charset.defaultCharset()).read());
	}
	
}
