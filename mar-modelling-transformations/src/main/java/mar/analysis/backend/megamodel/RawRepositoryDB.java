package mar.analysis.backend.megamodel;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import mar.analysis.backend.megamodel.stats.RawRepositoryStats;

public class RawRepositoryDB implements AutoCloseable {

	private Connection connection;

	public RawRepositoryDB(File file) {
		String url = getConnectionString(file);
		 
        try {
        	Connection conn = DriverManager.getConnection(url);
        	this.connection = conn;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } 
	}
	

	@Override
	public void close() throws IOException {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}
	}
	
	@CheckForNull
	public RawFile getArtefactInfo(String artefactId) {
		try {
			PreparedStatement files = connection.prepareStatement("select project_path, file_path, extension, type, created_at, created_author, updated_at, updated_author from files where file_path = ?");
			files.setString(1, artefactId);
			ResultSet rs = files.executeQuery();
			if (! rs.next()) 
				return null;
			
			RawFile rawFile = toRawFile(rs);			
			return rawFile;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@CheckForNull
	public RawProject getProjectInfo(String projectId) {
		try {
			PreparedStatement files = connection.prepareStatement("select id, description, created_at, updated_at from repo_info where id = ?");
			files.setString(1, projectId);
			ResultSet rs = files.executeQuery();
			if (! rs.next()) 
				return null;
			
			RawProject rawFile = toRawProject(rs);			
			return rawFile;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private RawProject toRawProject(ResultSet rs) throws SQLException {
		String id = rs.getString(1);
		String description = rs.getString(2);
		String created_at = rs.getString(3);
		String updated_at = rs.getString(4);
				
		OffsetDateTime createdAt = created_at != null ? OffsetDateTime.parse(created_at) : null;
		OffsetDateTime updatedAt = created_at != null ? OffsetDateTime.parse(updated_at) : null;
		RawProject rawFile = new RawProject(id, description, createdAt, updatedAt);
		return rawFile;
	}	

	
	public Map<String, RawFile> getFiles() {
		try {
			PreparedStatement files;
			files = connection.prepareStatement("select project_path, file_path, extension, type, created_at, created_author, updated_at, updated_author from files");
			Map<String, RawFile> results = new HashMap<>();
			ResultSet rs = files.executeQuery();
			while (rs.next()) {
				RawFile rawFile = toRawFile(rs);			
				results.put(rawFile.getId(), rawFile);
			}
			
			return results;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	private RawFile toRawFile(ResultSet rs) throws SQLException {
		String project = rs.getString(1);
		String filepath = rs.getString(2);
		String extension = rs.getString(3);
		String type = rs.getString(4);
		String created_at = rs.getString(5);
		String created_author = rs.getString(6);
		String updated_at = rs.getString(7);
		String updated_author = rs.getString(8);
				
		OffsetDateTime createdAt = created_at != null ? OffsetDateTime.parse(created_at) : null;
		OffsetDateTime updatedAt = created_at != null ? OffsetDateTime.parse(updated_at) : null;
		RawFile rawFile = new RawFile(project, filepath, extension, type, createdAt, created_author, updatedAt, updated_author);
		return rawFile;
	}	
		
	public RawRepositoryStats getStats() {
		try {
			PreparedStatement artefactCount = connection.prepareStatement("select type, count(*) from files group by type");
			ResultSet rs = artefactCount.executeQuery();
			RawRepositoryStats stats = new RawRepositoryStats();
			while (rs.next()) {
				String type = rs.getString(1);
				long count = rs.getLong(2);
				stats.addArtefactTypeCount(type, count);
			}
			return stats;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<String> getFilesUsing(String filename) {
		try {
			PreparedStatement deps = connection.prepareStatement("select using_file from dependencies where file_path = ?");
			deps.setString(1, filename);
			deps.execute();
			ResultSet rs = deps.getResultSet();
			List<String> files = new ArrayList<>();
			while (rs.next()) {
				String fname = rs.getString(1);
				files.add(fname);
			}
			deps.close();
			return files;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		// CREATE TABLE dependencies (file_path TEXT, using_file TEXT, using_extension VARCHAR(32), PRIMARY KEY (file_path, using_file));

	}

	
	@Nonnull
	public static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}

	public static class RawFile {

		private final String project;
		private final String filepath;
		private final String extension;
		private final String type;
		private OffsetDateTime createdAt;
		private String createdAuthor;
		private OffsetDateTime updatedAt;
		private String updatedAuthor;

		public RawFile(String project, String filepath, String extension, String type, OffsetDateTime createdAt, String createdAuthor, OffsetDateTime updatedAt, String updatedAuthor) {
			this.project = project;
			this.filepath = filepath;
			this.extension = extension;
			this.type = type;
			this.createdAt = createdAt;
			this.createdAuthor = createdAuthor;
			this.updatedAt = updatedAt;
			this.updatedAuthor = updatedAuthor;
		}
		
		public String getProject() {
			return project;
		}
		
		public String getFilepath() {
			return filepath;
		}
		
		public String getId() {
			return filepath;
		}
		
		public String getType() {
			return type;
		}
		
		public String getExtension() {
			return extension;
		}		
		
		public OffsetDateTime getCreatedAt() {
			return createdAt;
		}
		
		public String getCreatedAuthor() {
			return createdAuthor;
		}
		
		public OffsetDateTime getUpdatedAt() {
			return updatedAt;
		}
		
		public String getUpdatedAuthor() {
			return updatedAuthor;
		}
	}


	public static class RawProject {
		private final String id;
		private final String author;
		private final String description;
		private OffsetDateTime createdAt;
		private OffsetDateTime updatedAt;

		public RawProject(String id, String description, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
			this.id = id;
			this.author = id.split("/")[0];
			this.description = description;
			this.createdAt = createdAt;
			this.updatedAt = updatedAt;
		}

		public String getId() {
			return id;
		}

		public String getAuthor() {
			return author;
		}

		public String getDescription() {
			return description;
		}

		public OffsetDateTime getCreatedAt() {
			return createdAt;
		}

		public OffsetDateTime getUpdatedAt() {
			return updatedAt;
		}
	}


}
