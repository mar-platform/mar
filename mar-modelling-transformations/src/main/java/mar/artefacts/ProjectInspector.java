package mar.artefacts;

import java.io.File;
import java.nio.file.Path;

import javax.annotation.CheckForNull;

import mar.analysis.megamodel.model.Project;
import mar.artefacts.graph.RecoveryGraph;

public abstract class ProjectInspector {

	protected final Path repoFolder;
	protected final Path projectSubPath;

	public ProjectInspector(Path repoFolder, Path projectSubPath) {
		this.repoFolder = repoFolder;
		this.projectSubPath = projectSubPath;
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
	
	
	
	@CheckForNull
	public abstract RecoveryGraph process(File f) throws Exception;

}
