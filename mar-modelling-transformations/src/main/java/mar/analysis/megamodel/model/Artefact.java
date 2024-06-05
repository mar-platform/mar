package mar.analysis.megamodel.model;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

public class Artefact {

	public static final String MISSING_STATUS = "missing";
	
	@JsonProperty
	private final String id;
	@JsonProperty
	private final String type;
	@JsonProperty
	private final String name;
	@JsonProperty
	private final String category;
	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
	@JsonIdentityReference(alwaysAsId=true)
	private final Project project;
	@JsonProperty
	private String fileStatus;

	public Artefact(@Nonnull Project project, @Nonnull String id, @Nonnull String type, @Nonnull String category, @Nonnull String name, @Nonnull String status) {
		this.project = project;
		this.id = id;
		this.type = type;
		this.category = category;
		this.name = name;
		this.fileStatus = status;
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

	public String getCategory() {
		return category;
	}
	
	public Project getProject() {
		return project;
	}

	public String getFileStatus() {
		return fileStatus;
	}
}
