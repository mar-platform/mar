package mar.spark.indexer;

import javax.annotation.Nonnull;

public class TableNameUtils {

	@Nonnull
	public static String getInvertedIndex(@Nonnull String modelType) {
		return "inverted_index_" + modelType;
	}

	@Nonnull
	public static String getStopPaths(@Nonnull String modelType) {
		return "stop_paths_" + modelType;
	}

	@Nonnull
	public static String getGlobalStats(@Nonnull String modelType) {
		return "global_st_" + modelType;
	}

	@Nonnull
	public static String getMetaTable(@Nonnull String modelType) {
		return "meta_table";
	}

	@Nonnull
	public static String getDocsInfo(@Nonnull String modelType) {
		return "docs_info_" + modelType;
	}
	
	@Nonnull
	public static String getDocsInfo() {
		return "docs_info";
	}
}
