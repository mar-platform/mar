package mar.artefacts.search;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.text.similarity.LevenshteinDistance;

import mar.artefacts.RecoveredPath;
import mar.artefacts.RecoveredPath.HeuristicPath;

public class FileSearcher {

	private final Path projectRoot;
	private final Path repoRoot;
	@CheckForNull
	private SearchCache cache;

	public FileSearcher(@Nonnull Path repoRoot, @Nonnull Path projectRoot) {
		this.repoRoot = repoRoot;
		this.projectRoot = projectRoot;
	}
	
	public void setCache(SearchCache cache) {
		this.cache = cache;
	}
	
	public Path getRepoRoot() {
		return repoRoot;
	}
	
	@CheckForNull
	public RecoveredPath findFile(Path loosyPath) {
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

	public RecoveredPath findInFolder(Path folderPath, String filename) {
		Path path = folderPath.resolve(filename);
		if (Files.exists(path))
			return new HeuristicPath(path);
		return new RecoveredPath.MissingPath(path);
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
	
	protected static int distance(Path loosyPath, Path projectFilePath) {
		LevenshteinDistance distance = new LevenshteinDistance();
		return distance.apply(loosyPath.toString(), projectFilePath.toString());
	}

	
}
