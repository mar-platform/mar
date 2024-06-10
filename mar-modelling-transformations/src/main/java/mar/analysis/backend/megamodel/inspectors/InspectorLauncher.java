package mar.analysis.backend.megamodel.inspectors;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.backend.megamodel.Ignored;
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
import mar.artefacts.search.SearchCache;
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

	public InspectorResult fromEcoreFiles() throws SQLException {
		return doInspect("ecore", (projectPath) -> new EcoreInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}
	
	public InspectorResult fromBuildFiles() throws SQLException {
		return doInspect("ant", (projectPath) -> new BuildFileInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromLaunchFiles() throws SQLException {
		return doInspect("eclipse-launcher",
				(projectPath) -> new EpsilonLaunchInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromQvtoFiles() throws SQLException {
		return doInspect("qvto", (projectPath) -> new QvtoInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromOclFiles() throws SQLException {
		return doInspect("ocl", (projectPath) -> new OCLInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromXtextFiles() throws SQLException {
		return doInspect("xtext", (projectPath) -> new XtextInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromEmfaticFiles() throws SQLException {
		return doInspect("emfatic",
				(projectPath) -> new EmfaticInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromAcceleoFiles() throws SQLException {
		return doInspect("acceleo",
				(projectPath) -> new AcceleoInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromATLFiles() throws SQLException {
		return doInspect("atl",
				(projectPath) -> new AnATLyzerFileInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromEpsilonFiles() throws SQLException {
		return doInspect("epsilon",
				(projectPath) -> new EpsilonInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromSirius() throws SQLException {
		return doInspect("sirius", (projectPath) -> new SiriusInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	public InspectorResult fromHenshin() throws SQLException {
		return doInspect("henshin",
				(projectPath) -> new HenshinInspector(repositoryDataFolder, projectPath, analysisDb, db));
	}

	private InspectorResult doInspect(String fileType, Function<Path, ProjectInspector> factory) throws SQLException {
		InspectorResult result = new InspectorResult();
		
		// The cache can only be shared among inspectors here, because it is not thread-safe
		SearchCache cache = new SearchCache();
		
		for (RepoFile model : db.getFilesByType(fileType)) {
			Path path = model.getRelativePath();
			Path fullPath = model.getFullPath();
			Path projectPath = model.getProjectPath();

			if (filter != null && !filter.test(model)) {
				result.addIgnored(new Ignored(ProjectInspector.getRepositoryPath(repositoryDataFolder, fullPath), "filtered-by-content"));
				continue;
			}

			System.out.println("Analysing " + fileType.toUpperCase() + ": " + path);

			Preconditions.checkState(fullPath.isAbsolute());

			try {
				// QvtoInspector inspector = new QvtoInspector(repositoryDataFolder,
				// projectPath);
				ProjectInspector inspector = factory.apply(projectPath);
				inspector.setSharedCache(cache);
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

	public Map<ArtefactType, InspectorResult> execute(int numThreads) throws Exception {
		InspectorLauncher inspector = this;

		Map<ArtefactType, Callable<InspectorResult>> tasks = new LinkedHashMap<>();
		tasks.put(ArtefactType.ECORE, inspector::fromEcoreFiles);
		tasks.put(ArtefactType.EPSILON, inspector::fromEpsilonFiles);
		tasks.put(ArtefactType.ANT, inspector::fromBuildFiles);
		tasks.put(ArtefactType.LAUNCH, inspector::fromLaunchFiles);
		tasks.put(ArtefactType.QVTO, inspector::fromQvtoFiles);
		tasks.put(ArtefactType.OCL, inspector::fromOclFiles);
		tasks.put(ArtefactType.XTEXT, inspector::fromXtextFiles);
		tasks.put(ArtefactType.EMFATIC, inspector::fromEmfaticFiles);
		tasks.put(ArtefactType.ACCELEO, inspector::fromAcceleoFiles);
		tasks.put(ArtefactType.ATL, inspector::fromATLFiles);
		tasks.put(ArtefactType.SIRIUS, inspector::fromSirius);
		tasks.put(ArtefactType.HENSHIN, inspector::fromHenshin);
		
		Map<ArtefactType, InspectorResult> results = new HashMap<>();
		if (numThreads <= 1) {
			for (Entry<ArtefactType, Callable<InspectorResult>> entry : tasks.entrySet()) {
				results.put(entry.getKey(), entry.getValue().call());
			}
		} else {
			ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
			
			// Schedule everything
			Map<ArtefactType, Future<InspectorResult>> futures = new LinkedHashMap<>();
			for (Entry<ArtefactType, Callable<InspectorResult>> entry : tasks.entrySet()) {
				Future<InspectorResult> future = executorService.submit(entry.getValue());
				futures.put(entry.getKey(), future);
			}
			
			// Wait until all of the finish storing the results
			for (Entry<ArtefactType, Future<InspectorResult>> entry2 : futures.entrySet()) {
				results.put(entry2.getKey(), entry2.getValue().get());
			}
			
			executorService.shutdown();
		}
		return results;
	}

	public static class InspectorResult {
		private List<RecoveryGraph> graphs = new ArrayList<>();
		private List<Ignored> ignored = new ArrayList<>();
		private List<InspectionErrorException> errors = new ArrayList<>();

		public void add(RecoveryGraph minigraph) {
			graphs.add(minigraph);
		}

		public void addIgnored(Ignored i) {
			ignored.add(i);
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
		
		public List<Ignored> getIgnored() {
			return ignored;
		}
	}
}
