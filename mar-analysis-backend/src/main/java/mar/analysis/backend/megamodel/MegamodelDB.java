package mar.analysis.backend.megamodel;

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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class MegamodelDB implements Closeable {

	@Nonnull
	private Connection connection;
	private Map<String, Artefact> allArtefacts;
	
	@Nonnull	
	public MegamodelDB(File file) {					
		String url = getConnectionString(file);
		 
        try {
        	Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
            	if (! file.exists()) {
	                DatabaseMetaData meta = conn.getMetaData();
	                System.out.println("The driver name is " + meta.getDriverName());
	                System.out.println("A new database has been created.");
            	}
            	
                String artefacts = "CREATE TABLE IF NOT EXISTS artefacts (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    type          varchar(255) NOT NULL,\n"
                        + "    category      varchar(255) NOT NULL,\n"
                        + "    name          varchar(255) NOT NULL\n"
                        + ");";

                String relationships = "CREATE TABLE IF NOT EXISTS relationships (\n"
                        + "    source    varchar(255) NOT NULL,\n"  // FK (artefact)
                        + "    target    varchar(255) NOT NULL,\n"  // FK (artefact)
                        + "    type  varchar (255) NOT NULL"                      
                        + ");";

                Statement stmt = conn.createStatement();
                stmt.execute(artefacts);
                
                stmt = conn.createStatement();
                stmt.execute(relationships);
            }
                        
            this.connection = conn;
            this.connection.setAutoCommit(true);
            
            this.allArtefacts = getArtefacts();            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}

	private Map<String, Artefact> getArtefacts() throws SQLException {
		Map<String, Artefact> result = new HashMap<String, Artefact>();     
		PreparedStatement allArtefactsStm = connection.prepareStatement("SELECT id, type, category, name FROM artefacts");
		allArtefactsStm.execute();
		ResultSet rs = allArtefactsStm.getResultSet();
		while (rs.next()) {
			String id = rs.getString(1);
			String type = rs.getString(2);
			String category = rs.getString(3);
			String name = rs.getString(4);
			result.put(id, new Artefact(id, type, category, name));
		}
		allArtefactsStm.close();
		return result;
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
	
	@Nonnull
	public Map<? extends String, ? extends Artefact> getAllArtefacts() {
		return allArtefacts;
	}

	public void getRelationshipsByType(Relationship relationship, @Nonnull RelationshipConsumer consumer) {
		try {
			PreparedStatement stm = connection.prepareStatement("SELECT source, target, type FROM relationships WHERE type = ?");
			stm.setString(1, relationship.getKind());
			stm.execute();
			ResultSet rs = stm.getResultSet();
	        while (rs.next()) {
	        	String src = rs.getString(1);
	        	String target = rs.getString(2);
	        	Relationship rel = Relationship.getByKind(rs.getString(3));
	
	        	consumer.accept(src, target, rel);
	        }	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	
	@CheckForNull
	public void addRelationship(@Nonnull String sourceId, @Nonnull String targetId, @Nonnull Relationship type) {		
		try {
			Preconditions.checkState(allArtefacts.containsKey(sourceId));
			Preconditions.checkState(allArtefacts.containsKey(targetId));						

			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO relationships(source, target, type) VALUES (?, ?, ?)");
			preparedStatement.setString(1, sourceId);
			preparedStatement.setString(2, targetId);
			preparedStatement.setString(3, type.getKind());			
			preparedStatement.executeUpdate();
			preparedStatement.close();			
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}


	@CheckForNull
	public void addArtefact(@Nonnull String id, @Nonnull String type, @Nonnull String category, @Nonnull String name) {
		try {
			if (allArtefacts.containsKey(id)) {				
				return;
			}
						
			// We can insert
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO artefacts(id, type, category, name) VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, id);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, category);
			preparedStatement.setString(4, name);
			preparedStatement.executeUpdate();
			preparedStatement.close();			
			
			allArtefacts.put(id, new Artefact(id, type, category, name));
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}

	public void dump(@Nonnull RelationshipsGraph graph) {
		for (Node node : graph.getNodes()) {
			Artefact artefact = node.getArtefact();
			addArtefact(artefact.getId(), artefact.getType(), artefact.getCategory(), artefact.getName());
		}
		
		for (Edge edge : graph.getEdges()) {
			addRelationship(edge.getSourceId(), edge.getTargetId(), edge.getType());
		}		
	}

	
	@FunctionalInterface
	public static interface RelationshipConsumer {
		public void accept(String source, String target, Relationship type);
	}

}