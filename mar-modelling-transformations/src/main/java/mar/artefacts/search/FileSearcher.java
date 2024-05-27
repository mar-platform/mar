package mar.artefacts.search;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.text.similarity.LevenshteinDistance;

import mar.artefacts.RecoveredPath;
import mar.artefacts.RecoveredPath.HeuristicPath;
import mar.artefacts.db.RepositoryDB;

public class FileSearcher {

	private final Path projectRoot;
	private final Path projectSubPath;
	private final Path repoRoot;
	@CheckForNull
	private SearchCache cache;
	private RepositoryDB rawdb;

	public FileSearcher(@Nonnull Path repoRoot, @Nonnull Path projectSubPath, RepositoryDB rawdb) {
		this.repoRoot = repoRoot;
		this.projectSubPath = projectSubPath;
		this.projectRoot = repoRoot.resolve(projectSubPath);
		this.rawdb = rawdb;
	}
	
	public void setCache(SearchCache cache) {
		this.cache = cache;
	}
	
	public Path getRepoRoot() {
		return repoRoot;
	}

	@CheckForNull
	public RecoveredPath findFile(Path loosyPath) {
		final String loosyPathStr = loosyPath.toString();
		String filename = loosyPath.getName(loosyPath.getNameCount() - 1).toString();
		String projectName = projectSubPath.toString();
		try {
			List<String> files = rawdb.getFiles(projectName, "%" + filename);
			if (files.isEmpty())
				return findFileInFilesystem(loosyPath);

			files.sort((f1, f2) -> Integer.compare(distance(loosyPathStr, f1), distance(loosyPathStr, f2)));
			return new HeuristicPath(Paths.get(files.get(0)));
		} catch (SQLException e) {
			e.printStackTrace();
			return findFileInFilesystem(loosyPath);
		}
	}
	
	@CheckForNull
	public RecoveredPath findFileInFilesystem(Path loosyPath) {
		if (Files.exists(projectRoot.resolve(loosyPath))) {
			return new RecoveredPath(repoRoot.relativize(projectRoot.resolve(loosyPath)));
		}
		Path filename = loosyPath.getName(loosyPath.getNameCount() - 1);
		try {
			Optional<Path> match = Files.walk(projectRoot)
								.filter(f -> ! f.startsWith("."))
								.filter(f -> f.endsWith(filename))
								.map(p -> repoRoot.relativize(p))
								.sorted((f1, f2) -> Integer.compare(distance(loosyPath, f1), distance(loosyPath, f2)))
								.findFirst();
			// I could return the alternatives as well
			return match.<RecoveredPath>map(p -> new HeuristicPath(p)).orElseGet(() -> new RecoveredPath.MissingPath(loosyPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	public RecoveredPath findPotentiallyGeneratedFile(Path folderPath, String filename) {
		Path path = folderPath.resolve(filename);
		Path relPath = projectRoot.relativize(path);
		if (Files.exists(path)) {
			return new RecoveredPath(relPath);
		}
		return new RecoveredPath.GeneratedPath(relPath);
	}

	public boolean fileExistsInFolder(Path folderPath, String filename) {
		Path p = repoRoot.resolve(folderPath).resolve(filename);
		return Files.exists(p);
	}
	
	public List<Path> findFilesByExtension(String extension) throws IOException {
		if (cache != null) {
			List<Path> result = cache.getFilesByExtension(projectRoot, extension);
			if (result != null)
				return result;
		}
		
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*." + extension);
		List<Path> result = Files.walk(projectRoot).
				filter(f -> matcher.matches(f)).
				map(p -> repoRoot.relativize(p)).
				collect(Collectors.toList());
		
		if (cache != null)
			cache.putFilesByExtension(projectRoot, extension, result);
		
		return result;
	}
	
	private static LevenshteinDistance DISTANCE_ALG = new LevenshteinDistance();
	
	protected static int distance(Path loosyPath, Path projectFilePath) {
		return distance(loosyPath.toString(), projectFilePath.toString());
	}
	
	protected static int distance(String loosyPath, String projectFilePath) {
		return DISTANCE_ALG.apply(loosyPath, projectFilePath);
	}
	
}
