package mar.validation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import mar.ingestion.IngestedModel;
import mar.ingestion.IngestionDB;

/**
 * Gathers files to make them available to the validators. 
 * 
 * @author jesus
 */
public interface IFileProvider {
	
	@Nonnull
	List<? extends IFileInfo> getLocalFiles();
	
	public static class DefaultFileProvider implements IFileProvider {
		@Nonnull
		private final File rootFolder;
		@Nonnull
		private final List<IFileInfo> files;
		
		public DefaultFileProvider(@Nonnull File repository, @Nonnull File fileListFile) throws IOException {
			rootFolder = repository;
			files = toFileInfo(Files.readAllLines(fileListFile.toPath()));
		}
		
		public DefaultFileProvider(@Nonnull File repository, @Nonnull String... extensions) throws IOException {
			rootFolder = repository;
			files = toFileInfo(Files.walk(Paths.get(repository.toString())).
				filter(Files::isRegularFile).
				// TODO: Change this
				
				filter(f -> hasExtension(f.toFile(), extensions)).
				map(f -> repository.toPath().relativize(f).toString()).
				collect(Collectors.toList()));
		}

		private boolean hasExtension(@Nonnull File file, String[] extensions) {
			if (extensions.length == 0)
				return true;
			for (String ext : extensions) {
				return file.getName().endsWith("." + ext);
			}
			return false;
		}

		@Nonnull
		private List<IFileInfo> toFileInfo(@Nonnull List<? extends String> allFiles) {
			List<IFileInfo> files = new ArrayList<IFileInfo>();
			for (String f : allFiles) {
				files.add(new IFileInfo.FileInfo(rootFolder, f));
			}
			return files;
		}

		@Override
		public List<IFileInfo> getLocalFiles() {
			return files;
		}		
	}

	public static class DBFileProvider implements IFileProvider {

		private List<? extends IngestedModel> models;

		public DBFileProvider(@Nonnull IngestionDB ingestionDB) {
			this.models = ingestionDB.getModels();
		}
		
		@Override
		public List<? extends IFileInfo> getLocalFiles() {			
			return models;
		}
		
	}
}
