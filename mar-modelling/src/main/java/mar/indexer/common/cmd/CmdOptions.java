package mar.indexer.common.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.SingleIndexJob;

public class CmdOptions {

	public static IndexJobConfigurationData readConfiguration(File configurationFile) throws FileNotFoundException, IOException {
		return readConfiguration("file:/" + configurationFile.getAbsolutePath());
	}

	public static IndexJobConfigurationData readConfiguration(@Nonnull String configuration)
			throws IOException, FileNotFoundException {
		if (configuration.startsWith("file:/")) {
			String fname = configuration.replaceFirst("file:/", "");
			configuration = IOUtils.toString(new FileInputStream(fname), Charset.defaultCharset());
		}

		IndexJobConfigurationData data = IndexJobConfigurationData.fromJSON(configuration);
		if (data == null) {
			System.err.println("Configuration " + configuration + " not found.");
			System.exit(1);
		}
		return data;
	}

	@Nonnull
	public static SingleIndexJob readConfiguration(String configuration, String repoName) throws FileNotFoundException, IOException, InvalidJobSpecification {
		IndexJobConfigurationData data = readConfiguration(configuration);
		SingleIndexJob repo = data.getRepo(repoName);
		if (repo == null) {
			throw new InvalidJobSpecification("No repo " + repoName);
		}
		return repo;
	}

}
