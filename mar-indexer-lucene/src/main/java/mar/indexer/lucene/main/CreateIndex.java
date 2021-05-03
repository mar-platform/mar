package mar.indexer.lucene.main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class CreateIndex {

	public static void main(String[] args) throws IOException, InvalidJobSpecification, SQLException {
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());

		//EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
		
		if (args.length != 3) {
	        System.err.println("Usage: LuceneIndexer configuration-string repo-name inverted-index-path");
			System.err.println("  - inverted index path is where Lucene will store the index");
			return;
		}
		
	    String configuration = args[0];
	    String repoName = args[1];
		String pathIndex = args[2];
		
		initModels();
		
	    IndexJobConfigurationData data = CmdOptions.readConfiguration(configuration);	    
	    SingleIndexJob repoConf = data.getRepo(repoName);		
		String rootFolder = repoConf.getRootFolder();
		
		Indexer indexer = new Indexer(pathIndex);
				
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
				String modelType  = repoName;
				PathFactory pf = repoConf.getPathFactory();
				IMetaFilter mf = repoConf.getTextMetaFilter();
				indexer.indexModel(id, modelType, resource, mf, pf);
				resource.unload();
			} catch (Exception e) {
				e.printStackTrace();
			}    		    				
		}
	    
        indexer.close();
	}

	/* pp */ static void initModels() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("ecore", new EcoreResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("xmi", new XMIResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("uml", new UMLResourceFactoryImpl());
    	EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);	
	}

}
