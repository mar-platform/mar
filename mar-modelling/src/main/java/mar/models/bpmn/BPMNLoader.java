package mar.models.bpmn;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import mar.modelling.loader.ILoader;
import mar.modelling.xmi.LooseLoadingXMIResource;

public class BPMNLoader implements ILoader {

	private static ResourceSet BPMN = null;

	public Resource load(@Nonnull String xmi) throws IOException {
		initBPMN();
		Resource r = new XMIResourceImpl();
		r.load(new ByteArrayInputStream(xmi.getBytes()), null);
		return r;
	}

	@Override
	public Resource toEMF(@Nonnull File xmi) throws IOException {
		initBPMN();
		LooseLoadingXMIResource r = new LooseLoadingXMIResource(URI.createFileURI(xmi.getAbsolutePath()));
		r.load(new FileInputStream(xmi), null);
		// r.getFeatureNotFound().forEach(f -> System.out.println("Not found: " + f));
		return r;
	}	
	
	public static ResourceSet initBPMN() {
		if (BPMN != null) {
			return BPMN;
		}
		
		ResourceSet rs = new ResourceSetImpl();
		readResource(rs, "/models/DC.ecore");
		readResource(rs, "/models/DI.ecore");
		readResource(rs, "/models/BPMNDI.ecore");
		readResource(rs, "/models/BPMN20.ecore");
		return rs;
	}

	private static Resource readResource(ResourceSet rs, @Nonnull String name) {
		URL url = BPMNLoader.class.getResource(name);
		if (url == null)
			throw new IllegalStateException("Can't access " + name);
		try {
			Resource r = rs.createResource(URI.createURI(name));
			r.load(url.openStream(), null);
			r.getAllContents().forEachRemaining(o -> {
				if (o instanceof EPackage) {
					EPackage pkg = (EPackage) o;
					EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
				}
			});
			return r;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
