package mar.artefacts;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Artefact.ArtefactStatus;

/**
 * Represents a path in a repository
 * 
 * @author jesus
 */
public abstract class RecoveredPath {

	private Path path;
	// This means that we can't guarantee that the path is within the repository root (e.g., can be an absolute path)
	private boolean unchecked;

	/**
	 * @param path The path (relative to the root) of the artefact
	 */
	public RecoveredPath(Path path) {
		this(path, false);
	}
	
	public RecoveredPath(Path path, boolean unchecked) {
		this.unchecked = unchecked;
		this.path = path;
		if (this.unchecked == false)
			Preconditions.checkArgument(! path.isAbsolute());
	}

	
	public abstract Artefact.ArtefactStatus toPathStatus();
	
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

	public static class ExistingPath extends RecoveredPath {

		private ExistingPath(Path path) {
			super(path);
		}

		@Override
		public ArtefactStatus toPathStatus() {
			return ArtefactStatus.EXISTS;
		}
		
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
		
		@Override
		public Artefact.ArtefactStatus toPathStatus() {
			return Artefact.ArtefactStatus.HEURISTIC;
		}
	}

	/**
	 * A path that was found but we are not sure if this is the real file.
	 * 
	 * @author jesus
	 */
	public static class HeuristicPath extends RecoveredPath {

		public HeuristicPath(Path path) {
			super(path);
		}
		
		@Override
		public Artefact.ArtefactStatus toPathStatus() {
			return Artefact.ArtefactStatus.HEURISTIC;
		}
	}
	
	/**
	 * A path that was expected but could not be found.
	 * 
	 * @author jesus
	 */
	public static class MissingPath extends RecoveredPath {

		public MissingPath(Path path) {
			super(path, true);
		}		
		
		@Override
		public Artefact.ArtefactStatus toPathStatus() {
			return Artefact.ArtefactStatus.MISSING;
		}
	}
	
	/**
	 * A path that doesn't actually exist but could be generated.
	 * For instance, an .emf file generates an .ecore.
	 *  
	 * @author jesus
	 */
	public static class GeneratedPath extends RecoveredPath {

		public GeneratedPath(Path path) {
			super(path);
		}		
		
		@Override
		public Artefact.ArtefactStatus toPathStatus() {
			return Artefact.ArtefactStatus.GENERATED;
		}
	}
	
	public static class UnexpectedPath extends RecoveredPath {

		public UnexpectedPath(Path path) {
			super(path);
		}
		
		@Override
		public Artefact.ArtefactStatus toPathStatus() {
			return Artefact.ArtefactStatus.ERROR;
		}
	}
	
	public static RecoveredPath newExistingPath(Path relative, Path repoFolder) {
		Path r = repoFolder.resolve(relative);
		if (! Files.exists(r)) {
			throw new IllegalStateException("Non-existing path: " + r);
		}
		return new ExistingPath(relative);
	}
		
}
