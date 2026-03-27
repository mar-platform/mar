package mar.artefacts.acceleo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.annotations.VisibleForTesting;

import mar.analysis.backend.megamodel.inspectors.InspectionErrorException;
import mar.analysis.duplicates.HashDuplicates;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.RecoveredPath.MissingPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;

public class AcceleoInspector extends ProjectInspector {

	public AcceleoInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb, RepositoryDB repoDb) {
		super(repoFolder, projectSubPath, analysisDb, repoDb);
	}

	/**
	 * Extract meta-moel URIs from Acceleo files by inspecting line by line:
	 * 
	 * 		Syntax: [module <module_name>('metamodel_URI_1', 'metamodel_URI_2')]
	 */
	@Override
	public RecoveryGraph process(File f) throws Exception {
		AcceleoProgram program = new AcceleoProgram(RecoveredPath.newExistingPath(getRepositoryPath(f), repoFolder));
		program.setHash(HashDuplicates.toHash(f));

		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		List<String> uris = getURIs(f);
	    if (uris == null)
			throw new InspectionErrorException.SyntaxError(program);

	    // This means that it is an MTL but we really couldn't get the URIs 
	    if (uris.isEmpty())
	    	return graph;
	    
		for (String uri : uris) {
			Metamodel mm = toMetamodel(uri, getRepositoryPath(f).getParent(), ProjectInspector.AbsolutePathResolutionStrategy.ABSOLUTE);
			graph.addMetamodel(mm);
			program.addMetamodel(mm, MetamodelReference.Kind.TYPED_BY, MetamodelReference.Kind.INPUT_OF);
		}
		
		// TODO: Implement import dependencies
		// For example: [import me::mysoft::acceleo::sample::files::generate /]
		// Look in a folder named me/mysoft/acceleo/sample/files/generate.mtl
		
		for (String importPiece : getImports(f)) {
			String pathRelativeToSrc = importPiece.replace("::", "/") + ".mtl";
			Path srcFolder = getFileSearcher().findParentFolder(getRepositoryPath(f), "src");
			if (srcFolder != null) {
				if (getFileSearcher().fileExistsInFolder(srcFolder, pathRelativeToSrc)) {
					Path path = srcFolder.resolve(pathRelativeToSrc);						
					program.addImportDependency(RecoveredPath.newExistingPath(path, repoFolder));
				}
			} else {
				RecoveredPath r = getFileSearcher().findFile(Path.of(pathRelativeToSrc));
				if (!(r instanceof MissingPath)) {
					program.addImportDependency(r);				
				}
			}
		}
				
		
		return graph;
	}
	
	private static List<String> getURIs(File f) throws IOException {
		return getURIs(new FileReader(f));
	}
	
	@VisibleForTesting
	protected static List<String> getURIs(Reader reader) throws IOException {
		boolean hasModule = false;
		String transformationName = null;
		List<String> uris = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(reader)) {
			int c;
			MAIN: while((c = br.read()) != -1) {
				char character = (char) c;
				if (character == '[') {
					c = skipWhitespace(br);
					if (((char) c) != 'm')
						continue MAIN;
					
					char[] buffer = new char[5];
					int read = br.read(buffer);
					if (read != 5 || ! "odule".equals(new String(buffer))) {
						continue MAIN;
					}
					
					hasModule = true;
					
					c = skipWhitespace(br);

					// Read until "("
					StringBuilder name = new StringBuilder();
					while(c != -1) {
						if (((char) c) == '(') {
							break;
						} else {
							name.append((char) c);
						}
						c = br.read();
					}
					
					if (c == -1)
						return Collections.emptyList();
					
					transformationName = name.toString();
					
					while (true) {
						c = skipWhitespace(br);

						// This should be '
						if (c == -1 || ((char) c) != '\'')
							return Collections.emptyList();
	
						// Read everything until next '
						StringBuilder uri = new StringBuilder();
						while((c = br.read()) != -1) {
							if (c == '\'') {
								break;
							} else {
								uri.append((char) c);
							}
						}			
						
						if (c == -1)
							return Collections.emptyList();
					
						uris.add(uri.toString());
						c = skipWhitespace(br);
	
						if (c == -1)
							return Collections.emptyList();
						else if (((char) c) == ')')
							break MAIN;
						else if (((char) c) == ',')
							continue;
						else
							return Collections.emptyList();
						
					}
					
				}

			}
		}

		if (! hasModule) {
			return null;
		}
		
		return uris;
	}
	
	private static int skipWhitespace(BufferedReader br) throws IOException {
		int c;
		while((c = br.read()) != -1) {
			if (! Character.isWhitespace(c)) {
				return c;
			}
		}
		return -1;
	}

	private List<String> getImports(File f) {
		List<String> result = new ArrayList<String>();
		try (BufferedReader buffer = new BufferedReader(new FileReader(f))) {
		    String line;
		    while ((line = buffer.readLine()) != null) {
		    	line = line.stripLeading();
		    	int index = line.indexOf("[import");
		    	if (index != -1) {
		    		int last = line.lastIndexOf("/]");
		    		if (last != -1) {
		    			int prefixSize = "[import".length();
		    			String importData = line.substring(index + prefixSize, last).trim();
		    			result.add(importData);
		    		}
		    	} else if (line.startsWith("[template")) {
		    		break;
		    	}
		    }	
		} catch (Exception e) {
			System.out.println("Acceleo inspector error - reading imports");
			e.printStackTrace();
			return Collections.emptyList();
		}
		return result;
	}
	
}
