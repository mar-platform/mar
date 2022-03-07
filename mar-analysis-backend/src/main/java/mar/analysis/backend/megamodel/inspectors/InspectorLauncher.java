package mar.analysis.backend.megamodel.inspectors;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import mar.analysis.backend.RepositoryDB;
import mar.analysis.backend.RepositoryDB.RepoFile;
import mar.analysis.backend.megamodel.XtextInspector;
import mar.artefacts.ProjectInspector;
import mar.artefacts.acceleo.AcceleoInspector;
import mar.artefacts.epsilon.BuildFileInspector;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.qvto.QvtoInspector;

public class InspectorLauncher {

	private final RepositoryDB db;
	private final Path repositoryDataFolder;

	public InspectorLauncher(RepositoryDB db, Path repositoryDataFolder) {
		this.db = db;
		this.repositoryDataFolder = repositoryDataFolder;	
	}
	
	public Collection<? extends RecoveryGraph> fromBuildFiles() throws SQLException {
		return doInspect("ant", (projectPath) -> new BuildFileInspector(repositoryDataFolder, projectPath));		
	}
	
	public Collection<? extends RecoveryGraph> fromQvtoFiles() throws SQLException {
		return doInspect("qvto", (projectPath) -> new QvtoInspector(repositoryDataFolder, projectPath));		
	}
	
	public Collection<? extends RecoveryGraph> fromXtextFiles() throws SQLException {
		return doInspect("xtext", (projectPath) -> new XtextInspector(repositoryDataFolder, projectPath));		
	}
	
	public Collection<? extends RecoveryGraph> fromEmfaticFiles() throws SQLException {
		return doInspect("emfatic", (projectPath) -> new EmfaticInspector(repositoryDataFolder, projectPath));		
	}

	public Collection<? extends RecoveryGraph> fromAcceleoFiles() throws SQLException {
		return doInspect("acceleo", (projectPath) -> new AcceleoInspector(repositoryDataFolder, projectPath));		
	}
	
	private Collection<? extends RecoveryGraph> doInspect(String extension, Function<Path, ProjectInspector> factory) throws SQLException {
		List<RecoveryGraph> result = new ArrayList<>();
		for (RepoFile model : db.getFilesByType(extension)) {
			Path path = model.getRelativePath();
			Path projectPath = model.getProjectPath();
						
			System.out.println("Analysing " + extension.toUpperCase() + ": " + path);
			
			try {
				//QvtoInspector inspector = new QvtoInspector(repositoryDataFolder, projectPath);
				ProjectInspector inspector = factory.apply(projectPath);
				RecoveryGraph minigraph = inspector.process(model.getFullPath().toFile());
				if (minigraph != null)
					result.add(minigraph);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
