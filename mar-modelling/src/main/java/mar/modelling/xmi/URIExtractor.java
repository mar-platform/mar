package mar.modelling.xmi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.google.common.annotations.VisibleForTesting;

import mar.analysis.ecore.EcoreRepository;
import mar.analysis.ecore.EcoreRepository.EcoreModel;

public class URIExtractor {

	private static final Pattern pattern = Pattern.compile("xmlns(:([^\\s=]+))?=\"([^\"]+)\"");
	
	@Nonnull
	private final List<String> uris;
	@Nonnull
	private final File file;

	
	public URIExtractor(File file, List<String> uris) {
		this.file = file;
		this.uris = new ArrayList<>(uris);
	}

	@Nonnull
	private static List<String> extractURIs(@Nonnull File f) throws IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("xmlns:xmi=\"http://www.omg.org/XMI\"")) {
					return matchURIs(line);
				}
			}
		}
		return Collections.emptyList();
	}

	
	@VisibleForTesting
	/* pp */ static List<String> matchURIs(@Nonnull String line) {
		List<String> result = new ArrayList<>();
		Matcher matcher = newMatcher(line);
		while (matcher.find()) {
			// @CheckForNull String name = matcher.group(2);
			String uri = matcher.group(3);						
			result.add(uri);
		}
		return result;
	}

	@VisibleForTesting
	/* pp */ static Matcher newMatcher(@Nonnull String line) {
		return pattern.matcher(line);
	}

	public static URIExtractor fromXMI(@Nonnull File f) throws IOException {
		List<String> uris = extractURIs(f);
		return new URIExtractor(f, uris);
	}

	public boolean tryLoad(@Nonnull EcoreRepository repo) {
		System.out.println(file);
		// Given URIS A, B and C, each URI may have associated zero or more files
		// For example:
		//       A => fileA1, fileA2
		//       B => fileB1, fileB2, fileB3
		//       C => fileC
		// We need to generate are combinations of three models, that is:
		//
		//       (fileA1, fileB1, fileC), (fileA1, fileB2, fileC), (fileA1, fileB3, fileC), 
		//       (fileA2, fileB1, fileC), (fileA2, fileB2, fileC), (fileA2, fileB3, fileC),
		// 
		List<List<EcoreModel>> combinations = new ArrayList<>(); 
		for(String uri : uris) {
			if (isStandardIgnore(uri)) 
				continue;
			
			List<EcoreModel> metamodels = repo.findEcoreByURI(uri);
			if (metamodels.isEmpty()) {
				System.out.println("Can't find: " + uri);
				return false;
			}
				
			if (combinations.isEmpty()) {
				combinations.add(metamodels);
			} else {
				List<List<EcoreModel>> newCombinations = new ArrayList<>(combinations.size() * metamodels.size());
				for (List<EcoreModel> combination : combinations) {
					for (EcoreModel mm : metamodels) {
						List<EcoreModel> extended = new ArrayList<>(combination);
						extended.add(mm);
						newCombinations.add(extended);
					}
				}
				combinations = newCombinations;
			}			
		}
		
		System.out.println(uris);
		
		ResourceSet rs = new ResourceSetImpl();
		
		// Now we have all meta-models that we want to load in the ResourceSet
		// and we try in turn
		for (List<EcoreModel> metamodels : combinations) {
			try {
				for (EcoreModel mm : metamodels) {
					Resource r = mm.load(rs);
					TreeIterator<EObject> it = r.getAllContents();
					while (it.hasNext()) {
						EObject obj = it.next();
						if (obj instanceof EPackage) {
							EPackage pkg = (EPackage) obj;						
							rs.getPackageRegistry().put(pkg.getNsURI(), pkg);
						}
					}
				}
			
				Resource r = rs.getResource(URI.createFileURI(file.getAbsolutePath()), true);
				System.out.println("Loaded!");
				System.out.println(r);
				return true;
			} catch(Exception e) {
				e.printStackTrace();
			}			
		}
		
		return false;
	}

	private boolean isStandardIgnore(String uri) {
		return uri.equals("http://www.omg.org/XMI") || uri.equals("http://www.w3.org/2001/XMLSchema-instance");
	}
	
}
