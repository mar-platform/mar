package mar.spark.indexer;

import java.io.File;
import java.io.Serializable;

import javax.annotation.Nonnull;

import mar.indexer.common.configuration.SingleIndexJob;

public class ModelOrigin implements Serializable {
	private static final long serialVersionUID = -8782933506244860182L;

	@Nonnull
	private String fullPath;
	@Nonnull
	private String modelId;
	@Nonnull
	private String metadata;
	@Nonnull
	private SingleIndexJob repoConf;

	public ModelOrigin(@Nonnull String fullPath, @Nonnull String modelId, @Nonnull SingleIndexJob repoConf, @Nonnull String metadata) {
		this.fullPath = fullPath;
		this.modelId = modelId;
		this.metadata = metadata;
		this.repoConf = repoConf;
	}

	public SingleIndexJob getRepoConf() {
		return repoConf;
	}
	
	public String getFullPath() {
		return fullPath;
	}
	
	public String getModelId() {
		return modelId;
	}

	public String getMetadata() {
		return metadata;
	}
	
	public String getAbsolutePath() {
		return new File(fullPath).getAbsolutePath();
	}

	@Nonnull
	public File getModelFile() {
		return new File(fullPath);
	}
}
