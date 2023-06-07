package mar.models.xtext;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
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
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xtext.ecoreInference.Xtext2EcoreTransformer;

import com.google.common.annotations.VisibleForTesting;
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
			injector = new XtextStandaloneSetupGenerated().createInjectorAndDoEMFRegistration();
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

	public static class XtextInfo {
		private List<String> generatedURIs = new ArrayList<>();
		private List<String> importedURIs = new ArrayList<>();
		private boolean syntaxErrors = false;
		
		public void setSyntaxErrors(boolean b) {
			this.syntaxErrors = b;
		}
		
		public boolean SyntaxErrors() {
			return syntaxErrors;
		}
		
		public List<? extends String> getGeneratedURIs() {
			return generatedURIs;
		}
		
		public List<? extends String> getImportedURIs() {
			return importedURIs;
		}

	}

	public XtextInfo getInfo(File filePath) throws IOException {
		XtextInfo info = new XtextInfo();

		IParser parser = getInjector().getInstance(IParser.class);
		IParseResult parseResult = parser.parse(new FileReader(filePath));
		if (parseResult.hasSyntaxErrors()) {
			info.setSyntaxErrors(true);
		}

		BufferedReader reader = new BufferedReader(new StringReader(parseResult.getRootNode().getText()));
		String line;
		while ((line = reader.readLine()) != null) {
			boolean isImport = line.startsWith("import");
			boolean isGenerate = line.startsWith("generate");

			if (isGenerate) {
				String uri = extractGeneratedURI(line);
				if (uri != null)
					info.generatedURIs.add(uri);
			} else if (isImport) {
				String uri = extractImportedURI(line);
				if (uri != null)
					info.importedURIs.add(uri);
			}
			// TODO: Try to break as soon as possible for efficiency
		}

		return info;
	}

	@Nonnull
	public XtextAnalysisResult doAnalysis(@Nonnull File f) throws IOException {
		List<String> lines = Files.readAllLines(f.toPath());

		Set<String> importedURIs = new HashSet<>();
		Set<String> generatedURIs = new HashSet<>();

		boolean hasGenerate = false;
		StringBuffer sb = new StringBuffer();
		for (String line : lines) {
			boolean isImport = line.startsWith("import");
			boolean isGenerate = line.startsWith("generate");

			if (isGenerate) {
				String uri = extractGeneratedURI(line);
				if (uri != null)
					generatedURIs.add(uri);
			} else if (isImport) {
				String uri = extractImportedURI(line);
				if (uri != null)
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

		Provider<XtextResourceSet> xtextResourceSetProvider = getInjector()
				.getInstance(new Key<Provider<XtextResourceSet>>() {
				});
		XtextResourceSet resourceSet = xtextResourceSetProvider.get();
		resourceSet.getResources().add(r);

		Grammar g = (Grammar) r.getContents().get(0);

		Xtext2EcoreTransformer transformer = new Xtext2EcoreTransformer(g);
		transformer.transform();

		return new XtextAnalysisResult(g, transformer.getGeneratedPackages(), generatedURIs, importedURIs);
	}

	@VisibleForTesting
	@CheckForNull
	/* pp */ static String extractImportedURI(@Nonnull String line) {
		String uri = line.substring(6).trim();
		uri = uri.replaceFirst("^\"", "");

		int idx = uri.indexOf("\"");
		if (idx != -1) {
			return uri.substring(0, idx);
		}

		return null;
	}

	@VisibleForTesting
	@CheckForNull
	/* pp */ static String extractGeneratedURI(String line) {
		// The format is 'generate pkgName uri'
		String[] parts = line.split("\\s+");
		if (parts.length > 2) {
			String uri = parts[2];
			for (int i = 3; i < parts.length; i++) {
				uri += parts[i];
			}
			uri = uri.replaceFirst("^\"", "").replaceFirst("\"$", "");
			return uri;
		}
		return null;
	}

	public static class XtextAnalysisResult {
		private Collection<EPackage> packages;
		private Grammar grammar;
		private Collection<String> generatedURIs;
		private Collection<String> importedURIs;

		public XtextAnalysisResult(Grammar g, Collection<EPackage> packages, Collection<String> generatedURIs,
				Collection<String> importedURIs) {
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
