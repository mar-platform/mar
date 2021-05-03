package mar.ml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.LogOutputStream;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import com.google.common.annotations.VisibleForTesting;

import edu.emory.mathcs.backport.java.util.Collections;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.model2graph.IMetaFilter;
import mar.model2text.Utils;
import mar.paths.PathFactory;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import mar.validation.AnalysisMetadataDocument;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "ml-category", mixinStandardHelpOptions = true, description = "Uses an ML model to infer categories")
public class CategoryClassification implements Callable<Integer> {

	@Parameters(index = "0", description = "The configuration file.")
	private File configurationFile;

	@Option(required = true, names = { "-t", "--type" }, description = "The model type: ecore, genmymodel-bpmn, uml")
	private String type;
	
	@Override
	public Integer call() throws Exception {
		IndexJobConfigurationData configuration = CmdOptions.readConfiguration("file:/" + configurationFile.getAbsolutePath());
		// As in IndexJob. We need a configurable way to do this.
		initModels();		
			
		List<SingleIndexJob> repositories = configuration.getRepositoriesOfType(type);
		for (SingleIndexJob job : repositories) {
			File outputAnalysisDB = job.getModelDbFile();
			if (! outputAnalysisDB.exists()) {
				System.out.println("Analysis DB not exists: " + outputAnalysisDB);
				continue;
			}

			PathFactory pf = job.getPathFactory();
			IMetaFilter mf = job.getTextMetaFilter();

			AnalysisDB db = new AnalysisDB(outputAnalysisDB);
			for (Model model : db.getValidModels(rel -> Paths.get(job.getRootFolder(), rel).toString())) {
				try {
					ResourceSet rs = new ResourceSetImpl();
					Resource r = rs.getResource(URI.createFileURI(model.getFile().getAbsolutePath()), true);
									
					String content = Utils.model2document(r, mf, pf);

					AnalysisMetadataDocument document = AnalysisMetadataDocument.loadFromJSON(model.getMetadata());
					
					
					boolean update = false;
					String category = invokeCategory(content);
					if (category != null) {
						System.out.println(category + " : " + model.getFile());
						document.setCategory(category);
						update = true;						
					}	
					
					String tags = invokeTags(content);
					if (tags != null && ! tags.trim().isEmpty()) {
						System.out.println(tags + " : " + model.getFile());
						Set<String> topics = new HashSet<>();
						// Sanitize
						for(String t : document.getTopics()) {
							if (! t.trim().isEmpty())
								topics.add(t);
						}
						Collections.addAll(topics, tags.trim().split(" "));
						if (topics.size() > 0) {
							document.setTopics(new ArrayList<>(topics));
							update = true;			
						}
					}
					
					if (update) {
						db.updateMetadata(model.getId(), document.toJSON());
						db.commit();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			db.close();
		}
		
		return 0;
	}
	

	public static void main(String[] args) {
		int exitCode = new CommandLine(new CategoryClassification()).execute(args);
		System.exit(exitCode);
	}
	
	/* pp */ static void initModels() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("ecore", new EcoreResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("xmi", new XMIResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("uml", new UMLResourceFactoryImpl());
    	EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);	
	}
	
	@VisibleForTesting
	/* pp */ static  String invokeTags(String text) throws ExecuteException, IOException {
		return invokeService(text, "tags");
	}
	
	@VisibleForTesting
	/* pp */ static String invokeCategory(String text) throws ExecuteException, IOException {
		return invokeService(text, "category");
	}
	
	// Docs: http://commons.apache.org/proper/commons-exec/tutorial.html
	private static String invokeService(String text, String service) throws ExecuteException, IOException {
		final String MAR = System.getenv("REPO_MAR");
		if (MAR == null)
			throw new IllegalStateException("REPO_MAR environment variable is not defined");
		
		// To run in background
		/*
		CommandLine cmdLine = new CommandLine("python3");
		cmdLine.addArgument(MAR + "/mar-ml/src/main/resources/infer_category.py");
		cmdLine.addArgument("\"State Machine Transition\"");
		
		DefaultExecutor executor = new DefaultExecutor();
		CollectingLogOutputStream stream = new CollectingLogOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(stream);
		executor.setStreamHandler(psh);
		executor.setExitValue(0);		
		int exitValue = executor.execute(cmdLine);
		
		System.out.println("=> " + stream.getLines());
		*/
		
		HttpResponse<String> result = Unirest.get("http://localhost:5000/" + service)	
				.queryString("text", text)
				.asString();
		if (result.isSuccess()) {
			return result.getBody();
		} else {
			System.out.println(result.getStatus());
			System.out.println(result.getBody());
			return null;
		}
	}
	

	public class CollectingLogOutputStream extends LogOutputStream {
	    private final List<String> lines = new ArrayList<String>();
	    
	    @Override protected void processLine(String line, int level) {
	        lines.add(line);
	    }   
	    public List<String> getLines() {
	        return lines;
	    }
	}
}
