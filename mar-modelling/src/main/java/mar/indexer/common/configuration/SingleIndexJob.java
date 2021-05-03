package mar.indexer.common.configuration;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;

import mar.model2graph.AbstractPathComputation;
import mar.model2graph.IMetaFilter;
import mar.model2graph.MetaFilter;
import mar.model2graph.MetaFilterNames;
import mar.model2graph.Model2GraphAllpaths;
import mar.paths.PathFactory;
import mar.paths.PathFactory.DefaultPathFactory;

public class SingleIndexJob implements Serializable {

	private static final long serialVersionUID = -1233908699965256231L;

	@JsonProperty 
	private String type;

	@JsonProperty
	private String origin;

	@JsonProperty 
	private String repo_root;
	
	// Either file_list of model_db
	@CheckForNull
	@JsonProperty
	private String file_list;

	@JsonProperty
	private String crawler_db;

	@CheckForNull
	@JsonProperty
	private String model_db;

	@JsonProperty
	private String model_loader;

	@JsonProperty
	private int graph_length;
	@JsonProperty
	private String graph_filter;
	@JsonProperty
	private String graph_factory;
	
	public String getRootFolder() {
		return replaceEnv(repo_root, System.getenv());
	}

	public boolean usesFileList() {
		return file_list != null;
	}
	
	public String getFileList() {
		return replaceEnv(file_list, System.getenv());
	}
	
	public String getModelDb() {
		return replaceEnv(model_db, System.getenv());
	}

	@Nonnull
	public String getCrawlerDb() {
		return replaceEnv(crawler_db, System.getenv());
	}

	@Nonnull
	public File getCrawlerDbFile() {
		return new File(getCrawlerDb());
	}

	@Nonnull
	public File getModelDbFile() {
		return new File(getModelDb());
	}

	public boolean hasModelDb() {
		return model_db != null;
	}

	@Nonnull
	public String getType() {
		return type;
	}
	
	@Nonnull
	public String getOrigin() {
		return origin;
	}
	
	public int getGraphLength() {
		return graph_length;
	}

	public String getGraphFilter() {
		return graph_filter;
	}

	public String getGraphFactory() {
		return graph_factory;
	}
	
	@Nonnull
	public ModelLoader getModelLoader() throws InvalidJobSpecification {
		if (model_loader == null)
			return ModelLoader.DEFAULT;
		ModelLoader m = ModelLoader.valueOf(model_loader);
		if (m == null)
			throw new InvalidJobSpecification("Unknown model loader: " + model_loader);
		return m;
	}
	
	/**
	 * Given a path, it replaces variables in the form $(VAR) with environment variables. 
	 */
	@VisibleForTesting
	/* pp */ String replaceEnv(@Nonnull String path, @Nonnull Map<String, String> env) {
		Pattern pattern = Pattern.compile("\\$\\((.+)\\)");
		Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
        	String var = matcher.group(1);
        	String sub = env.get(var);
        	if (sub == null)
        		throw new RuntimeException("No variable: " + var);
        	path = matcher.replaceFirst(sub);
        	matcher.reset(path);
        }
		return path;
	}

	@Nonnull
	public AbstractPathComputation toPathComputation() throws InvalidJobSpecification {		
		PathFactory factory = getPathFactory();
		
		Function<Integer, Integer> nodeSizeToMaxLength = (v) -> graph_length;
		IMetaFilter metaFilter = MetaFilter.getNoFilter();
		if ("EcoreFilter".equalsIgnoreCase(graph_filter)) {
			metaFilter = MetaFilter.getEcoreFilterNames();
			// This might need to be configured as well externally
			nodeSizeToMaxLength = (v) -> v >= 2000 ? Math.min(graph_length, 3) : graph_length;
		} else if ("BPMNFilter".equalsIgnoreCase(graph_filter)) {
			metaFilter = MetaFilterNames.getBpmnMetaFilter();					
		} else if ("UMLFilter".equalsIgnoreCase(graph_filter)) {
			metaFilter = MetaFilter.getUMLEasyFilter();
		}
		
    	AbstractPathComputation pathComputation = new Model2GraphAllpaths(graph_length, nodeSizeToMaxLength)
    			.withPathFactory(factory);
		pathComputation.withFilter(metaFilter);
		
		return pathComputation;
	}

	@Nonnull
	public PathFactory getPathFactory() throws InvalidJobSpecification {
		String graphFactoryOpt = getGraphFactory();
		PathFactory factory;
		if (graphFactoryOpt == null) {
			factory = DefaultPathFactory.INSTANCE;
		} else	if ("EcoreFactory".equalsIgnoreCase(getGraphFactory())) {
			factory = new PathFactory.EcoreTokenizer();
		} else if ("BPMNFactory".equalsIgnoreCase(getGraphFactory())) {
			// Use the default
			factory = DefaultPathFactory.INSTANCE;
		} else {
			throw new InvalidJobSpecification("Invalid path factory: " + getGraphFactory());
		}
		return factory;
	}

	/**
	 * TODO: This is a hack, we should specify this properly with a property in the JSON 
	 */
	public IMetaFilter getTextMetaFilter() {
		IMetaFilter metaFilter = MetaFilter.getNoFilter();
		if ("EcoreFilter".equalsIgnoreCase(graph_filter)) {
			metaFilter = MetaFilter.getEcoreFilterNames();
		} else if ("BPMNFilter".equalsIgnoreCase(graph_filter)) {
			metaFilter = MetaFilterNames.getBpmnMetaFilter();					
		} else if ("UMLFilter".equalsIgnoreCase(graph_filter)) {
			metaFilter = MetaFilter.getUMLEasyFilter();
		}
		return metaFilter;
	}

}