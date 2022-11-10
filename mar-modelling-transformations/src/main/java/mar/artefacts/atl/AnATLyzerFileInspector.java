package mar.artefacts.atl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.tests.api.AtlLoader;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atl.util.ATLUtils.ModelInfo;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.epsilon.FileSearcher;
import mar.artefacts.graph.RecoveryGraph;
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
public class AnATLyzerFileInspector extends ProjectInspector {

	private static final String NS_URI = "@nsURI";
	private static final String PATH   = "@path";

	private static final int NS_URI_LENGTH = NS_URI.length();
	private static final int PATH_LENGTH   = PATH.length();
	
	private final FileSearcher searcher;

	public AnATLyzerFileInspector(@Nonnull Path repoFolder, @Nonnull Path projectSubPath, AnalysisDB analysisDb) {
		super(repoFolder, projectSubPath, analysisDb);
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
	}
	

	@Override
	public RecoveryGraph process(File f) throws Exception {
		ATLProgram program = new ATLProgram(new RecoveredPath(getRepositoryPath(f)));
		
		Resource trafo = AtlLoader.load(f.getAbsolutePath());
		ATLModel m = new ATLModel(trafo, trafo.getURI().toFileString(), true);
		
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		for (ModelInfo modelInfo : ATLUtils.getModelInfo(m)) {
			// Some ATL transformations are not annotated with meta-model information
			if (modelInfo.getURIorPath() == null)
				continue;
			
			Metamodel mm;
			if (modelInfo.isURI()) {
				mm = extractPath(modelInfo.getMetamodelName(), modelInfo.getURIorPath());
			} else {
    			mm = toMetamodel(modelInfo.getURIorPath(), getRepositoryPath(f).getParent());
			}
			
			List<MetamodelReference.Kind> kinds = new ArrayList<>();
			kinds.add(MetamodelReference.Kind.TYPED_BY);
			if (modelInfo.isInput()) 
				kinds.add(MetamodelReference.Kind.INPUT_OF);
			if (modelInfo.isOutput()) 
				kinds.add(MetamodelReference.Kind.OUTPUT_OF);
			
			graph.addMetamodel(mm);
			program.addMetamodel(mm, kinds.toArray(MetamodelReference.EMPTY_KIND));
		}
		
		return graph;
	}

	@Nonnull
	private Metamodel extractPath(String name, String file) {
		// @path routes are normally rooted in the workspace, so we make it relative
		if (file.startsWith("/")) 
			file = file.substring(1);
		
		Path loosyPath = Paths.get(file);
		// Remove the project-specific part of the path because many time this is not in-sync with the actual folder
		loosyPath = loosyPath.subpath(1, loosyPath.getNameCount());
		
		RecoveredPath p = searcher.findFile(loosyPath);
		System.out.println("Recovered: " + p.getPath());
		return Metamodel.fromFile(name, p);
	}

}
