package mar.validation;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.ingestion.CrawlerDB;
import mar.validation.ISingleFileAnalyser.Remote;
import mar.validation.ResourceAnalyser.Factory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * An application to validate crawled models according to some validator(s) and
 * output the information that can be used by the indexers.
 * 
 * @author jesus
 */
@Command(name = "analyser", mixinStandardHelpOptions = true, description = "Analyses files according to a given validator")
public class AnalyserMain implements Callable<Integer> {

	@Parameters(index = "0", description = "The configuration file.")
	private File configurationFile;

	@Option(required = true, names = { "-t", "--type" }, description = "The model type: ecore, bpmn2, uml")
	private String type;

	@Option(required = false, names = { "-r",
			"--repository" }, description = "A specific repository in the configuration file")
	private String repository = null;

	@Option(required = false, names = { "-mode" }, description = "Can be: plain, remote, resilient")
	private String mode = "remote";

	@Option(required = false, names = { "-parallel" }, description = "Use parallel mode")
	private Integer parallel;

	@Override
	public Integer call() throws Exception {
		IndexJobConfigurationData configuration = CmdOptions
				.readConfiguration("file:/" + configurationFile.getAbsolutePath());

		Factory factory = AnalyserRegistry.INSTANCE.getFactory(type);
		if (factory == null) {
			showAvailableAnalysers();
			return -1;
		}
		
		factory.configureEnvironment();

		List<SingleIndexJob> repositories;
		if (repository != null) {
			SingleIndexJob repo = configuration.getRepo(repository);
			repositories = Collections.singletonList(repo);
		} else {
			repositories = configuration.getRepositoriesOfType(type);
		}
		
		for(SingleIndexJob repo : repositories) {					
			File outputAnalysisDB = repo.getModelDbFile();
			// Try to create the output folder
			outputAnalysisDB.getParentFile().mkdirs();
			
			CrawlerDB crawler = new CrawlerDB(type, repo.getOrigin(), repo.getRootFolder(), repo.getCrawlerDbFile());
	
			if (mode.equals("plain")) {
				ISingleFileAnalyser singleAnalyser = factory.newAnalyser();
				try (ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, new IFileProvider.DBFileProvider(crawler), outputAnalysisDB)) {					
					analyser.check();
				}
			} else if (mode.equals("resilient")) {				
				ISingleFileAnalyser singleAnalyser = factory.newResilientAnalyser();
				try (ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, new IFileProvider.DBFileProvider(crawler), outputAnalysisDB)) {					
					if (parallel != null && parallel > 1)
						analyser.withParallelThreads(parallel);
					analyser.check();
				}
			} else if (mode.equals("remote")) {
				try (ISingleFileAnalyser.Remote singleAnalyser = (Remote) factory.newRemoteAnalyser()) {
					try (ResourceAnalyser analyser = new ResourceAnalyser(singleAnalyser, new IFileProvider.DBFileProvider(crawler), outputAnalysisDB)) {
						if (parallel != null && parallel > 1)
							analyser.withParallelThreads(parallel);
						analyser.check();
					}
				}
			} else {
				System.out.println("Invalid mode " + mode + ". " + "Available are: plain, remote, resilient.");
				return -1;
			}
		}

		return 0;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new AnalyserMain()).execute(args);
		System.exit(exitCode);
	}

	private static void showAvailableAnalysers() {
		System.out.println("Available analysers: ");
		AnalyserRegistry.INSTANCE.forEach((str, factory_) -> {
			System.out.println("  " + str);
		});
	}

}
