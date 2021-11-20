package mar.models.elysium;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.elysium.LilyPondStandaloneSetupGenerated;

import com.google.inject.Injector;

import mar.modelling.loader.ILoader;

public class LilypondLoader implements ILoader {

	@CheckForNull
	private static Injector injector = null;
	
	private static Injector getInjector() {
		if (injector == null)
			injector = new LilyPondStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
		return injector;
	}
	
	@Override
	public Resource toEMF(@Nonnull File f) {
		Injector injector = getInjector();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		Resource resource = resourceSet.getResource(URI.createFileURI(f.getAbsolutePath()), true);

		// The root element is a LilyPond object
		// LilyPond lilypond = (LilyPond) resource.getContents().get(0);
		return resource;
	}

}
