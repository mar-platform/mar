package mar.common.test;

import java.io.File;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import mar.indexer.common.configuration.EnvironmentVariables;
import mar.indexer.common.configuration.EnvironmentVariables.MAR;

public class TestUtils {

	@Nonnull
	public static File getExternalResource(String category, String path) {
		String mar = EnvironmentVariables.getVariable(MAR.REPO_MAR);
		File file = Paths.get(mar, "external-resources", "data", category, path).toFile();
		if (! file.exists())
			throw new IllegalStateException("File " + file.getAbsolutePath() + " doesn't exist");
		return file;
	}

	@Nonnull
	public static File getOutputFile(@Nonnull String path) {
		return new File("/tmp/" + path);
	}
	
}
