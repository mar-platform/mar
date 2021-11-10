package mar.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mar.analysis.smells.Smell;

/**
 * 
 * @author jesus
 *
 */
public class AnalysisMetadataDocument {

	// Smell and the list of model URIs with this smell
	@JsonProperty 
	private Map<String, List<String>> smells = new HashMap<>();	
	@JsonProperty
	private String url;
	@JsonProperty
	private List<String> topics;
	@JsonProperty
	private int numElements = -1;
	@JsonProperty(required = false)
	private String explicitName;
	@JsonProperty(required = false)
	private String description;
	@JsonProperty(required = false)
	private String category;
	
	public String getURL() {
		return url;
	}

	@Nonnull
	public List<String> getTopics() {
		return topics == null ? Collections.emptyList() : topics;
	}
	
	@Nonnull
	public Map<? extends String, List<String>> getSmells() {
		return smells;
	}

	public void addSmell(@Nonnull String smellName, @Nonnegative List<? extends Smell> smells) {
		for (Smell smell : smells) {
			List<String> uris = new ArrayList<>(smell.getSmellyObjectURIs());
			this.smells.put(smellName, uris);
		}
	}
	
	@Nonnull
	public static AnalysisMetadataDocument loadFromJSON(@Nonnull String jsonString) {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		return gson.fromJson(jsonString, AnalysisMetadataDocument.class);
	}

	public void setURL(String url) {
		this.url = url;
	}

	public void setTopics(List<? extends String> topics) {
		this.topics = new ArrayList<>(topics);
	}

	public void setNumElements(int numElements) {
		this.numElements = numElements;
	}

	public void setExplicitName(String explicitName) {
		this.explicitName = explicitName;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@CheckForNull
	public String getDescription() {
		return description;
	}
	
	@CheckForNull
	public String getExplicitName() {
		return explicitName;
	}
	
	public boolean hasNumElements() {
		return numElements >= 0;
	}

	public int getNumElements() {
		return numElements;
	}

	public String toJSON() throws JsonProcessingException {
		ObjectMapper mapperObj = new ObjectMapper();
		return mapperObj.writeValueAsString(this);	
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	@CheckForNull
	public String getCategory() {
		return category;
	}
	
}
