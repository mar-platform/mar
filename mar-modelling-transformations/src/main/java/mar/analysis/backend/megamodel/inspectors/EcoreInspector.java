package mar.analysis.backend.megamodel.inspectors;

import java.io.File;
import java.nio.file.Path;

import mar.artefacts.Metamodel;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import mar.validation.AnalysisDB.Status;


public class EcoreInspector extends ProjectInspector {

	public EcoreInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb, RepositoryDB repoDb) {
		super(repoFolder, projectSubPath, analysisDb, repoDb);
	}
	
	
	@Override
	public RecoveryGraph process(File f) throws Exception {
		RecoveryGraph graph = new RecoveryGraph(getProject());
		Path repoPath = getRepositoryPath(f);
		Model m = analysisDb.getModelByPath(repoPath.toString(), (s) -> s);
		if (m != null) {
			Metamodel mm = Metamodel.fromFile(f.getName(), new RecoveredPath(m.getRelativePath()));
			graph.addMetamodel(mm);	
			return graph;
		} else {
			Status s = analysisDb.hasFile(repoPath.toString());
			if (s != null) {
				// TODO: Mark the meta-model with metadata, like it is erroneous or something like this
				Metamodel mm = Metamodel.fromFile(f.getName(), new RecoveredPath(repoPath));
				graph.addMetamodel(mm);	
				return graph;
			}
			System.out.println("Not found in analysisDb: " + f.getAbsolutePath());
		}
	
		return null;
	}	

}
