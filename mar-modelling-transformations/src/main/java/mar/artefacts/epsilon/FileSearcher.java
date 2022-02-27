package mar.artefacts.epsilon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.artefacts.RecoveredPath;
import mar.artefacts.RecoveredPath.HeuristicPath;

public class FileSearcher {

	private final Path root;

	public FileSearcher(@Nonnull Path root) {
		this.root = root;
	}
	
	@CheckForNull
	public RecoveredPath findFile(Path loosyPath) {
		Path filename = loosyPath.getName(loosyPath.getNameCount() - 1);
		try {
			Optional<Path> match = Files.walk(root)
								.filter(f -> ! f.startsWith("."))
								.filter(f -> f.endsWith(filename))
								.map(p -> root.relativize(p))
								.sorted((f1, f2) -> -1 * Integer.compare(similarity(loosyPath, f1), similarity(loosyPath, f2)))
								.findFirst();
			// I could return the alternatives as well
			return match.<RecoveredPath>map(p -> new HeuristicPath(p)).orElse(new RecoveredPath.MissingPath(loosyPath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	private int similarity(Path loosyPath, Path projectFilePath) {
		int i, len = loosyPath.getNameCount();
		for(i = len - 1; i >= 0; i++) {
			if (projectFilePath.endsWith(loosyPath.subpath(i, len))) {
				return len - i;
			}
		}
		return 0;
	}

	private boolean matches(Path f, String loosyPath) {
		return f.toString().contains(loosyPath);
	}
	
}
