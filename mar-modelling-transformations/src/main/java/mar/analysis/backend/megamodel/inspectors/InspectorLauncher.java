package mar.analysis.backend.megamodel.inspectors;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import mar.analysis.backend.megamodel.XtextInspector;
import mar.artefacts.FileProgram;
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
	private Predicate<RepoFile> filter;
	
	public InspectorLauncher(RepositoryDB db, Path repositoryDataFolder, AnalysisDB analysisDb) {
		this.db = db;
		this.repositoryDataFolder = repositoryDataFolder;	
		this.analysisDb = analysisDb;
	}
	
	public InspectorLauncher withFilter(Predicate<RepoFile> filter) {
		this.filter = filter;
		return this;
	}
	
	public InspectorResult fromBuildFiles() throws SQLException {
		return doInspect("ant", (projectPath) -> new BuildFileInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
	
	public InspectorResult fromLaunchFiles() throws SQLException {
		return doInspect("eclipse-launcher", (projectPath) -> new EpsilonLaunchInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
	
	public InspectorResult fromQvtoFiles() throws SQLException {
		return doInspect("qvto", (projectPath) -> new QvtoInspector(repositoryDataFolder, projectPath, analysisDb));	
	}
	
	public InspectorResult fromOclFiles() throws SQLException {
		return doInspect("ocl", (projectPath) -> new OCLInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
		
	public InspectorResult fromXtextFiles() throws SQLException {
		return doInspect("xtext", (projectPath) -> new XtextInspector(repositoryDataFolder, projectPath, analysisDb));		
	}
	
	public InspectorResult fromEmfaticFiles() throws SQLException {
		return doInspect("emfatic", (projectPath) -> new EmfaticInspector(repositoryDataFolder, projectPath, analysisDb));
	}

	public InspectorResult fromAcceleoFiles() throws SQLException {
		return doInspect("acceleo", (projectPath) -> new AcceleoInspector(repositoryDataFolder, projectPath, analysisDb));
	}

    public InspectorResult fromATLFiles() throws SQLException {
		return doInspect("atl", (projectPath) -> new AnATLyzerFileInspector(repositoryDataFolder, projectPath, analysisDb));	
	}

    public InspectorResult fromEpsilonFiles() throws SQLException {
		return doInspect("epsilon", (projectPath) -> new EpsilonInspector(repositoryDataFolder, projectPath, analysisDb));	
	}
    
    public InspectorResult fromSirius() throws SQLException {
		return doInspect("sirius", (projectPath) -> new SiriusInspector(repositoryDataFolder, projectPath, analysisDb));		
	}

    public InspectorResult fromHenshin() throws SQLException {
		return doInspect("henshin", (projectPath) -> new HenshinInspector(repositoryDataFolder, projectPath, analysisDb));
	}
    
	private InspectorResult doInspect(String fileType, Function<Path, ProjectInspector> factory) throws SQLException {
		InspectorResult result = new InspectorResult();
		for (RepoFile model : db.getFilesByType(fileType)) {
			Path path = model.getRelativePath();
			Path fullPath = model.getFullPath();
			Path projectPath = model.getProjectPath();
						
			if (filter != null && !filter.test(model)) {
				continue;
			}
			
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
			} catch (InspectionErrorException e) {
				result.addError(e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static class InspectorResult {
		private List<RecoveryGraph> graphs = new ArrayList<>();
		private List<InspectionErrorException> errors = new ArrayList<>();
		
		public void add(RecoveryGraph minigraph) {
			graphs.add(minigraph);
		}

		public void addError(InspectionErrorException e) {
			errors.add(e);
		}
		
		public List<RecoveryGraph> getGraphs() {
			return graphs;
		}
		
		public List<InspectionErrorException> getErrors() {
			return errors;
		}
	}
}
