package mar.restservice.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.MarChatBotConfiguration;
import mar.MarConfiguration;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.ModelLoader;
import mar.indexer.lucene.core.Searcher;
import mar.models.bpmn.BPMNLoader;
import mar.restservice.emfatic.EmfaticReader;
import mar.restservice.services.SearchOptions.ModelType;
import mar.restservice.services.SearchOptions.SyntaxType;
import spark.Response;

public abstract class AbstractAPI {

    private IConfigurationProvider configuration;
	private Searcher textSearcher;
	
	public AbstractAPI(IConfigurationProvider configuration) {
    	this.configuration = configuration;
	}

	@Nonnull
	protected IndexJobConfigurationData getIndexConfiguration() {
		return configuration.getIndexJobConfiguration();
	}	
	
	@NonNull
	protected Searcher getTextSearcher() {
		if (textSearcher == null)
			textSearcher = configuration.newSearcher();
		return textSearcher;
	}
	
	protected String getModelFile(String id) {
		return this.configuration.getModelFile(id);
	}
	
	public IndexJobConfigurationData getConfiguration() {
		return this.configuration.getIndexJobConfiguration();
	}
	
	// This is an utility method
    protected Map<String, Double> searchAndScore(@Nonnull String model, @Nonnull ModelType modelType, @Nonnull SyntaxType syntax) throws IOException {
		Resource r;
		switch (syntax) {
		case xmi:
			r = loadXMI(model, modelType);
			break;
		case emfatic:
			EmfaticReader reader = new EmfaticReader();
			r = reader.read(model);
			break;
		default:
			throw new IllegalStateException();
		}
		
		try {
			Map<String, Double> scores = getConfiguration(modelType).getScorer().score(r);
			return scores;
		} finally {
			// This seems to fail with NPE. 
			// TODO: Observe isfthere are memory leaks because of this. Maybe each loader needs to be in charge of unloading its models 
			// r.unload();
		}
    }

    @Nonnull
	protected Resource loadXMI(@Nonnull File file, @Nonnull ModelType modelType) throws IOException {
    	// TODO: How to detect the charset?? Possibly do the loadXMI with the file instead of the contents
    	String str = IOUtils.toString(new FileInputStream(file), "UTF-8");
    	return loadXMI(str, modelType);
    }
    
    @Nonnull
	protected Resource loadXMI(@Nonnull String model, @Nonnull ModelType modelType) throws IOException {
		Resource r;
		switch (modelType) {
		case uml:
			UMLResourceFactoryImpl factory = new UMLResourceFactoryImpl();
			Resource resource = factory.createResource(URI.createFileURI("ex.uml"));
			try {
				resource.load(new ByteArrayInputStream(model.getBytes()), null);
			}catch (Exception e) {
				e.printStackTrace();
				return null;
		    }
			r = resource;
			break;
		case bpmn2:
			BPMNLoader loader = new BPMNLoader();
			r = loader.load(model);
			break;
		case ecore:
			XMIResourceImpl impl = new XMIResourceImpl();
			impl.load(new ByteArrayInputStream(model.getBytes()), null);
			r = impl;
			break;
		case pnml:
			r = ModelLoader.PNML.load(new ByteArrayInputStream(model.getBytes()));
			break;
		case sculptor:
			r = ModelLoader.SCULPTOR.load(new ByteArrayInputStream(model.getBytes()));
			break;
		case archimate:
			r = ModelLoader.ARCHIMATE.load(new ByteArrayInputStream(model.getBytes()));
			break;
		case rds:
			r = ModelLoader.RDS.load(new ByteArrayInputStream(model.getBytes()));
			break;
		case simulink:
			r = ModelLoader.SIMULINK.load(new ByteArrayInputStream(model.getBytes()));
			break;
		case xtext:
			r = ModelLoader.XTEXT.load(new ByteArrayInputStream(model.getBytes()));
			break;			
		default:
			throw new IllegalStateException();
		}
		return r;
	}
       
	protected MarConfiguration getConfiguration(ModelType modelType) {
		String t = modelType.name();
		return configuration.getConfiguration(t);
	}
	
	protected MarChatBotConfiguration getChatBotConfiguration(String modelType) {
		return configuration.getChatBotConfiguration(modelType);
	}

	protected static <K, V extends Comparable<? super V>> Map<K, V> firtsElements(Map<K, V> map, int n) {
	        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
	        //list.sort(Entry.comparingByValue());
	        Collections.sort(list, Collections.reverseOrder(Entry.comparingByValue()));
	        list = list.stream().limit(n).collect(Collectors.toList());
	        Map<K, V> result = new LinkedHashMap<>();
	        for (Entry<K, V> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }

	        return result;
	 }
	
	protected static Map<String,List<Double>> firtsElementsPartitioned(Map<String,List<Double>> map, int n){
		Map<String, Double> auxMap = new HashMap<String, Double>();
		for (Entry<String,List<Double>> e : map.entrySet()) {
			double sum = e.getValue().stream().mapToDouble(Double::doubleValue).sum();
			auxMap.put(e.getKey(), sum);
		}
		auxMap = firtsElements(auxMap, n);
		
		Map<String,List<Double>> result = new HashMap<String, List<Double>>();
		for (Entry<String,Double> e : auxMap.entrySet()) {
			result.put(e.getKey(), map.get(e.getKey()));
		}
		return result;
	}

	protected Object toJson(Response res, Object value) throws JsonProcessingException {
		ObjectMapper mapperObj = new ObjectMapper();
		String jsonResp = mapperObj.writeValueAsString(value);	
		res.type("text/json");	    
	    return jsonResp;
	}   
}
