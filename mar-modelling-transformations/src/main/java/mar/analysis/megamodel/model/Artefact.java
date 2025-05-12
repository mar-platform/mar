package mar.analysis.megamodel.model;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

public class Artefact {
	
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
	private ArtefactStatus fileStatus;

	public Artefact(@Nonnull Project project, @Nonnull String id, @Nonnull String type, @Nonnull String category, @Nonnull String name, @Nonnull ArtefactStatus status) {
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

	public ArtefactStatus getFileStatus() {
		return fileStatus;
	}
	
	public static enum ArtefactStatus {
		EXISTS,
		/** A file is expected but can't be found */
		MISSING,
		/** A meta-model (typically) is referenced by URI and we can't find it in the project */
		UNRESOLVED,
		GENERATED,
		HEURISTIC,
		BUILTIN,
		ERROR /* no-path */, 
	}
}
