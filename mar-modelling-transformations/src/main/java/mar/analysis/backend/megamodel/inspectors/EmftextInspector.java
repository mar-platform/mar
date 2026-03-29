package mar.analysis.backend.megamodel.inspectors;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agrona.SystemUtil;

import mar.analysis.backend.megamodel.inspectors.EmftextInspector.EmfTextParser.Result;
import mar.analysis.duplicates.HashDuplicates;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;


public class EmftextInspector extends ProjectInspector {


	public EmftextInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb, RepositoryDB rawRepoDb) {
		super(repoFolder, projectSubPath, analysisDb, rawRepoDb);
	}

	@Override
	public RecoveryGraph process(File f) throws Exception {
		RecoveryGraph graph = new RecoveryGraph(getProject());
		
		EmftextProgram p = new EmftextProgram(RecoveredPath.newExistingPath(getRepositoryPath(f), repoFolder));		
		p.setHash(HashDuplicates.toHash(f));
		graph.addProgram(p);
		
		Result result = EmfTextParser.parse(f.toPath());
		
		List<String> uris = new ArrayList<>(result.imports);
		uris.add(result.mainUri);
		
		for (String uri : uris) {
			Metamodel mm = toMetamodel(uri, getRepositoryPath(f).getParent());
			graph.addMetamodel(mm);
			p.addMetamodel(mm, MetamodelReference.Kind.IMPORT, MetamodelReference.Kind.TYPED_BY);
		}
		
		for (String file : result.syntaxUris) {
			Path path = getRepositoryPath(f).resolveSibling(file);
			RecoveredPath recovered = getFileSearcher().findFile(path);
			p.addImportDependency(recovered);
		}
		
		return graph;
	}
	
	public static class EmftextProgram extends FileProgram {

		public EmftextProgram(RecoveredPath path) {
			super(path);
		}
		
		@Override
		public String getKind() {
			return "emftext";
		}
		
		@Override
		public String getCategory() {
			return "textual-syntax";
		}
		
	}
	
	public static class EmfTextParser {

	    private static final Pattern MAIN_URI =
	            Pattern.compile("FOR\\s*<([^>]+)>");

	    private static final Pattern IMPORT_URI =
	            Pattern.compile(":\\s*<([^>]+)>");

	    private static final Pattern SYNTAX_URI =
	            Pattern.compile("WITH\\s+SYNTAX\\s+\\w+\\s*<([^>]+)>",
	                    Pattern.CASE_INSENSITIVE);


	    public static class Result {
	        String mainUri;
	        List<String> imports = new ArrayList<>();
	        List<String> syntaxUris = new ArrayList<>();
	    }

	    public static Result parse(Path file) throws Exception {
	    	List<String> lines = Files.readAllLines(file);
	    	return parse(lines);
	    }
	    
	    public static Result parse(List<String> lines) throws Exception {
	    	Result result = new Result();
	        boolean insideImports = false;

	        for (String line : lines) {

	            String trimmed = line.trim();

	            // --- main metamodel ---
	            Matcher mainMatcher = MAIN_URI.matcher(trimmed);
	            if (mainMatcher.find()) {
	                result.mainUri = mainMatcher.group(1);
	            }

	            // --- detect imports block ---
	            if (trimmed.startsWith("IMPORTS")) {
	                insideImports = true;
	                continue;
	            }

	            if (insideImports && trimmed.startsWith("}")) {
	                insideImports = false;
	                continue;
	            }

	            if (!insideImports) {
	                continue;
	            }

	            // --- import URI ---
	            Matcher importMatcher = IMPORT_URI.matcher(trimmed);
	            if (importMatcher.find()) {
	            	var imp = importMatcher.group(1);
	            	if (imp != null) {
	            		result.imports.add(imp);
	            	} else {
	            		System.out.println("Error parsing import in Emftext");
	            	}
	            }

	            // --- syntax URI ---
	            Matcher syntaxMatcher = SYNTAX_URI.matcher(trimmed);
	            if (syntaxMatcher.find()) {
	            	var imp = syntaxMatcher.group(1);
	            	if (imp != null) {
	            		result.syntaxUris.add(imp);
	            	} else {
	            		System.out.println("Error parsing syntax dependency in Emftext");
	            	}
	            }
	        }

	        return result;
	    }
	}

}
