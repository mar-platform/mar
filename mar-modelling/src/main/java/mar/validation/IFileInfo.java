package mar.validation;

import java.io.File;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

public interface IFileInfo {

	@Nonnull
	String getModelId();

	@Nonnull
	File getRelativeFile();

	@Nonnull
	File getFullFile();

	@Nonnull
	default String getRelativePath() {
		return getRelativeFile().getPath();
	}
	
	@Nonnull
	public default String getAbsolutePath() {
		return getFullFile().getAbsolutePath();
	}

	public static class FileInfo extends FileInfoById {

		public FileInfo(File root, File fullPath) {
			super(getRelativePath(root, fullPath), getRelativePath(root, fullPath), fullPath.getAbsolutePath());
			Preconditions.checkArgument(fullPath.toPath().startsWith(root.toPath()), "File must be a child of root");
		}

		public FileInfo(File root, String relativeFile) {
			super(relativeFile, relativeFile, Paths.get(root.getAbsolutePath(), relativeFile).toString());
		}

		private static String getRelativePath(File root, File fullPath) {
			return root.toPath().relativize(fullPath.toPath()).toString();		
		}
	}
	
	public static class FileInfoById implements IFileInfo {

		@Nonnull
		private final String modelId;
		private final File file;
		private final File relativeFile;

		public FileInfoById(@Nonnull String modelId, @Nonnull String relativePath, @Nonnull String fullPath) {
			this.modelId = modelId;
			this.relativeFile = new File(relativePath);
			this.file = new File(fullPath);
		}
		
		@Override
		public String getModelId() {
			return modelId;
		}

		@Override
		public File getRelativeFile() {
			return relativeFile;
		}

		@Override
		public File getFullFile() {
			return file;
		}		
	}

}