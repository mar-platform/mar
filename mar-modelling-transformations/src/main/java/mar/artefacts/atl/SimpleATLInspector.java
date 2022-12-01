package mar.artefacts.atl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.search.FileSearcher;
import mar.validation.AnalysisDB;

/**
 * This inspector relies on module annotations to determine the meta-model.
 * <pre>
 * 		-- @path AnyMM=/AnyProject/AnyFolder/AnyMM.ecore
 * 		-- @nsURI UML=http://www.eclipse.org/uml2/2.1.0/UML
 * </pre>
 * @author jesus
 *
 * Other sources of meta-model information:
 * * .properties files associated with a Java class that executes the transformation
 * * .launch files
 * * build.xml
 * * detect typing errors and compare with other meta-models
 * 
 */
public class SimpleATLInspector extends ProjectInspector {

	private static final String NS_URI = "@nsURI";
	private static final String PATH   = "@path";

	private static final int NS_URI_LENGTH = NS_URI.length();
	private static final int PATH_LENGTH   = PATH.length();
	
	private final FileSearcher searcher;

	public SimpleATLInspector(@Nonnull Path repoFolder, @Nonnull Path projectSubPath, AnalysisDB db) {
		super(repoFolder, projectSubPath, db);
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
	}
	

	@Override
	public RecoveryGraph process(File f) throws Exception {
		ATLProgram program = new ATLProgram(new RecoveredPath(getRepositoryPath(f)));
		
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	line = line.stripLeading();
		    	
		    	// If we found a line starting with "module" we assume it is an ATL module
		    	if (line.startsWith("module"))
		    		return graph;
		    	
		    	if (line.startsWith("--")) {
		    		line = line.substring(2).stripLeading();
		    		if (line.startsWith(PATH)) {
		    			Metamodel mm = extractPath(line);
		    			graph.addMetamodel(mm);
		    			program.addMetamodel(mm, MetamodelReference.Kind.TYPED_BY);
		    		} else if (line.startsWith(NS_URI)) {
		    			Metamodel mm = extractURI(line);
		    			graph.addMetamodel(mm);
		    			program.addMetamodel(mm, MetamodelReference.Kind.TYPED_BY);
		    			// TODO: Check whether this is input or output
		    		}
		    	}
		    }
		}
		
		return null;
	}

	@Nonnull
	private Metamodel extractPath(String line) {
		Pair<String, String> parts = extractParts(line, PATH_LENGTH);
		String file = parts.getRight();
		// @path routes are normally rooted in the workspace, so we make it relative
		if (file.startsWith("/")) 
			file = file.substring(1);
		
		Path loosyPath = Paths.get(file);
		// Remove the project-specific part of the path because many time this is not in-sync with the actual folder
		loosyPath = loosyPath.subpath(1, loosyPath.getNameCount());
		
		RecoveredPath p = searcher.findFile(loosyPath);
		System.out.println("Recovered: " + p.getPath());
		return Metamodel.fromFile(parts.getLeft(), p);
	}

	@CheckForNull
	private Metamodel extractURI(String line) {
		Pair<String, String> parts = extractParts(line, NS_URI_LENGTH);
		return Metamodel.fromURI(parts.getLeft(), parts.getRight());
	}

	private Pair<String, String> extractParts(String line, int skip) {
		line = line.substring(skip).stripLeading();
		int idx = line.indexOf("=");
		if (idx == -1)
			return null;
		String name = line.substring(0, idx);
		String mm   = line.substring(idx + 1);
		return Pair.of(name, mm);
	}

}
