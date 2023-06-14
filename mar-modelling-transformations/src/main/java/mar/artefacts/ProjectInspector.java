package mar.artefacts;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.CheckForNull;

import mar.analysis.megamodel.model.Project;
import mar.artefacts.RecoveredPath.HeuristicPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.search.FileSearcher;
import mar.artefacts.search.MetamodelSeacher;
import mar.artefacts.search.SearchCache;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;

public abstract class ProjectInspector {

	protected final Path repoFolder;
	protected final Path projectSubPath;
	private final FileSearcher searcher;
	private final AnalysisDB analysisDb;
	private final MetamodelSeacher mmSearcher;

	public ProjectInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb) {
		this.repoFolder = repoFolder;
		this.projectSubPath = projectSubPath;
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
		this.analysisDb = analysisDb;
		this.mmSearcher = new MetamodelSeacher(searcher, analysisDb, (p) -> getRepositoryPath(p));		
	}
	
	public void setSharedCache(SearchCache cache) {
		this.mmSearcher.setCache(cache);
		this.searcher.setCache(cache);
	}
	
	protected MetamodelSeacher getMetamodelSearcher() {
		return mmSearcher;
	}

	protected Path getProjectFolder() {
		return repoFolder.resolve(projectSubPath);
	}

	protected Path getRepositoryPath(File f) {
		return getRepositoryPath(f.toPath());
	}
	
	protected Path getRepositoryPath(Path p) {
		return getRepositoryPath(repoFolder, p);
	}
	
	public static Path getRepositoryPath(Path repoFolder, Path p) {		
		return repoFolder.relativize(p);
	}

	protected Project getProject() {
		String id = projectSubPath.subpath(0, 2).toString();
		return new Project(id);
	}
	
	protected FileSearcher getFileSearcher() {
		return searcher;
	}
	
	protected Metamodel toMetamodelFromURI(String name, String uri) {
		uri = sanitize(uri);
		Metamodel mm = tryFindURI(uri);
		if (mm != null)
			return mm;
		
		if (uri.startsWith("platform:/")) {
			return fromPlatformResource(uri);
		}
		
		return Metamodel.fromURI(uri, uri);
	}
	
	protected Metamodel toMetamodel(String uriOrFile, Path folder) {
		uriOrFile = sanitize(uriOrFile);
		
		Metamodel mm = tryFindURI(uriOrFile);
		if (mm != null)
			return mm;
		
		if (uriOrFile.startsWith("http")) {
			// This shouldn't happen, but in case, we have this fallback to detect URIs
			return Metamodel.fromURI(uriOrFile, uriOrFile);
		} else if (uriOrFile.startsWith("platform:/")) {
			return fromPlatformResource(uriOrFile);
		}
						
		Path p = folder.resolve(uriOrFile);
		if (! p.isAbsolute()) {
			p = repoFolder.resolve(p);
		}
 		if (Files.exists(p)) {
 			p = getRepositoryPath(p); // Convert back to relative...
			// Heuristically...
			return Metamodel.fromFile(uriOrFile, new RecoveredPath(p));
		} 
		
		// TODO: This is going to fail if uriOrFile is not absolute...
		//p = getRepositoryPath(Paths.get(uriOrFile));
		//if (Files.exists(p)) {
		//	// Heuristically...
		//	return Metamodel.fromFile(uriOrFile, new RecoveredPath(p));
		//} 
		
		// Which is a proper fallback?
		return Metamodel.fromFile(uriOrFile, new HeuristicPath(folder.resolve(uriOrFile)));
	}

	@CheckForNull
	private Metamodel tryFindURI(String uriOrFile) {
		List<Model> models = analysisDb.findByMetadata("nsURI", uriOrFile, s -> s);
		for(Model m : models) {
			if (m.getRelativePath().startsWith(projectSubPath)) {
				return Metamodel.fromFile(uriOrFile, new RecoveredPath(m.getRelativePath()));
			}
		}
		return null;
	}
	
	private Metamodel fromPlatformResource(String uri) {
		String file = uri.replace("platform:/resource/", "");
		
		Path loosyPath = Paths.get(file);
		// Remove the project-specific part of the path because many time this is not in-sync with the actual folder
		// Not sure that this is a good idea, because then findFile cannot reason with the full path
		// loosyPath = loosyPath.subpath(1, loosyPath.getNameCount());
		
		RecoveredPath p = getFileSearcher().findFile(loosyPath);
		return Metamodel.fromFile(p.toString(), p);		
	}
	
	private String sanitize(String uriOrFile) {
		if (uriOrFile.endsWith("/#"))
			return uriOrFile.substring(0, uriOrFile.length() - 2);
		if (uriOrFile.endsWith("#"))
			return uriOrFile.substring(0, uriOrFile.length() - 1);		
		return uriOrFile;
	}

	
	@CheckForNull
	public abstract RecoveryGraph process(File f) throws Exception;

}
