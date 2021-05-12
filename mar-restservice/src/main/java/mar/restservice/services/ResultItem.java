package mar.restservice.services;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import mar.restservice.model.IModelResult;
import mar.validation.AnalysisMetadataDocument;

public class ResultItem implements IModelResult {
	private String name;
	private double score;
	private double mrankScore;
	
	@JsonProperty("metadata")
	private AnalysisMetadataDocument metadata;
	private String explicitType;

	public ResultItem(@Nonnull String name, double score) {
		this.name = name;
		this.score = score;
	}

	public ResultItem(@Nonnull String name, double score, @Nonnull String type) {
		this(name, score);
		this.explicitType = type;
	}

	@JsonGetter("id")
	@Override
	public String getId() {
		return name;
	}
	
	@JsonGetter("name")
	@Override
	public String getName() {
		String[] parts = name.split("/");
		return parts[parts.length - 1];
	}

	@JsonGetter("score")
	public double getScore() {
		return score;
	}
	
	@JsonGetter("mrankScore")
	public double getMrankScore() {
		return mrankScore;
	}
	
	@JsonGetter("description")
	public String getDescription() {
		return "No description available";
	}
	
	@JsonGetter("url")
	@Override
	public String getURL() {
		return metadata == null ? this.name : this.metadata.getURL();
	}
	
	@CheckForNull
	@JsonGetter("modelType")
	public String getModelType() {
		return explicitType == null ? getImplicitType() : explicitType;
	}
	
	public String getURLHumanName() { 
		return getURL();
	}

	@Override
	public void setMetadata(AnalysisMetadataDocument metadata) {
		this.metadata = metadata;
	}
	
	public AnalysisMetadataDocument getMetadata() {
		return metadata;
	}

	/**
	 * The implicit type is encoded in the id, in the form of: 
	 *    origin:type:id
	 * as in:
	 *    github:ecore:/repo/jesus/model.xmi
	 * @return
	 */
	@CheckForNull
	public String getImplicitType() {
		String[] parts = getId().split(":");
		if (parts.length < 3) {
			return null;
		}
		return parts[1];
	}

	@CheckForNull
	@JsonGetter("origin")
	public String getOrigin() {
		String[] parts = getId().split(":");
		if (parts.length < 3) {
			return null;
		}
		return parts[0];
	}

	
	public void setMrankScore(double mrankScore) {
		this.mrankScore = mrankScore;
	}
	
}
