package mar.indexer.lucene.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import com.opencsv.CSVReader;

import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.ModelLoader;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.indexer.lucene.core.Indexer;
import mar.model2graph.IMetaFilter;
import mar.paths.PathFactory;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "create-index", mixinStandardHelpOptions = true, description = "Index models with Lucene")
public class CreateIndex implements Callable<Integer> {

	@Parameters(index = "0", description = "The configuration file.")
	private File configurationFile;

	@Parameters(index = "1", description = "The path to the Lucene index")
	private File pathIndex;
	
	@Option(required = false, names = { "-t", "--type" }, description = "The model type: ecore, bpmn2, uml")
	private String type;

	@Option(required = false, names = { "-r",
			"--repository" }, description = "A specific repository in the configuration file")
	private String repository = null;

	@Option(required = false, names = { "-all" }, description = "Index all repositories of the configuration file")
	private boolean all = false;
	
	@Override
	public Integer call() throws Exception {		
		initModels();
		
		if (type == null && repository == null && !all) {
			System.out.println("Invalid arguments. Expecting -type or -repository or -all");
			return -1;
		}

	    IndexJobConfigurationData data = CmdOptions.readConfiguration(configurationFile);	    

		List<SingleIndexJob> jobs = new ArrayList<SingleIndexJob>();
		if (type != null) {
			jobs.addAll(data.getRepositoriesOfType(type));
		} else if (repository != null) {
			SingleIndexJob repo = data.getRepo(repository);
			if (repo == null) {
				System.out.println("No repository " + repository);
				return -1;
			}
			jobs.add(repo);
		} else if (all) {
			jobs.addAll(data.getRepositories());
		}
		
		for (SingleIndexJob repoConf : jobs) {
			indexRepo(repoConf);			
		}
		
        return 0;
	}

	private void indexRepo(SingleIndexJob repoConf)
			throws IOException, InvalidJobSpecification, FileNotFoundException, SQLException {
		String rootFolder = repoConf.getRootFolder();
		Indexer indexer = new Indexer(pathIndex.getAbsolutePath());				
		ModelLoader loader = repoConf.getModelLoader();   
		
		List<File> files = new ArrayList<>();
		Map<File, String> ids = new HashMap<File, String>();
	    if (repoConf.usesFileList()) {	    	
			File repo_ecore = new File(repoConf.getFileList());
			try(CSVReader reader = new CSVReader(new FileReader(repo_ecore))) {
		        String[] line;
		        while ((line = reader.readNext()) != null) {
			    	String fileName = line[0];
			    	String fullPath = rootFolder + File.separator + fileName;	        	
		        	File f = new File(fullPath);
			    	files.add(f);
		        	ids.put(f, fileName);
		        }
			}
	    } else {	    
	    	try(AnalysisDB analysisDB = new AnalysisDB(repoConf.getModelDbFile())) {
	    		List<Model> models = analysisDB.getValidModels(r -> rootFolder + File.separator + r);
	    		for (Model model : models) {
	    			String id = model.getId();
	    			File f = model.getFile();
	    			if (f.isFile()) {
	    				files.add(f);
	    				ids.put(f, id);
	    			}   			
	    		}
	    	}
	    }

	    for (File f : files) {
			System.out.println("Indexing... " + f.getAbsolutePath());
			
			Resource resource;
			try {
				String id = ids.get(f);
				resource = loader.load(f);
				PathFactory pf = repoConf.getPathFactory();
				IMetaFilter mf = repoConf.getTextMetaFilter();
				indexer.indexModel(id, repoConf.getType(), resource, mf, pf);
				resource.unload();
			} catch (Exception e) {
				e.printStackTrace();
			}    		    				
		}
	    
        indexer.close();
	}
	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new CreateIndex()).execute(args);
		System.exit(exitCode);
	}
	

	/* pp */ static void initModels() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("ecore", new EcoreResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("xmi", new XMIResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("uml", new UMLResourceFactoryImpl());
    	EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);	
	}

}
