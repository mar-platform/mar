package mar.models.pnml;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.sculptor.dsl.SculptordslStandaloneSetup;

import com.google.inject.Injector;

import mar.modelling.loader.ILoader;

/**
 * 
 * Doc: https://dev.lip6.fr/trac/research/ISOIEC15909/wiki/English/User/Import
 * 
 * @author jesus
 *
 */
public class SculptorLoader implements ILoader {

	@CheckForNull
	private static Injector injector = null;
	
	private static Injector getInjector() {
		if (injector == null)			
			injector = new SculptordslStandaloneSetup().createInjectorAndDoEMFRegistration();
		return injector;
	}
	
	@Override
	public Resource toEMF(@Nonnull File f) {
		Injector injector = getInjector();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		Resource resource = resourceSet.getResource(URI.createFileURI(f.getAbsolutePath()), true);

		return resource;
	}

}
