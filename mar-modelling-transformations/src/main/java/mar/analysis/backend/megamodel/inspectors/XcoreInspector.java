package mar.analysis.backend.megamodel.inspectors;

import java.io.File;
import java.nio.file.Path;

import mar.artefacts.Metamodel;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;


public class XcoreInspector extends ProjectInspector {

	public XcoreInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb, RepositoryDB repoDb) {
		super(repoFolder, projectSubPath, analysisDb, repoDb);
	}
	
	
	@Override
	public RecoveryGraph process(File f) throws Exception {
		RecoveryGraph graph = new RecoveryGraph(getProject());
		Path repoPath = getRepositoryPath(f);
		Metamodel mm = Metamodel.fromFile(f.getName(), RecoveredPath.newExistingPath(repoPath, repoFolder));
		graph.addMetamodel(mm);	
		return graph;
	}	

}
