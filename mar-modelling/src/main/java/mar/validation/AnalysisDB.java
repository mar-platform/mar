package mar.validation;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;

public class AnalysisDB implements Closeable {

	public enum Status {
		NOT_PROCESSED, DUPLICATED, TOO_SMALL, VALID, CRASHED, TIMEOUT, NO_VALIDATE, NOT_HANDLED

	}

	@Nonnull
	protected Connection connection;
	@Nonnull
	private Map<String, Status> alreadyChecked = new HashMap<>();
	private boolean isReadOnly;
	
	@Nonnull	
	public AnalysisDB(File file) {					
		String url = getConnectionString(file);
		 
        try {
        	Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
            	if (! file.exists()) {
	                DatabaseMetaData meta = conn.getMetaData();
	                System.out.println("The driver name is " + meta.getDriverName());
	                System.out.println("A new database has been created.");
            	}
            	
                String models = "CREATE TABLE IF NOT EXISTS models (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    relative_file text NOT NULL,\n"
                        + "    hash          text NOT NULL,\n"
                        + "    status        varchar(255) NOT NULL,\n"
                        + "    metadata_document TEXT,\n"
                        + "    duplicate_of  varchar(255)\n"  // in case it is a duplicate
                        + ");";

                String stats = "CREATE TABLE IF NOT EXISTS stats (\n"
                        + "    id    varchar(255) NOT NULL,\n"  // FK (models)
                        + "    type  varchar (255) NOT NULL,\n"    // e.g., total_elements, statemachine, num_classes  
                        + "    count integer NOT NULL\n"                          
                        + ");";

                String metadata = "CREATE TABLE IF NOT EXISTS metadata (\n"
                        + "    id    varchar(255) NOT NULL,\n"  // FK (models)
                        + "    type  varchar (255) NOT NULL,\n"    // e.g., total_elements, statemachine, num_classes  
                        + "    value text NOT NULL\n"                          
                        + ");";
                
                String index = "create index if not exists idx_metadata on metadata(type, value);";
                String indexByPath = "create index if not exists by_path on models(relative_file);";
                
                Statement stmt = conn.createStatement();
                stmt.execute(models);
                
                stmt = conn.createStatement();
                stmt.execute(stats);

                stmt = conn.createStatement();
                stmt.execute(metadata);

                stmt = conn.createStatement();
                stmt.execute(index);

                stmt = conn.createStatement();
                stmt.execute(indexByPath);                
            }
                        
            this.connection = conn;
            this.connection.setAutoCommit(false);
            
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}
	
