package mar.artefacts.db;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * The expected database schema:
 *  
 * ```
 * CREATE TABLE projects (project_path VARCHAR(255), name VARCHAR(255), PRIMARY KEY (project_path));
 * CREATE TABLE files (project_path VARCHAR(255), file_path TEXT, filename VARCHAR(255), extension VARCHAR(32), type VARCHAR(32), PRIMARY KEY (file_path));
 * ```
 * 
 * @author jesus
 */
public class RepositoryDB implements AutoCloseable {

	private Connection connection;
	private Path rootFolder;
	private PreparedStatement filesByType;
	
	@Nonnull
	public RepositoryDB(@Nonnull Path rootFolder, @Nonnull File file) throws SQLException {
		String url = getConnectionString(file);
		this.connection = DriverManager.getConnection(url);
		this.rootFolder = rootFolder;
			
		String query = "SELECT file_path FROM files WHERE type = ?";
		this.filesByType = connection.prepareStatement(query);
	}

	@Override
	public void close() throws Exception {
		// filesByType.close();
		connection.close();
	}
	
	
	@Nonnull
	private static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}

	public List<RepoFile> getFilesByType(String type) throws SQLException {
		//String query = "SELECT file_path FROM files WHERE type = ?";
		//PreparedStatement filesByType = connection.prepareStatement(query);
		
		filesByType.setString(1, type);
		filesByType.execute();
		ResultSet rs = filesByType.getResultSet();
		
		List<RepoFile> files = new ArrayList<>(1024);
		while (rs.next()) {
			files.add(new RepoFile(rootFolder, rs.getString(1)));
		}
		
		return files;
	}
	
	public static class RepoFile {

		private Path rootFolder;
		private Path filePath;

		public RepoFile(Path rootFolder, String filePath) {
			this.rootFolder = rootFolder;
			this.filePath = Paths.get(filePath).normalize();
		}

		public Path getFullPath() {
			return rootFolder.resolve(filePath);
		}
		
		public Path getRelativePath() {
			return filePath;
		}

		public Path getProjectPath() {
			return filePath.subpath(0, 2);
		}
		
	}

	

}
