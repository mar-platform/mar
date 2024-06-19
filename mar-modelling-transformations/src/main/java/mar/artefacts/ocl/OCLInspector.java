package mar.artefacts.ocl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;

public class OCLInspector extends ProjectInspector {

	public OCLInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb, RepositoryDB repoDb) {
		super(repoFolder, projectSubPath, analysisDb, repoDb);
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
		
		return toMetamodel(import_, folder, ProjectInspector.AbsolutePathResolutionStrategy.RESOURCE_PREFIX);
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
