package mar.indexer.common.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represent the data which is needed to configure an index job.
 * 
 * This is done in JSON syntax:
 * 
 * <pre>
 * {
 *   "repo-ecore" : {
 *        type: "ecore",
 *        repo_root : "root-path-to-repo",
 *        file_list : "path-to-file-with-the-file-list",
 *        
 *        graph_length : 3,
 *        graph_filter: EcoreFilter
 *        graph_factory : EcoreFactory
 *        
 *   }
 * }
 * </pre>
 * 
 * @author jesus
 *
 */
public class IndexJobConfigurationData {

	@JsonProperty
	private Map<String, SingleIndexJob> repositories = new HashMap<String, SingleIndexJob>();
	@JsonProperty
	private Map<String, TypeSpecification> types = new HashMap<String, TypeSpecification>();
	
	public static IndexJobConfigurationData fromJSON(String jsonString) {
		GsonBuilder builder = new GsonBuilder();
		// builder.setPrettyPrinting();

		Gson gson = builder.create();
		IndexJobConfigurationData data = gson.fromJson(jsonString, IndexJobConfigurationData.class);

		return data;
	}

	@CheckForNull
	public SingleIndexJob getRepo(@Nonnull String name) {
		return repositories.get(name);
	}

	public Collection<? extends SingleIndexJob> getRepositories() {
		return repositories.values();
	}

	@Nonnull
	public Map<? extends String, Double> getMrankConfiguration(@Nonnull String type) {
		TypeSpecification typeSpec = types.get(type);
		if (typeSpec == null)
			typeSpec = TypeSpecification.DEFAULT_SPEC;
		return typeSpec.getMrank();
	}

	@Nonnull
	public List<SingleIndexJob> getRepositoriesOfType(@Nonnull String type) {
		List<SingleIndexJob> repos = new ArrayList<>();
		for (Entry<String, SingleIndexJob> entry : repositories.entrySet()) {
			if (type.equals(entry.getValue().getType()))
				repos.add(entry.getValue());
		}
		return repos;
	}

	// TODO: Modify the configuration files to have a specific ModelConfiguration and a RepoConfiguration 
	@CheckForNull
	public SingleIndexJob getModelConfigurationByType(String modelType) {
		List<SingleIndexJob> repos = getRepositoriesOfType(modelType);
		if (repos.isEmpty())
			return null;
		return repos.get(0);
	}

}
