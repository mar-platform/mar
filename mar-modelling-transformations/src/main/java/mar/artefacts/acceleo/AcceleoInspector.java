package mar.artefacts.acceleo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;

public class AcceleoInspector extends ProjectInspector {

	public AcceleoInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb) {
		super(repoFolder, projectSubPath, analysisDb);
	}

	/**
	 * Extract meta-moel URIs from Acceleo files by inspecting line by line:
	 * 
	 * 		Syntax: [module <module_name>('metamodel_URI_1', 'metamodel_URI_2')]
	 */
	@Override
	public RecoveryGraph process(File f) throws Exception {
	    List<String> lines = Files.readAllLines(f.toPath());
	    List<String> uris = Collections.emptyList();
	    for (String line : lines) {
	    	uris = getURIs(line);
	    	if (! uris.isEmpty()) {
	    		break;
	    	}
		}

	    if (uris.isEmpty())
	    	return null;
	    
		AcceleoProgram program = new AcceleoProgram(new RecoveredPath(getRepositoryPath(f)));
		
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		for (String uri : uris) {
			Metamodel mm = toMetamodel(uri, getRepositoryPath(f).getParent());
			graph.addMetamodel(mm);
			program.addMetamodel(mm, MetamodelReference.Kind.TYPED_BY);
		}
		
		return graph;
	}

	private static Pattern pattern = Pattern.compile("^\\[\\s*module");
	
	@VisibleForTesting
	static List<String> getURIs(String line) {
		line = line.stripLeading();
		Matcher matcher = pattern.matcher(line);
		
		if (! matcher.find())
			return Collections.emptyList();
		
		int start = line.indexOf("(");
		if (start == -1)
			return Collections.emptyList();
		
		int end   = line.lastIndexOf(")");
		if (end == -1)
			return Collections.emptyList();;
		
		
		List<String> uris = new ArrayList<String>();
		String[] parts = line.substring(start + 1, end).split(",");
		for (String part : parts) {
			part = part.trim();
			int startSubstr = 0;
			int endSubstr = part.length();
			if (part.startsWith("'") || part.startsWith("\""))
				startSubstr = 1;
			if (part.endsWith("'") || part.endsWith("\""))
				endSubstr = part.length() - 1;
			
			String uri = part.substring(startSubstr, endSubstr);
			uris.add(uri);
		}
		
		return uris;
	}
	
}
