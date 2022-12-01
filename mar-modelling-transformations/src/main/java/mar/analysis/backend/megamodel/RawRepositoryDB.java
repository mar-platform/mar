package mar.analysis.backend.megamodel;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
	
	@Nonnull
	public static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}
	
}
