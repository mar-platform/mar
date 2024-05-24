package mar.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class SqliteIndexDatabase implements AutoCloseable {

	private Connection connection;
	private PreparedStatement stmInsert;
	private PreparedStatement stmInsertMetadata;

	@Nonnull	
	public SqliteIndexDatabase(File file) {					
		String url = getConnectionString(file);
        try {
        	Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
            	if (! file.exists()) {
	                DatabaseMetaData meta = conn.getMetaData();
	                System.out.println("The driver name is " + meta.getDriverName());
	                System.out.println("A new database has been created.");
            	}
            	
                String index_tbl = "CREATE TABLE IF NOT EXISTS mar_index (\n"
                		+ "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "    path        text NOT NULL,\n"
                        + "    doc_id      text NOT NULL,\n"
                        + "    n_occurences integer NOT NULL,\n"
                        + "    n_tokens     integer NOT NULL,\n"
                        + "    n_docs_t     integer\n"
                        + ");";
                
                String stats_tbl = "CREATE TABLE IF NOT EXISTS stats (\n"
                		+ "    id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "    total_tokens    integer NOT NULL,\n"
                        + "    total_documents integer NOT NULL\n"                       
                        + ");";

                String metadata_tbl = "CREATE TABLE IF NOT EXISTS metadata (\n"
                		+ "    doc_id text NOT NULL PRIMARY KEY,"
                        + "    metadata text NOT NULL\n"                       
                        + ");";
                                
                //String index = "create index if not exists idx_path on mar_index(path);";
                String index = "create index if not exists idx_path on mar_index(path, doc_id, n_occurences, n_tokens, n_docs_t);";
                
                Statement stmt = conn.createStatement();
                stmt.execute(index_tbl);

                stmt = conn.createStatement();
                stmt.execute(stats_tbl);
                
                stmt = conn.createStatement();
                stmt.execute(metadata_tbl);
                
                stmt = conn.createStatement();
                stmt.execute(index);
                
                this.connection = conn;
                this.connection.setAutoCommit(false);            

                stmInsert = connection.prepareStatement("INSERT INTO mar_index(path, doc_id, n_occurences, n_tokens) VALUES (?, ?, ?, ?)");                
                stmInsertMetadata = connection.prepareStatement("INSERT INTO metadata(doc_id, metadata) VALUES (?, ?)");                

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}

	@Nonnull
	private static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}

	public void add(String path, String docId, int nocurrences, int ntokens) throws SQLException {
		stmInsert.setString(1, path);
		stmInsert.setString(2, docId);
		stmInsert.setInt(3, nocurrences);
		stmInsert.setInt(4, ntokens);
		stmInsert.execute();
	}

	public void addModel(String modelId, String metadata) throws SQLException {
		stmInsertMetadata.setString(1, modelId);
		stmInsertMetadata.setString(2, metadata);
		stmInsertMetadata.execute();
	}

	@Override
	public void close() throws Exception {
		if (this.connection != null) {
			stmInsert.close();
			stmInsertMetadata.close();
			this.connection.commit();
			PreparedStatement opt = this.connection.prepareStatement("PRAGMA optimize;");
			opt.execute();
			opt.close();
			this.connection.close();
		}
	}

	public void getModels(String query, SqlitePathConsumer consumer) throws SQLException {
		PreparedStatement stm = this.connection.prepareStatement(query);
		stm.execute();
		
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			// doc_id, n_occurences, n_tokens
			String path  = rs.getString(1);
			String docId = rs.getString(2);
			int numDocsWithPath = rs.getInt(3);
			int nOccurences = rs.getInt(4);
			int nTokens = rs.getInt(5);
			consumer.consume(path, docId, numDocsWithPath, nTokens, nOccurences);
		}
		stm.close();
	}

	public void getPaths(SqliteSimplePathConsumer consumer) throws SQLException {
		PreparedStatement stm = this.connection.prepareStatement("select path, id from mar_index");
		stm.execute();
		
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			String path  = rs.getString(1);
			long pathId = rs.getLong(2);
			consumer.consume(pathId, path);
		}
		stm.close();
	}
	
	public void addGlobalStats(long totalTokens, long totalDocuments) throws SQLException {
		PreparedStatement stm = connection.prepareStatement("INSERT INTO stats(total_tokens, total_documents) VALUES (?, ?)");
		stm.setLong(1, totalTokens);
		stm.setLong(2, totalDocuments);
		stm.execute();
		stm.close();
	}	

	public String getModelMetadataById(String modelId) throws SQLException {
		PreparedStatement stm = connection.prepareStatement("SELECT metadata FROM metadata WHERE doc_id = ?");
		stm.setString(1, modelId);
		stm.execute();
		ResultSet rs = stm.getResultSet();
		if (rs.next()) {
			return rs.getString(1);
		}
		stm.close();
		return null;		
	}

	public int getPathCount() throws SQLException {
		PreparedStatement stm = connection.prepareStatement("SELECT count(*) FROM mar_index");
		stm.execute();
		ResultSet rs = stm.getResultSet();
		if (rs.next()) {
			return rs.getInt(1);
		}
		stm.close();
		throw new IllegalStateException();	
	}


	public String getPath(Integer pathId) throws SQLException {
		PreparedStatement stm = connection.prepareStatement("SELECT path FROM mar_index WHERE id = ?");
		stm.setLong(1, pathId);
		stm.execute();
		ResultSet rs = stm.getResultSet();
		if (rs.next()) {
			return rs.getString(1);
		}
		stm.close();

		throw new IllegalArgumentException("No path with id " + pathId);	
	}
	
	@CheckForNull
	public Stats getStats() throws SQLException {
		PreparedStatement stm = connection.prepareStatement("SELECT total_tokens, total_documents FROM stats LIMIT 1");
		stm.execute();
		ResultSet rs = stm.getResultSet();
		if (rs.next()) {
			return new Stats(rs.getLong(1), rs.getLong(2));
		}
		stm.close();
		return null;
	}

	public static class Stats {
		public final long totalTokens;
		public final long totalDocuments;
		
		public Stats(long totalTokens, long totalDocuments) {
			this.totalTokens = totalTokens;
			this.totalDocuments = totalDocuments;
		}
	}

	public void updateDocCounts() throws SQLException {
		String update = "with counts(path, n_docs_t) as (select path, count(*) from mar_index group by path) " + 
	       "update mar_index set n_docs_t = (select n_docs_t from counts where counts.path = mar_index.path);\n";
		PreparedStatement stm = connection.prepareStatement(update);
		stm.execute();
		stm.close();
		
		// Use:
		//    select id, path from mar_index where id in (select id from mar_index group by path having count(*) > (select total_documents * 0.7 from stats limit 1));
		// As the basis to drop stop paths
	}

}
