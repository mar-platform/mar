package mar.artefacts.atl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.tests.api.AtlLoader;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atl.util.ATLUtils.ModelInfo;
import anatlyzer.atlext.ATL.LibraryRef;
import anatlyzer.atlext.OCL.OclModel;
import anatlyzer.atlext.OCL.OclModelElement;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.MetamodelReference.Kind;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.RecoveredPath.MissingPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.search.FileSearcher;
import mar.artefacts.search.MetamodelSeacher;
import mar.artefacts.search.MetamodelSeacher.RecoveredMetamodelFile;
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
		
		List<ModelInfo> untyped = new ArrayList<ATLUtils.ModelInfo>();
		for (ModelInfo modelInfo : ATLUtils.getModelInfo(m)) {
			// Some ATL transformations are not annotated with meta-model information
			if (modelInfo.getURIorPath() == null) {
				untyped.add(modelInfo);
				continue;
			}
			
			Metamodel mm;
			if (modelInfo.isURI()) {
				mm = extractPath(modelInfo.getMetamodelName(), modelInfo.getURIorPath());
			} else {
    			mm = toMetamodel(modelInfo.getURIorPath(), getRepositoryPath(f).getParent());
			}
			
			List<MetamodelReference.Kind> kinds = toKinds(modelInfo);
			
			graph.addMetamodel(mm);
			program.addMetamodel(mm, kinds.toArray(MetamodelReference.EMPTY_KIND));
		}
		
		// There are models which are not annotated, for them we have other meta-models in the project
		if (untyped.size() > 0) {
			Map<ModelInfo, RecoveredMetamodelFile> recoveredMetamodels = checkUntyped(m, untyped);
			recoveredMetamodels.forEach((mi, mmFile) -> {
				Path repoFile = getRepositoryPath(mmFile.getBestMetamodel());				
				Metamodel mm = Metamodel.fromFile(mi.getMetamodelName(), new RecoveredPath(repoFile));
				graph.addMetamodel(mm);
				List<Kind> kinds = toKinds(mi);
				program.addMetamodel(mm, kinds.toArray(MetamodelReference.EMPTY_KIND));
			});
		}
		
		// Match libraries by name, looking up files in the project
		for (LibraryRef library : m.getRoot().getLibraries()) {
			Path file = getRepositoryPath(f).resolveSibling(library.getName() + ".atl");
			RecoveredPath recovered = searcher.findFile(file);
			if (recovered instanceof MissingPath) {
				// TODO: Try another strategy, by methods calls
			} else {
				program.addImportDependency(recovered.getPath());
			}
		}
		
		return graph;
	}


	private List<MetamodelReference.Kind> toKinds(ModelInfo modelInfo) {
		List<MetamodelReference.Kind> kinds = new ArrayList<>();
		kinds.add(MetamodelReference.Kind.TYPED_BY);
		if (modelInfo.isInput()) 
			kinds.add(MetamodelReference.Kind.INPUT_OF);
		if (modelInfo.isOutput()) 
			kinds.add(MetamodelReference.Kind.OUTPUT_OF);
		return kinds;
	}

	private Map<ModelInfo, RecoveredMetamodelFile> checkUntyped(ATLModel m, List<? extends ModelInfo> untyped) {
		Map<ModelInfo, RecoveredMetamodelFile> classFootprints = toClassFootprints(m, untyped);		
		MetamodelSeacher ms = new MetamodelSeacher(searcher);
		return ms.search(classFootprints);
		// TODO: I can use AnATLyzer to test the best ones
	}


	private Map<ModelInfo, RecoveredMetamodelFile> toClassFootprints(ATLModel m, List<? extends ModelInfo> untyped) {
		Map<String, ModelInfo> metamodels = untyped.stream().
				collect(Collectors.toMap(mi -> mi.getMetamodelName(), mi -> mi));
		Map<ModelInfo, RecoveredMetamodelFile> classFootprints = untyped.stream().
				collect(Collectors.toMap(name -> name, name -> new RecoveredMetamodelFile()));
		
		List<OclModelElement> elements = m.allObjectsOf(OclModelElement.class);
		for (OclModelElement me : elements) {
			OclModel model = me.getModel();
			if (model != null) {
				ModelInfo mi = metamodels.get(model.getName());
				if (mi != null) {
					classFootprints.get(mi).addToFootprint(me.getName());
				}
			}
		}
		
		return classFootprints;
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
