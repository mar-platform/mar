package mar.neural.search.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.opencsv.CSVReader;

import mar.model2graph.MetaFilter;
import mar.neural.search.core.GraphModel;

public class JsonGeneration {
	
	public static void main(String[] args) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
		LinkedList<String> files = new LinkedList<String>();
		LinkedList<String> domains = new LinkedList<String>();
		LinkedList<LinkedList<String>> tags = new LinkedList<>();
		CSVReader reader = null;

        
        File repo_ecore = new File("/home/antolin/wakame/randomStuff/categories_ecore.csv");
        try {
            reader = new CSVReader(new FileReader(repo_ecore));
            String[] line;
            while ((line = reader.readNext()) != null) {
            	//skip headers
            	if (line[0].equals("id"))
            		continue;
            	//skip no english
            	if (!line[3].equals("english"))
            		continue;
            	//skip unknowns
            	if (line[1].equals("unknown"))
            		continue;
            	
            	files.add(line[0]);
            	domains.add(line[1]);
            	
            	LinkedList<String> ts = new LinkedList<>();
            	ts.add(line[1]);
            	//purpose
            	if (!line[4].equals("unknown"))
            		ts.add(line[4]);
            	//tags column
            	if (!line[2].equals("")) {
            		String[] t =line[2].split("\\|");
            		for (int i = 0; i<t.length;++i) {
            			ts.add(t[i]);
            		}
            	}
            	tags.add(ts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String prefix = "/home/antolin/wakame/mde-datasets/";
        String output_prefix = "/home/antolin/wakame/randomStuff/ModelSetTags/all/";
        
        int i = 0;
        for (String filename : files) {
			File f = new File(prefix+filename);
			
			ResourceSet rs = new ResourceSetImpl();
	    	Resource resource = null;
	    	try {
	    		resource = rs.getResource(URI.createFileURI(f.getAbsolutePath()), true);
	    	} catch (Exception e) { 
	    		e.printStackTrace();
	    		System.out.println(f.getAbsolutePath());
	            continue;
	        }
	    	
	    	MetaFilter mf = MetaFilter.getEcoreFilter();
	    	GraphModel gm = new GraphModel(resource, tags.get(i),mf);
	    	
	    	
	    	
	    	PrintWriter out = null;
			try {
				out = new PrintWriter(output_prefix+f.getName()+Integer.toString(i)+"----"+domains.get(i)+".json");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	i = i + 1;
	    	out.println(gm.generateJson());			
	    	out.close();
	    
	    	
        }
        
	}
}
