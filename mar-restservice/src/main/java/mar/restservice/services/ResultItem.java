package mar.restservice.services;

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
	
	public ResultItem(String name, double score) {
		this.name = name;
		this.score = score;
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
	
//	@JsonGetter("metadata")
//	public String getMetadataAsJSON() {
//		try {
//			return metadata.toJSON();
//		} catch (JsonProcessingException e) {
//			return "null";
//		}
//	}
	
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
	public String getImplicitType() {
		String[] parts = getId().split(":");
		if (parts.length < 3) {
			return null;
		}
		return parts[1];
	}

	public void setMrankScore(double mrankScore) {
		this.mrankScore = mrankScore;
	}
	
}
