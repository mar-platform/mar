package mar.analysis.backend.megamodel.inspectors;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import mar.analysis.backend.megamodel.XtextInspector;
import mar.artefacts.ProjectInspector;
import mar.artefacts.acceleo.AcceleoInspector;
import mar.artefacts.atl.AnATLyzerFileInspector;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.db.RepositoryDB.RepoFile;
import mar.artefacts.epsilon.BuildFileInspector;
import mar.artefacts.epsilon.EpsilonInspector;
import mar.artefacts.epsilon.EpsilonLaunchInspector;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.henshin.HenshinInspector;
import mar.artefacts.ocl.OCLInspector;
import mar.artefacts.qvto.QvtoInspector;
import mar.artefacts.sirius.SiriusInspector;
import mar.validation.AnalysisDB;

public class InspectorLauncher {

	private final RepositoryDB db;
	private final Path repositoryDataFolder;
	private AnalysisDB analysisDb;

	public InspectorLauncher(RepositoryDB db, Path repositoryDataFolder, AnalysisDB analysisDb) {
		this.db = db;
		this.repositoryDataFolder = repositoryDataFolder;	
		this.analysisDb = analysisDb;
	}
	
	public Collection<RecoveryGraph> fromBuildFiles() throws SQLException {
		return doInspect("ant", (projectPath) -> new BuildFileInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
	
	public Collection<RecoveryGraph> fromLaunchFiles() throws SQLException {
		return doInspect("eclipse-launcher", (projectPath) -> new EpsilonLaunchInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
	
	public Collection<RecoveryGraph> fromQvtoFiles() throws SQLException {
		return doInspect("qvto", (projectPath) -> new QvtoInspector(repositoryDataFolder, projectPath, analysisDb));	
	}
	
	public Collection<RecoveryGraph> fromOclFiles() throws SQLException {
		return doInspect("ocl", (projectPath) -> new OCLInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
		
	public Collection<RecoveryGraph> fromXtextFiles() throws SQLException {
		return doInspect("xtext", (projectPath) -> new XtextInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
	
	public Collection<RecoveryGraph> fromEmfaticFiles() throws SQLException {
		return doInspect("emfatic", (projectPath) -> new EmfaticInspector(repositoryDataFolder, projectPath, analysisDb));
	}

	public Collection<RecoveryGraph> fromAcceleoFiles() throws SQLException {
		return doInspect("acceleo", (projectPath) -> new AcceleoInspector(repositoryDataFolder, projectPath, analysisDb));
	}

    public Collection<RecoveryGraph> fromATLFiles() throws SQLException {
		return doInspect("atl", (projectPath) -> new AnATLyzerFileInspector(repositoryDataFolder, projectPath, analysisDb));	
	}

    public Collection<RecoveryGraph> fromEpsilonFiles() throws SQLException {
		return doInspect("epsilon", (projectPath) -> new EpsilonInspector(repositoryDataFolder, projectPath, analysisDb));	
	}
    
    public Collection<RecoveryGraph> fromSirius() throws SQLException {
		return doInspect("sirius", (projectPath) -> new SiriusInspector(repositoryDataFolder, projectPath, analysisDb));		
	}

    public Collection<RecoveryGraph> fromHenshin() throws SQLException {
		return doInspect("henshin", (projectPath) -> new HenshinInspector(repositoryDataFolder, projectPath, analysisDb));
	}
    
	private Collection<RecoveryGraph> doInspect(String fileType, Function<Path, ProjectInspector> factory) throws SQLException {
		List<RecoveryGraph> result = new ArrayList<>();
		for (RepoFile model : db.getFilesByType(fileType)) {
			Path path = model.getRelativePath();
			Path fullPath = model.getFullPath();
			Path projectPath = model.getProjectPath();
						
			System.out.println("Analysing " + fileType.toUpperCase() + ": " + path);
			
			Preconditions.checkState(fullPath.isAbsolute());
			
			try {
				//QvtoInspector inspector = new QvtoInspector(repositoryDataFolder, projectPath);
				ProjectInspector inspector = factory.apply(projectPath);
				RecoveryGraph minigraph = inspector.process(fullPath.toFile());
				if (minigraph != null) {
					minigraph.assertValid();
					result.add(minigraph);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
