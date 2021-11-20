package mar.analysis.rds;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import mar.modelling.loader.ILoader;

public class RdsLoader implements ILoader {
	private static EPackage rdsPackage;
	
	public EPackage loadPackage() {
		if (rdsPackage == null) {
			rdsPackage = readResource("/models/rds/rds_manual.ecore");
		}
		return rdsPackage;
	}
	
	@Override
	public Resource toEMF(@Nonnull File f) {
		EPackage pkg = loadPackage();
		ResourceSet rs = new ResourceSetImpl();
		rs.getPackageRegistry().put(pkg.getNsURI(), pkg);
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource resource = rs.getResource(URI.createFileURI(f.getAbsolutePath()), true);
		return resource;
	}

		
	/* Copied from BPMNLoader */
	private static EPackage readResource(@Nonnull String name) {
		URL url = RdsLoader.class.getResource(name);
		if (url == null)
			throw new IllegalStateException("Can't access " + name);
		try {
			Resource r = new XMIResourceImpl();
			r.load(url.openStream(), null);
			return (EPackage) r.getContents().get(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
