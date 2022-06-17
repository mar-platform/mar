package mar.artefacts;

import java.io.File;
import java.nio.file.Path;

import javax.annotation.CheckForNull;

import mar.analysis.megamodel.model.Project;
import mar.artefacts.epsilon.FileSearcher;
import mar.artefacts.graph.RecoveryGraph;

public abstract class ProjectInspector {

	protected final Path repoFolder;
	protected final Path projectSubPath;
	private FileSearcher searcher;

	public ProjectInspector(Path repoFolder, Path projectSubPath) {
		this.repoFolder = repoFolder;
		this.projectSubPath = projectSubPath;
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
	}

	protected Path getProjectFolder() {
		return repoFolder.resolve(projectSubPath);
	}

	protected Path getRepositoryPath(File f) {
		return getRepositoryPath(f.toPath());
	}
	
	protected Path getRepositoryPath(Path p) {
		return repoFolder.relativize(p);
	}

	protected Project getProject() {
		String id = projectSubPath.subpath(0, 2).toString();
		return new Project(id);
	}
	
	protected FileSearcher getFileSearcher() {
		return searcher;
	}
	
	@CheckForNull
	public abstract RecoveryGraph process(File f) throws Exception;

}
