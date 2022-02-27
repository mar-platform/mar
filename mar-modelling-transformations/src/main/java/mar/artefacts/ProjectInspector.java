package mar.artefacts;

import java.io.File;
import java.nio.file.Path;

import mar.artefacts.graph.RecoveryGraph;

public abstract class ProjectInspector {

	protected final Path repoFolder;
	protected final Path projectPath;

	public ProjectInspector(Path repoFolder, Path projectPath) {
		this.repoFolder = repoFolder;
		this.projectPath = projectPath;
	}

	public abstract RecoveryGraph process(File f) throws Exception;

}
