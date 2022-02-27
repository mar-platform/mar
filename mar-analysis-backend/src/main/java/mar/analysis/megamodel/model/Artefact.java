package mar.analysis.megamodel.model;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Artefact {

	@JsonProperty
	private final String id;
	@JsonProperty
	private final String type;
	@JsonProperty
	private final String name;

	public Artefact(@Nonnull String id, @Nonnull String type, @Nonnull String name) {
		this.id = id;
		this.type = type;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
}