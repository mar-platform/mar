package mar.analysis.backend.megamodel.inspectors;

import java.io.File;
import java.nio.file.Path;

import mar.analysis.duplicates.HashDuplicates;
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
			Metamodel mm = Metamodel.fromFile(f.getName(), RecoveredPath.newExistingPath(m.getRelativePath(), repoFolder));
			mm.setHash(HashDuplicates.toHash(f));
			graph.addMetamodel(mm);
			addDependencies(repoPath, m, mm);
			return graph;
		} else {
			Status s = analysisDb.hasFile(repoPath.toString());
			if (s != null) {
				// TODO: Mark the meta-model with metadata, like it is erroneous or something like this
				Metamodel mm = Metamodel.fromFile(f.getName(), RecoveredPath.newExistingPath(repoPath, repoFolder));
				graph.addMetamodel(mm);	
				return graph;
			}
			System.out.println("Not found in analysisDb: " + f.getAbsolutePath());
		}
	
		return null;
	}

	private void addDependencies(Path repoPath, Model m, Metamodel mm) {
		String uris = m.getKeyValueMetadata("externalURIs");
		if (uris != null && ! uris.isBlank()) {
			String[] externalURIs = uris.split(",");
			for (String externalURI : externalURIs) {
				externalURI = normalize(externalURI, repoPath);
				Metamodel dep = tryFindURI(externalURI);
				if (dep != null) {
					mm.addDependent(mm);
				}
			}
		}
	}

	private String normalize(String externalURI, Path repoPath) {
		String path = repoPath.toString();
		if (! path.endsWith("/")) 
			path = path + "/";
		int idx = externalURI.indexOf(path);
		if (idx != -1) {
			return externalURI.substring(idx + path.length());
		}
		return externalURI;
	}	

}
