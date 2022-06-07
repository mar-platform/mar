package mar.artefacts.qvto;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.m2m.internal.qvt.oml.cst.UnitCS;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.Transformation.Qvto;
import mar.artefacts.graph.RecoveryGraph;

public class QvtoInspector extends ProjectInspector {

	//private final EcoreRepository ecoreRepository;

	public QvtoInspector(Path repoFolder, Path projectPath /* , AnalysisDB metamodels */) {
		super(repoFolder, projectPath);
		//this.ecoreRepository = new EcoreRepository(metamodels, repoFolder.toFile());
	}

	@Override
	public RecoveryGraph process(File qvtoFile) throws Exception {
		//Qvto tmp = new QvtoLoader().load(qvtoFile.getAbsolutePath(), Collections.emptyList());
		//Collection<String> expectedMetamodels = tmp.getMetamodelURIs();

		UnitCS unit = new QvtoLoader().parse(qvtoFile.getAbsolutePath(), Collections.emptyList());
		Collection<String> expectedMetamodels = Qvto.getMetamodelURIs(unit, qvtoFile.toString());		
		
		QvtoProgram program = new QvtoProgram(new RecoveredPath(getRepositoryPath(qvtoFile)));
		
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		for (String uri : expectedMetamodels) {
			// Do I have a way to extract the logical name of the meta-model from QVTo file?
			Metamodel mm = Metamodel.fromURI(uri, uri);
			graph.addMetamodel(mm);
			program.addMetamodel(mm, MetamodelReference.Kind.IMPORT, MetamodelReference.Kind.TYPED_BY);
		}
		
		return graph;
	}

}
