package mar.artefacts.qvto;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.internal.qvt.oml.cst.ImportCS;
import org.eclipse.m2m.internal.qvt.oml.cst.MappingModuleCS;
import org.eclipse.m2m.internal.qvt.oml.cst.UnitCS;
import org.eclipse.ocl.cst.PathNameCS;
import org.eclipse.ocl.cst.SimpleNameCS;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.Transformation.Qvto;
import mar.artefacts.Transformation.TransformationParameter;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;

public class QvtoInspector extends ProjectInspector {

	//private final EcoreRepository ecoreRepository;

	public QvtoInspector(Path repoFolder, Path projectPath, AnalysisDB analysisDb) {
		super(repoFolder, projectPath, analysisDb);
		//this.ecoreRepository = new EcoreRepository(metamodels, repoFolder.toFile());
	}

	@Override
	public RecoveryGraph process(File qvtoFile) throws Exception {
		//Qvto tmp = new QvtoLoader().load(qvtoFile.getAbsolutePath(), Collections.emptyList());
		//Collection<String> expectedMetamodels = tmp.getMetamodelURIs();

		UnitCS unit = new QvtoLoader().parse(qvtoFile.getAbsolutePath(), Collections.emptyList());
		Collection<TransformationParameter> expectedMetamodels = Qvto.getModelParameters(unit, qvtoFile.toString());		
		
		Path qvtoFilePath = getRepositoryPath(qvtoFile);
		QvtoProgram program = new QvtoProgram(new RecoveredPath(qvtoFilePath));
		
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		for (TransformationParameter p : expectedMetamodels) {
			// Do I have a way to extract the logical name of the meta-model from QVTo file?
			Metamodel mm = toMetamodel(p.getUri(), qvtoFilePath.getParent());
			graph.addMetamodel(mm);
			
			List<MetamodelReference.Kind> kinds = new ArrayList<>();
			kinds.add(MetamodelReference.Kind.TYPED_BY);
			// kinds.add(MetamodelReference.Kind.IMPORT);
			if (p.isIn()) 
				kinds.add(MetamodelReference.Kind.INPUT_OF);
			if (p.isOut()) 
				kinds.add(MetamodelReference.Kind.OUTPUT_OF);

			program.addMetamodel(mm, kinds.toArray(MetamodelReference.EMPTY_KIND));
		}
		
		MappingModuleCS mod = (MappingModuleCS) unit.getTopLevelElements().get(0);
		
		for (ImportCS import_ : mod.getImports()) {
			PathNameCS pathName = import_.getPathNameCS();
			if (pathName != null) {
				List<SimpleNameCS> names = pathName.getSimpleNames();
				if (names.size() > 0) {
					String name = names.stream().map(n -> n.getValue()).collect(Collectors.joining("/")) + ".qvto";					
					if (getFileSearcher().fileExistsInFolder(qvtoFilePath.getParent(), name)) {
						Path path = qvtoFilePath.getParent().resolve(name);
						program.addImportDependency(path);
					}
				}
			}
		}

		Resource r = unit.eResource();
		if (r != null) {
			r.unload();
		}
		
		return graph;
	}

}
