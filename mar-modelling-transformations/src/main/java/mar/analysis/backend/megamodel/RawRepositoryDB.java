package mar.analysis.backend.megamodel;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mar.analysis.backend.megamodel.stats.RawRepositoryStats;

public class RawRepositoryDB implements AutoCloseable {

	private Connection connection;


	@Nonnull	
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
	
	public List<RawFile> getFiles() throws SQLException {
		PreparedStatement files = connection.prepareStatement("select project_path, file_path, extension, type from files");
		List<RawFile> results = new ArrayList<>();
		ResultSet rs = files.executeQuery();
		while (rs.next()) {
			String project = rs.getString(1);
			String filepath = rs.getString(2);
			String extension = rs.getString(3);
			String type = rs.getString(4);
			results.add(new RawFile(project, filepath, extension, type));
		}
		
		return results;
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

		public RawFile(String project, String filepath, String extension, String type) {
			this.project = project;
			this.filepath = filepath;
			this.extension = extension;
			this.type = type;
		}
		
		public String getProject() {
			return project;
		}
		
		public String getFilepath() {
			return filepath;
		}
		
		public String getType() {
			return type;
		}
		
		public String getExtension() {
			return extension;
		}		
	}

}
