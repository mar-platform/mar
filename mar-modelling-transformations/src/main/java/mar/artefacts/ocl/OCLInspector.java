package mar.artefacts.ocl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.RecoveredPath.HeuristicPath;
import mar.artefacts.atl.ATLProgram;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;

public class OCLInspector extends ProjectInspector {

	private AnalysisDB analysisDb;

	public OCLInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb) {
		super(repoFolder, projectSubPath);
		this.analysisDb = analysisDb;
	}

	@Override
	public RecoveryGraph process(File f) throws Exception {
		// This attempt to parse and the imports doesn't work. It seems that they don't appear in the CSTNode
		// OCL ocl = OCL.newInstance();
		// String text = IOUtils.toString(new FileInputStream(f), Charset.defaultCharset());
		// OCLAnalyzer<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> analyzer = ocl.createAnalyzer(text);
		// CSTNode cs = analyzer.parseConcreteSyntax();
		
		Path filePathInRepo = getRepositoryPath(f);
		OclProgram program = new OclProgram(new RecoveredPath(filePathInRepo));
		
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		List<Metamodel> metamodels = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	line = line.stripLeading();
		    	
		    	// If we found a line starting with "module" we assume it is an ATL module
		    	if (line.startsWith("import")) {
		    		Metamodel mm = importToMetamodel(line, filePathInRepo.getParent());
		    		if (mm != null)
		    			metamodels.add(mm);
		    	} else if (line.startsWith("package"))
		    		break;		    	
		    }
		}
		
		for (Metamodel metamodel : metamodels) {
			graph.addMetamodel(metamodel);
			program.addMetamodel(metamodel, MetamodelReference.Kind.TYPED_BY);
		}
		
		return graph;
	}

	@CheckForNull
	private Metamodel importToMetamodel(String line, Path folder) {
		String import_ = parseImport(line);
		if (import_ == null)
			return null;
		
		return toMetamodel(import_, folder);
	}
	
	private Metamodel toMetamodel(String uriOrFile, Path folder) {
		uriOrFile = sanitize(uriOrFile);
		
		List<Model> models = analysisDb.findByMetadata("nsURI", uriOrFile, s -> s);
		if (models.isEmpty()) {
			if (uriOrFile.startsWith("http")) {
				// This shouldn't happen, but in case, we have this fallback to detect URIs
				return Metamodel.fromURI(uriOrFile, uriOrFile);
			} else if (uriOrFile.startsWith("platform:/")) {
				return fromPlatformResource(uriOrFile);
			}
						
			return Metamodel.fromFile(uriOrFile, new RecoveredPath(folder.resolve(uriOrFile)));
		} else {
			for(Model m : models) {
				if (m.getRelativePath().startsWith(projectSubPath)) {
					return Metamodel.fromFile(uriOrFile, new RecoveredPath(m.getRelativePath()));
				}
			}
		}		
		
		// Which is a proper fallback?
		return Metamodel.fromFile(uriOrFile, new HeuristicPath(folder.resolve(uriOrFile)));
	}
	
	private Metamodel fromPlatformResource(String uri) {
		String file = uri.replace("platform:/resource/", "");
		
		Path loosyPath = Paths.get(file);
		// Remove the project-specific part of the path because many time this is not in-sync with the actual folder
		loosyPath = loosyPath.subpath(1, loosyPath.getNameCount());
		
		RecoveredPath p = getFileSearcher().findFile(loosyPath);
		return Metamodel.fromFile(p.toString(), p);		
	}
	
	private String sanitize(String uriOrFile) {
		if (uriOrFile.endsWith("/#"))
			return uriOrFile.substring(0, uriOrFile.length() - 2);
		if (uriOrFile.endsWith("#"))
			return uriOrFile.substring(0, uriOrFile.length() - 1);		
		return uriOrFile;
	}

	private String parseImport(String line) {
		char delimiter = '\'';
		int start = line.indexOf(delimiter);
		if (start == -1) { 
			delimiter = '"'; 
			start = line.indexOf(delimiter);
		}
		
		if (start == -1)
			return null;
		
		int end = line.indexOf(delimiter, start + 1);
		if (end == -1)
			return null;
		
		return line.substring(start + 1, end);		
	}


}