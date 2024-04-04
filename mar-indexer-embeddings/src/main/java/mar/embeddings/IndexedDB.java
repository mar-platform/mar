package mar.embeddings;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.validation.AnalysisDB.Model;

/**
 * This database contains the models that have been indexed by the vector database.
 */
public class IndexedDB implements Closeable {

	@Nonnull
	protected Connection connection;
	
	public static enum Mode {
		READ,
		WRITE
	}
	
	@Nonnull	
	public IndexedDB(File file, Mode mode) {					
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
            /*
			PreparedStatement allModels = connection.prepareStatement("SELECT id, status FROM models WHERE status <> ?");
			allModels.setString(1, Status.NOT_PROCESSED.name());
			allModels.execute();
			ResultSet rs = allModels.getResultSet();
            while (rs.next()) {
            	String id = rs.getString(1);
            	Status status = Status.valueOf(rs.getString(2));
            	alreadyChecked.put(id, status);
            }
            allModels.close();     
            
            PreparedStatement removeNotProcessed = connection.prepareStatement("DELETE FROM models WHERE status = ?");
            removeNotProcessed.setString(1,  Status.NOT_PROCESSED.name());
            removeNotProcessed.execute();
            */
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}
	
	private void createTables(Connection conn) throws SQLException {
        String models = "CREATE TABLE IF NOT EXISTS models (\n"
                + "    seq_id     INTEGER PRIMARY KEY AUTOINCREMENT,"
        		+ "    model_id   varchar(255),\n"
                + "    metadata_document TEXT\n"
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

	/*
	@Nonnull
	public static String getValidModelsQuery() {
		return "select relative_file, id, metadata_document from models where status = '" + Status.VALID.name() + "' or status = '" + Status.NO_VALIDATE.name() + "'";
	}
	
	@Nonnull
	public List<String> getValidModels() throws SQLException {
		PreparedStatement statement = connection.prepareStatement(getValidModelsQuery());
		statement.execute();

		List<String> result = new ArrayList<>();
		ResultSet rs = statement.getResultSet();
		while (rs.next()) {
			result.add(rs.getString(1));
		}
		
		return result;
	}
	*/

	public IndexedModel addModel(@Nonnull Model model) {
		try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO models(model_id, metadata_document) VALUES (?, ?)")) {
			// We can insert
			preparedStatement.setString(1, model.getId());
			preparedStatement.setString(2, model.getMetadata());
			preparedStatement.executeUpdate();
			ResultSet keys = preparedStatement.getGeneratedKeys();
			keys.next();
			return new IndexedModel(keys.getInt(1), model);
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}
	

	public IndexedModel getById(int seqId) {
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT model_id, metadata_document from models WHERE seq_id = ?")) {
			// We can insert
			preparedStatement.setInt(1, seqId);
			ResultSet rs = preparedStatement.executeQuery();
			if (! rs.next())
				return null;
			
			String modelId = rs.getString(1);
			String metadata = rs.getString(2);
			Path relativePath = null;
			File file = null;
			
			Model m = new Model(modelId, relativePath, file, metadata);
			
			return new IndexedModel(seqId, m);
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}
	
	public static class IndexedModel {
		private Model analysedModel;
		private int seqId;

		public IndexedModel(int seqId, Model m) {
			this.seqId = seqId;
			this.analysedModel = m;
		}
		
		public int getSeqId() {
			return seqId;
		}

		public File getFile() {
			return analysedModel.getFile();
		}

		public String getModelId() {
			return analysedModel.getId();
		}
	}

}