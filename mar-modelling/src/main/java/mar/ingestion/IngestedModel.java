package mar.ingestion;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import mar.validation.IFileInfo;

public class IngestedModel implements IFileInfo, IngestedMetadata {
	
	private String id;
	private String url;
	private int sizeBytes = -1;
	private int stars;
	private int forks;
	private List<String> topics = new ArrayList<>();
	
	private File relativeFile;
	private File fullPath;
	private String description;
	private String explicitName;

	public IngestedModel(@Nonnull String id, @Nonnull File relativeFile, File fullPath, @Nonnull String url) {
		this.id = id;
		this.relativeFile = relativeFile;
		this.fullPath = fullPath;
		this.url = url;
	}
	
	@Override
	public String getURL() {
		return url;
	}
	
	@Override
	public String getModelId() {
		return id;
	}
	
	@Override
	public File getRelativeFile() {
		return relativeFile;
	}
	
	@Override
	public File getFullFile() {
		return fullPath;
	}

	@Override
	public int getSizeBytes() {
		return sizeBytes;
	}
	
	public int getStars() {
		return stars;
	}
	
	public int getForks() {
		return forks;
	}
	
	@Override
	public List<? extends String> getTopics() {
		return topics;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String getExplicitName() {
		return explicitName;
	}
	
	public IngestedModel withSizeBytes(int size) {
		this.sizeBytes = size;
		return this;
	}


	public IngestedModel withStars(int stars) {
		this.stars = stars;
		return this;
	}
	
	public IngestedModel withForks(int forks) {
		this.forks = forks;
		return this;
	}
	
	public IngestedModel withDescription(String description) {
		this.description = description;
		return this;
	}

	public IngestedModel withExplicitName(String name) {
		this.explicitName = name;
		return this;
	}

	public IngestedModel withTopics(String... topics) {
		Collections.addAll(this.topics, topics);
		return this;
	}
	
	/**
	 * Example: github:ecore:/jesusc/rubytl/mymodel.xmi
	 */
	public static String newId(@Nonnull String originName, @Nonnull String modelType, @Nonnull String originUniqueName) {
		return originName + ":" + modelType + ":/" + originUniqueName;
	}

}
