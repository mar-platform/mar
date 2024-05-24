package mar.embeddings;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.indexer.embeddings.VectorizedPath;

/**
 * This database contains the models that have been indexed by the vector database.
 */
public class PathIndexesDB implements Closeable {

	@Nonnull
	protected Connection connection;
	
	protected boolean keepPath = false;
	
	public static enum Mode {
		READ,
		WRITE
	}
	
	@Nonnull	
	public PathIndexesDB(File file, Mode mode) {					
		String url = getConnectionString(file);
		 
        try {
        	Connection conn = DriverManager.getConnection(url);
        	Preconditions.checkNotNull(conn);
            
        	if (! file.exists()) {
        		if (mode == Mode.READ)
        			throw new IllegalStateException("No db " + file);
        		
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
        	}
        	createTables(conn);
        	        
            this.connection = conn;
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}
	
	private void createTables(Connection conn) throws SQLException {
        String models = "CREATE TABLE IF NOT EXISTS paths (\n"
                + "    seq_id     INTEGER PRIMARY KEY,\n"
                + "    path       TEXT,\n"
                + "    path_ids   TEXT,\n"
        		+ "    vector     TEXT\n"
        		+ ");";
        
        
        Statement stmt = conn.createStatement();
        stmt.execute(models);        
	}

	public void setAutocommit(boolean autocommit) {
		try {
			this.connection.setAutoCommit(autocommit);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close() throws IOException {
		if (connection != null) {
			try {
				if (! connection.getAutoCommit())
					connection.commit();
				connection.close();
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}
	}

	public void commit() {
		try {
			if (! connection.getAutoCommit())
				connection.commit();
		} catch (SQLException e) {
			// throw new IllegalStateException(e);
		}
	}

	
	@Nonnull
	public static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}

	public void addPath(@Nonnull VectorizedPath vp) {
		String query = keepPath ? 
				"INSERT INTO paths(seq_id, path_ids, vector, path) VALUES (?, ?, ?, ?)" :
				"INSERT INTO paths(seq_id, path_ids, vector) VALUES (?, ?, ?)";
				
		try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
			// We can insert
			preparedStatement.setInt(1, vp.getSeqId());
			preparedStatement.setString(2, vp.toPathIdsString());
			preparedStatement.setString(3, vp.toVectorString());			
			
			if (keepPath) {
				preparedStatement.setString(4, vp.getPathText());
			}
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}
	
	public int[] getPaths(int seqId) {
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT path_ids from paths WHERE seq_id = ?")) {
			preparedStatement.setInt(1, seqId);
			ResultSet rs = preparedStatement.executeQuery();
			if (! rs.next())
				return null;
			
			String paths = rs.getString(1);
			return VectorizedPath.fromPathIdsStrings(paths);
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}
	
}