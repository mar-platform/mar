package mar.artefacts;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import javax.annotation.CheckForNull;

import mar.analysis.megamodel.model.Artefact.ArtefactStatus;
import mar.analysis.megamodel.model.Project;
import mar.artefacts.RecoveredPath.MissingPath;
import mar.artefacts.RecoveredPath.UnexpectedPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.search.FileSearcher;
import mar.artefacts.search.MetamodelSeacher;
import mar.artefacts.search.SearchCache;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;

public abstract class ProjectInspector {

	protected final Path repoFolder;
	protected final Path projectSubPath;
	protected final FileSearcher searcher;
	protected final AnalysisDB analysisDb;
	protected final MetamodelSeacher mmSearcher;

	public ProjectInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb, RepositoryDB rawRepoDb) {
		this.repoFolder = repoFolder;
		this.projectSubPath = projectSubPath;
		this.searcher = new FileSearcher(repoFolder, projectSubPath, rawRepoDb);
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
		if (BuiltinMetamodels.INSTANCE.isBuiltin(uri)) {
			return Metamodel.fromURI(name, uri, ArtefactStatus.BUILTIN);
		}
		
		Metamodel mm = tryFindURI(uri);
		if (mm != null)
			return mm;
		
		if (uri.startsWith("platform:/")) {
			return fromPlatformResource(uri);
		}
		
		return Metamodel.fromURI(uri, uri, ArtefactStatus.UNRESOLVED);
	}
	
	protected Metamodel toMetamodel(String uriOrFile, Path folder) {
		return toMetamodel(uriOrFile, folder, EMPTY_RESOLUTION_STRATEGY);
	}
	
	protected Metamodel toMetamodel(String uriOrFile, Path folder, AbsolutePathResolutionStrategy... resolutionStrategies) {
		uriOrFile = sanitize(uriOrFile);
		if (BuiltinMetamodels.INSTANCE.isBuiltin(uriOrFile)) {
			return Metamodel.fromURI(uriOrFile, uriOrFile, ArtefactStatus.BUILTIN);
		}
		
		Metamodel mm = tryFindURI(uriOrFile);
		if (mm != null)
			return mm;
		
		if (uriOrFile.startsWith("http")) {
			// This shouldn't happen, but in case, we have this fallback to detect URIs
			return Metamodel.fromURI(uriOrFile, uriOrFile, ArtefactStatus.UNRESOLVED);
		} else if (uriOrFile.startsWith("platform:/")) {
			return fromPlatformResource(uriOrFile);
		}
						
		Path p = folder.resolve(uriOrFile);
		if (! p.isAbsolute()) {
			p = repoFolder.resolve(p);
		}
 		if (Files.exists(p) && Files.isRegularFile(p)) {
 			p = getRepositoryPath(p); // Convert back to relative...
			// Heuristically... 			
			return Metamodel.fromFile(uriOrFile, RecoveredPath.newExistingPath(p, repoFolder));
		} else if (uriOrFile.startsWith("/") && resolutionStrategies.length > 0) {
			Path repoName = folder.subpath(0, 2);
			
			AbsolutePathResolutionStrategy matchedStrategy = null;
			for(AbsolutePathResolutionStrategy r : resolutionStrategies) {
				if (r.match(uriOrFile)) {
					if (matchedStrategy == null)
						matchedStrategy = r;
					
					RecoveredPath rp = r.tryRecover(repoFolder, repoName, uriOrFile);
					if (p != null) {
						return Metamodel.fromFile(uriOrFile, rp);
					}
				}
			}
			
			if (matchedStrategy != null) {
				Path expected = matchedStrategy.getExpectedPath(repoFolder, repoName, uriOrFile);
				return Metamodel.fromFile(uriOrFile, new MissingPath(expected));				
			}
		}
		
		// TODO: This is going to fail if uriOrFile is not absolute...
		//p = getRepositoryPath(Paths.get(uriOrFile));
		//if (Files.exists(p)) {
		//	// Heuristically...
		//	return Metamodel.fromFile(uriOrFile, new RecoveredPath(p));
		//} 
		
		// Which is a proper fallback?
		return Metamodel.fromFile(uriOrFile, new UnexpectedPath(folder.resolve(uriOrFile)));
	}

	@CheckForNull
	protected Metamodel tryFindURI(String uriOrFile) {
		List<Model> models = analysisDb.findByMetadata("nsURI", uriOrFile, s -> s);
		for(Model m : models) {
			if (m.getRelativePath().startsWith(projectSubPath)) {
				RecoveredPath p;
				Path rp = repoFolder.resolve(m.getRelativePath());
				if (Files.exists(rp)) {
					p = RecoveredPath.newExistingPath(m.getRelativePath(), repoFolder);
				} else {
					p = new RecoveredPath.MissingPath(m.getRelativePath());
				}
				
				return Metamodel.fromFile(uriOrFile, p);
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
		if (uriOrFile.endsWith("/#") || uriOrFile.endsWith("#/")) {
			return uriOrFile.substring(0, uriOrFile.length() - 2);
		} else if (uriOrFile.endsWith("#")) {
			return uriOrFile.substring(0, uriOrFile.length() - 1);
		} else if (uriOrFile.contains("#/")) {
		    int idx = uriOrFile.lastIndexOf("#/");
		    return (idx != -1) ? uriOrFile.substring(0, idx) : uriOrFile;
		}
		return uriOrFile;
	}

	
	@CheckForNull
	public abstract RecoveryGraph process(File f) throws Exception;

	private static AbsolutePathResolutionStrategy[] EMPTY_RESOLUTION_STRATEGY = new AbsolutePathResolutionStrategy[0];
	
	public static enum AbsolutePathResolutionStrategy {
		ABSOLUTE {
			@Override
			boolean match(String filePath) {				
				return filePath.startsWith("/");
			}

			@Override
			RecoveredPath tryRecover(Path repoFolder, Path repoName, String filePath) {
				Path p = getExpectedPath(repoFolder, repoName, filePath);
				Path absolute = repoFolder.resolve(p);
				if (Files.exists(absolute)) {
		 			return RecoveredPath.newExistingPath(p, repoFolder); 				
				}
				return null;
				
			}

			@Override
			Path getExpectedPath(Path repoFolder, Path repoName, String filePath) {
				return repoName.resolve(filePath.substring(1));
			}
		},
		RESOURCE_PREFIX {
			@Override
			boolean match(String uriOrFile) {
				return uriOrFile.startsWith("/resource/");
			}

			@Override
			RecoveredPath tryRecover(Path repoFolder, Path repoName, String filePath) {
				Path p = getExpectedPath(repoFolder, repoName, filePath);
				Path absolute = repoFolder.resolve(p);
				if (Files.exists(absolute)) {
		 			return RecoveredPath.newExistingPath(p, repoFolder); 				
				}
				return null;			
			}
			
			Path getExpectedPath(Path repoFolder, Path repoName, String filePath) {
				return repoName.resolve(filePath.replaceFirst("/resource/", ""));
			}
		},
		
		PLUGIN_PREFIX {
			@Override
			boolean match(String uriOrFile) {
				return uriOrFile.startsWith("/plugin/");
			}

			@Override
			RecoveredPath tryRecover(Path repoFolder, Path repoName, String filePath) {
				Path p = getExpectedPath(repoFolder, repoName, filePath);
				Path absolute = repoFolder.resolve(p);
				if (Files.exists(absolute)) {
		 			return RecoveredPath.newExistingPath(p, repoFolder); 				
				}
				return null;			
			}
			
			Path getExpectedPath(Path repoFolder, Path repoName, String filePath) {
				return repoName.resolve(filePath.replaceFirst("/plugin/", ""));
			}
		};

		abstract boolean match(String uriOrFile);

		abstract Path getExpectedPath(Path repoFolder, Path repoName, String uriOrFile);

		abstract RecoveredPath tryRecover(Path repoFolder, Path repoName, String filePath);
	}
}