	@Nonnull
	public static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}

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

	public List<Model> getValidModels(@Nonnull Function<String, String> relativePathTransformer) throws SQLException {
		List<Model> models = new ArrayList<>(1024);
		PreparedStatement statement = connection.prepareStatement(getValidModelsQuery());
		statement.execute();
		
		ResultSet rs = statement.getResultSet();
		while (rs.next()) {
			String id = rs.getString(2);
			Path relative = Paths.get(rs.getString(1));
			File file = new File(relativePathTransformer.apply(rs.getString(1)));
			String metadata = rs.getString(3);
			models.add(new Model(id, relative, file, metadata));			
		}
		
		return models;
	}
	
	@CheckForNull
	public Status addFile(@Nonnull String modelId, @Nonnull String relativeName, @Nonnull String hash) {
		Preconditions.checkState(! isReadOnly);
		try {
			Status status = Status.NOT_PROCESSED;
			
			// We check that the hash is not repeated
			String originalModelId = null;
			PreparedStatement hashStatement = connection.prepareStatement("SELECT id FROM models WHERE hash = ? AND duplicate_of IS NULL");
			hashStatement.setString(1, hash);
			hashStatement.execute();
			ResultSet rs = hashStatement.getResultSet();
			if (rs.next()) {
				originalModelId = rs.getString(1);
				status = Status.DUPLICATED;
			}
			hashStatement.close();

			// We can insert
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO models(id, relative_file, hash, status, duplicate_of) VALUES (?, ?, ?, ?, ?)");
			preparedStatement.setString(1, modelId);
			preparedStatement.setString(2, relativeName);
			preparedStatement.setString(3, hash);			
			preparedStatement.setString(4, status.name());
			preparedStatement.setString(5, originalModelId);
			preparedStatement.executeUpdate();
			preparedStatement.close();			
			
			return status;
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}


	@CheckForNull
	public Status hasFile(@Nonnull String modelId) {
		return alreadyChecked.get(modelId);
	}
	
	@CheckForNull
	private Status getStatus(@Nonnull String modelId)  {
		try {
			PreparedStatement stm = connection.prepareStatement("SELECT status FROM models WHERE id = ?");
			stm.setString(1, modelId);
			stm.execute();
			ResultSet rs = stm.getResultSet();
			if (! rs.next())
				return null;
			return Status.valueOf(rs.getString(1));
		} catch (SQLException e) {
			throw new RuntimeException(e);			
		}
	}

	@CheckForNull
	public String getRelativeFile(@Nonnull String modelId) {
		try {
			PreparedStatement stm = connection.prepareStatement("SELECT relative_file FROM models WHERE id = ?");
			stm.setString(1, modelId);
			stm.execute();
			ResultSet rs = stm.getResultSet();
			if (! rs.next())
				return null;
			return rs.getString(1);
		} catch (SQLException e) {
			throw new RuntimeException(e);			
		}
	}
	
	public void updateStatus(@Nonnull String modelId, @Nonnull Status status, @CheckForNull String jsonDocument) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE models SET status = ?, metadata_document = ? WHERE id = ?");
			preparedStatement.setString(1, status.name());
			preparedStatement.setString(2, jsonDocument);
			preparedStatement.setString(3, modelId);
			int count = preparedStatement.executeUpdate();
			Preconditions.checkState(count == 1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void updateMetadata(@Nonnull String modelId, @CheckForNull String jsonDocument) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE models SET metadata_document = ? WHERE id = ?");
			preparedStatement.setString(1, jsonDocument);
			preparedStatement.setString(2, modelId);
			int count = preparedStatement.executeUpdate();
			Preconditions.checkState(count == 1);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void addStats(@Nonnull String modelId, @Nonnull String type, int count) {
		Preconditions.checkState(! isReadOnly);
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO stats(id, type, count) VALUES (?, ?, ?)");
			preparedStatement.setString(1, modelId);
			preparedStatement.setString(2, type);
			preparedStatement.setInt(3, count);
			preparedStatement.execute();
			preparedStatement.close();			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}			
	}

	public void addMetadata(@Nonnull String modelId, @Nonnull String type, @Nonnull String value) {
		Preconditions.checkState(! isReadOnly);
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO metadata(id, type, value) VALUES (?, ?, ?)");
			preparedStatement.setString(1, modelId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, value);
			preparedStatement.execute();
			preparedStatement.close();			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}			
	}

	
	private Map<String, Model> modelByPathCache = null;
	
	@CheckForNull
	public Model getModelByPath(String relativePath, @Nonnull Function<String, String> relativePathTransformer) {
		Preconditions.checkNotNull(relativePath);
		if (isReadOnly) {
			if (modelByPathCache == null) {
				modelByPathCache = new HashMap<>();
				try(PreparedStatement stm = connection.prepareStatement("SELECT m.id, relative_file, metadata_document, value, type FROM models m, metadata mm WHERE m.id = mm.id ORDER BY relative_file, type")) {
					stm.execute();
					ResultSet rs = stm.getResultSet();
					Model m = null;
					while (rs.next()) {
						String id = rs.getString(1);
						if (m == null || !m.getId().equals(id)) {
							m = getModelFromRecord(rs, relativePathTransformer);
							modelByPathCache.put(m.getRelativePath().toString(), m);
						} else {
							String type = rs.getString(5);
							String metadataValue = rs.getString(4);
							m.putKeyValueMetadata(type, metadataValue);
						}
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}			
			}
			return modelByPathCache.get(relativePath);
		}
		
		try (PreparedStatement stm = connection.prepareStatement("SELECT m.id, relative_file, metadata_document, value, type FROM models m, metadata mm WHERE m.id = mm.id AND relative_file = ?")) {
			stm.setString(1, relativePath);
			//stm.setString(2, metadataType); // Typically nsURI
			stm.execute();
			ResultSet rs = stm.getResultSet();			
		
			// Previously, we had 'AND type = ?' in the query above so that we get only one result. However this has a performance impact
			// becase we can't have multi-table indexes. Since there are typically few metadata records, we check programatically.
			Model m = null;
			while (rs.next()) {
				if (m != null) {
					m = getModelFromRecord(rs, relativePathTransformer);
				}
				String metadataValue = rs.getString(4);
				String type = rs.getString(5);			
				m.putKeyValueMetadata(type, metadataValue);
			}
			return null;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}			
	}

	private Model getModelFromRecord(ResultSet rs, Function<String, String> relativePathTransformer)
			throws SQLException {
		String id = rs.getString(1);
		Path relative = Paths.get(rs.getString(2));
		File fullFile = new File(relativePathTransformer.apply(rs.getString(2)));
		String metadataDocument = rs.getString(3);
		String metadataValue = rs.getString(4); // This is because we have two flavours of metadata (a Json document and additional data in a string-map style, which is a pity)			
		String type = rs.getString(5);
		return new Model(id, relative, fullFile, metadataDocument).putKeyValueMetadata(type, metadataValue);
	}

	
	@Nonnull
	public List<Model> findByMetadata(@Nonnull String key, @Nonnull String value, @Nonnull Function<String, String> relativePathTransformer) {
		try (PreparedStatement stm = connection.prepareStatement("SELECT m.id, relative_file, metadata_document FROM models m, metadata mm WHERE m.id = mm.id AND m.status IN ('VALID', 'INVALID') AND mm.type = ? AND mm.value = ?")) {			
			stm.setString(1, key);
			stm.setString(2, value);
			stm.execute();
			
			List<Model> result = new ArrayList<>();
			ResultSet rs = stm.getResultSet();
			while (rs.next()) {
				String id = rs.getString(1);
				File fullFile = new File(relativePathTransformer.apply(rs.getString(2)));
				String metadataDocument = rs.getString(3);
				result.add(new Model(id, Paths.get(rs.getString(2)), fullFile, metadataDocument));
			}
			return result;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public static class Model {
		@Nonnull
		private String id;
		@Nonnull
		private File file;
		@Nonnull
		private String metadata;
		@Nonnull
		private Path relativePath;		
		@Nonnull
		private Map<String, String> keyValueMetadata;
		
		public Model(@Nonnull String id, @Nonnull Path relativePath, @Nonnull File file, String metadata) {
			this.id = id;
			this.file = file;
			this.relativePath = relativePath;
			this.metadata = metadata;
		}		
		
		protected Map<String, String> getKeyValueMetadata() {
			if (keyValueMetadata == null)
				keyValueMetadata = new HashMap<>();
			return keyValueMetadata;
		}
		
		public Model putKeyValueMetadata(String metadataType, String metadataValue) {
			getKeyValueMetadata().put(metadataType, metadataValue);
			return this;
		}

		public String getKeyValueMetadata(String metadataType) {
			return getKeyValueMetadata().get(metadataType);
		}
		
		public String getId() {
			return id;
		}

		@Nonnull
		public Path getRelativePath() {
			return relativePath;
		}

		@Nonnull
		public File getFile() {
			return file;
		}
		
		public String getMetadata() {
			return metadata;
		}

	}

}