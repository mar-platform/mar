package mar.spark.indexer;

import java.io.Serializable;

import javax.annotation.Nonnull;

import mar.paths.PartitionedPathMap;

@SuppressWarnings("serial")
public class ModelPaths implements IModelPaths, Serializable {

	@Nonnull
	public final PartitionedPathMap pathMap;
	@Nonnull
	public final ModelOrigin origin;

	public ModelPaths(@Nonnull PartitionedPathMap pathMap, @Nonnull ModelOrigin origin) {
		this.pathMap = pathMap;
		this.origin = origin;
	}
}