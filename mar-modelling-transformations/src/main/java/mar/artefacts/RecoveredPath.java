package mar.artefacts;

import java.nio.file.Path;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

/**
 * Represents a path in a repository
 * 
 * @author jesus
 */
public class RecoveredPath {

	private Path path;

	/**
	 * @param path The path (relative to the root) of the artefact
	 */
	public RecoveredPath(Path path) {
		Preconditions.checkArgument(! path.isAbsolute());
		this.path = path;
	}
	
	public Path getPath() {
		return path;
	}
	
	@Nonnull
	public Path getCompletePath(Path repoFolder) {
		return repoFolder.resolve(getPath());
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecoveredPath other = (RecoveredPath) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
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
