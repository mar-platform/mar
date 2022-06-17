package mar.artefacts.epsilon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.artefacts.RecoveredPath;
import mar.artefacts.RecoveredPath.HeuristicPath;

public class FileSearcher {

	private final Path projectRoot;
	private final Path repoRoot;

	public FileSearcher(@Nonnull Path repoRoot, @Nonnull Path projectRoot) {
		this.repoRoot = repoRoot;
		this.projectRoot = projectRoot;
	}
	
	@CheckForNull
	public RecoveredPath findFile(Path loosyPath) {
		Path filename = loosyPath.getName(loosyPath.getNameCount() - 1);
		try {
			Optional<Path> match = Files.walk(projectRoot)
								.filter(f -> ! f.startsWith("."))
								.filter(f -> f.endsWith(filename))
								.map(p -> repoRoot.relativize(p))
								.sorted((f1, f2) -> -1 * Integer.compare(similarity(loosyPath, f1), similarity(loosyPath, f2)))
								.findFirst();
			// I could return the alternatives as well
			return match.<RecoveredPath>map(p -> new HeuristicPath(p)).orElse(new RecoveredPath.MissingPath(loosyPath));
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
		Path p = projectRoot.resolve(folderPath).resolve(filename);
		return Files.exists(p);
	}
	
	
	protected static int similarity(Path loosyPath, Path projectFilePath) {
		int i, len = loosyPath.getNameCount();
		for(i = len - 1; i >= 0; i++) {
			if (projectFilePath.endsWith(loosyPath.subpath(i, len))) {
				return len - i;
			}
		}
		return 0;
	}
	
}
