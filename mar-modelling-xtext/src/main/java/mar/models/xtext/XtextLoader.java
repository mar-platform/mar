package mar.models.xtext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.XtextStandaloneSetupGenerated;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xtext.ecoreInference.Xtext2EcoreTransformer;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;

import mar.modelling.loader.ILoader;

public class XtextLoader implements ILoader {
	
	@CheckForNull
	private static Injector injector = null;
	
	private static Injector getInjector() {
		if (injector == null) {
			XtextStandaloneSetup.doSetup();
			injector = new XtextStandaloneSetupGenerated().createInjector();
		}
		return injector;
	}
	    
	@Nonnull
	public Resource toEMF(@Nonnull File f) throws IOException {
		List<EPackage> pkgs = extractMetamodel(f);
		Resource res = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath() + ".ecore"));
	    for (EPackage pkg : pkgs) {
			res.getContents().add(pkg);
		}
	    return res;
	}
	
	@Nonnull
	public List<EPackage> extractMetamodel(@Nonnull File f) throws IOException {
	    List<String> lines = Files.readAllLines(f.toPath());
	    
	    boolean hasGenerate = false;
	    StringBuffer sb = new StringBuffer();
	    for (String line: lines) {
	    	if (line.contains("http://www.eclipse.org/emf/2002/Ecore"))
	    		continue;
	    	
			if (line.startsWith("import")) {
				line = line.replaceFirst("import", "generate");
			} else if (line.startsWith("generate")) {
				hasGenerate = true;
				break;
			}
			sb.append(line).append("\n");
		}
	    
	    
	    InputStream stream;
		if (hasGenerate) {
			stream = new FileInputStream(f);
	    } else {
	    	stream = new ByteArrayInputStream(sb.toString().getBytes());
	    }	    
	    
	    IResourceFactory resourceFactory = getInjector().getInstance(IResourceFactory.class);
	    Resource r = resourceFactory.createResource(URI.createFileURI(f.getAbsolutePath()));
	    r.load(stream, null);
	    
	    Provider<XtextResourceSet> xtextResourceSetProvider = getInjector().getInstance(new Key<Provider<XtextResourceSet>>() {});
	    XtextResourceSet resourceSet = xtextResourceSetProvider.get();
	    resourceSet.getResources().add(r);
	    
	    Grammar g = (Grammar) r.getContents().get(0);
	    
	    Xtext2EcoreTransformer transformer = new Xtext2EcoreTransformer(g);
	    transformer.transform();	    
	    
	    return transformer.getGeneratedPackages();
	}
}
