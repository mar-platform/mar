package mar.models.xtext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		XtextAnalysisResult r = doAnalysis(f);		
		Collection<? extends EPackage> pkgs = r.getPackages();
		Resource res = new XMIResourceImpl(URI.createFileURI(f.getAbsolutePath() + ".ecore"));
	    for (EPackage pkg : pkgs) {
			res.getContents().add(pkg);
		}
	    return res;
	}
	
	@Nonnull
	public XtextAnalysisResult doAnalysis(@Nonnull File f) throws IOException {
	    List<String> lines = Files.readAllLines(f.toPath());
	    
	    Set<String> importedURIs = new HashSet<>();
	    Set<String> generatedURIs = new HashSet<>();
	    
	    boolean hasGenerate = false;
	    StringBuffer sb = new StringBuffer();
	    for (String line: lines) {
	    	boolean isImport = line.startsWith("import");
	    	boolean isGenerate = line.startsWith("generate");
	    	
	    	if (isGenerate) {	    		
	    		// The format is 'generate pkgName uri' 
	    		String[] parts = line.split("\\s*");
	    		if (parts.length > 2) {
	    			String uri = parts[2];
	    			for(int i = 3; i < parts.length; i++) {
	    				uri += parts[i];
	    			}
	    			generatedURIs.add(uri);
	    		}
	    	} else if (isImport) {
	    		String uri = line.substring(5).trim();
	    		importedURIs.add(uri);
	    	}
	    	
	    	if (line.contains("http://www.eclipse.org/emf/2002/Ecore"))
	    		continue;
	    	
			if (isImport) {
				line = line.replaceFirst("import", "generate");
			} else if (isGenerate) {
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
	    
	    return new XtextAnalysisResult(g, transformer.getGeneratedPackages(), generatedURIs, importedURIs);
	}
	
	public static class XtextAnalysisResult {
		private Collection<EPackage> packages;
		private Grammar grammar;
		private Collection<String> generatedURIs;
		private Collection<String> importedURIs;

		public XtextAnalysisResult(Grammar g, Collection<EPackage> packages, Collection<String> generatedURIs, Collection<String> importedURIs) {
			this.grammar = g;
			this.packages = packages;
			this.generatedURIs = generatedURIs;
			this.importedURIs = importedURIs;			
		}
		
		public Grammar getGrammar() {
			return grammar;
		}
		
		public Collection<? extends EPackage> getPackages() {
			return packages;
		}
		
		public Collection<? extends String> getImportedURIs() {
			return importedURIs;
		}
		
		public Collection<? extends String> getGeneratedURIs() {
			return generatedURIs;
		}
	}
}
