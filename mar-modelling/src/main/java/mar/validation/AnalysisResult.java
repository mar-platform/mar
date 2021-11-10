package mar.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import mar.validation.AnalysisDB.Status;

/**
 * Represents the results of the analysis of a model. The main result of an analysis
 * is its validation result, represented with a {@link Status}. 
 * A VALID model may also contain additional information gathered
 * during the analysis:
 * <ul>
 *   <li>stats. Statistics about the model in terms of number of elements, types of elements</li>
 *   <li>metadata. Pairs of (key, values) to attach generic information</li>
 *   <li>jsonMetadata. A JSON document</li>
 * </ul>
 * 
 * This information will be stored in a database, so "stats" and "metadata" can be easily
 * inserted and queried. On the contrary, "jsonMetadata" contains a JSON document with 
 * the previous information plus other information. This document is intended to be attached
 * to the model.
 * 
 * @author jesus
 *
 */
public class AnalysisResult {

	@JsonProperty
	private final String modelId;
	private final Status status;
	@JsonProperty
	private Map<String, Integer> stats;
	private Map<String, List<String>> metadata;
	@JsonProperty
	private String jsonMetadata;

	public AnalysisResult(@Nonnull String modelId, @Nonnull Status status) {
		this.modelId = modelId;
		this.status = status;
	}
	
	@Nonnull
	public AnalysisResult withStats(String statsId, int value) {
		if (stats == null)
			stats = new HashMap<String, Integer>();
		stats.put(statsId, value);
		return this;
	}
	
	@Nonnull
	public AnalysisResult withStats(@CheckForNull Map<String, Integer> value) {
		if (value == null)
			return this;
		
		if (stats == null)
			stats = new HashMap<String, Integer>();
		stats.putAll(value);
		return this;
	}
	
	@Nonnull
	public AnalysisResult withMetadata(@CheckForNull Map<String, List<String>> value) {
		if (value == null)
			return this;

		if (metadata == null)
			metadata = new HashMap<String, List<String>>();
		metadata.putAll(value);
		return this;
	}
	
	@Nonnull
	public AnalysisResult withMetadataJSON(@CheckForNull AnalysisMetadataDocument document) throws IOException {
		if (document == null)
			return this;

		this.jsonMetadata = document.toJSON();
		return this;
	}	
	
	public AnalysisResult withMetadataJSON(String jsonMetadata) {
		this.jsonMetadata = jsonMetadata;
		return this;
	}
	
	@CheckForNull
	public Map<String, Integer> getStats() {
		return stats;
	}

	@CheckForNull
	public Map<String, List<String>> getMetadata() {
		return metadata;
	}

	@Nonnull
	public String getModelId() {
		return modelId;
	}
	
	@Nonnull
	public Status getStatus() {
		return status;
	}

	@CheckForNull
	public String getJsonMetadata() {
		return jsonMetadata;
	}

}