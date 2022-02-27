package mar.artefacts;

import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * Represents a path in a repository
 * 
 * @author jesus
 */
public class RecoveredPath {

	private Path path;

	/**
	 * 
	 * @param root Refers to the path segment from the repository folder up to a folder of interest (e.g., a project path or where a build file is located) 
	 * @param path The path (relative to the root) of the artefact
	 * @param loosyFilePath Whether this file is known to exists or just recovered heuristically
	 */
	public RecoveredPath(Path path) {
		this.path = path;
	}
	
	public Path getPath() {
		return path;
	}
	
	@Nonnull
	public Path getCompletePath(Path repoFolder) {
		return repoFolder.resolve(getPath());
	}
	
	
	public static class Ant extends RecoveredPath {
		private boolean loosyFilePath;

		public Ant(Path path, boolean loosyFilePath) {
			super(path);
			this.loosyFilePath = loosyFilePath;
		}
		
		public boolean isLoosyFilePath() {
			return loosyFilePath;
		}
	}
	
	public static class HeuristicPath extends RecoveredPath {

		public HeuristicPath(Path path) {
			super(path);
		}
		
	}
	
	public static class MissingPath extends RecoveredPath {

		public MissingPath(Path path) {
			super(path);
		}
		
	}
}
