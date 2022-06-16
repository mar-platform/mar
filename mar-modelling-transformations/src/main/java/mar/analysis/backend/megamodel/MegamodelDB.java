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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.Project;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.ArtefactNode;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import mar.analysis.megamodel.model.RelationshipsGraph.VirtualNode;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.graph.RecoveryStats.PerFile;

public class MegamodelDB implements Closeable {

	@Nonnull
	private Connection connection;
	private Map<String, Artefact> allArtefacts;
	private Map<String, String> allVirtualNodes;
	
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
            	
            	String projects = "CREATE TABLE IF NOT EXISTS projects (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    url           text NOT NULL"
                        + ");";
            	
                String artefacts = "CREATE TABLE IF NOT EXISTS artefacts (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    type          varchar(255) NOT NULL,\n"
                        + "    category      varchar(255) NOT NULL,\n"
                        + "    name          varchar(255) NOT NULL,\n"
                        + "    project_id    varchar(255)\n"
                        + ");";

                String virtualNodes = "CREATE TABLE IF NOT EXISTS virtual_nodes (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    kind          varchar(255) NOT NULL"
                        + ");";
                
                String relationships = "CREATE TABLE IF NOT EXISTS relationships (\n"
                        + "    source    varchar(255) NOT NULL,\n"  // FK (artefact)
                        + "    target    varchar(255) NOT NULL,\n"  // FK (artefact)
                        + "    type  varchar (255) NOT NULL"                      
                        + ");";

                String stats = "CREATE TABLE IF NOT EXISTS stats (\n"
                        + "    filename   text NOT NULL,\n"  
                        + "    type       varchar(255) NOT NULL,\n"  
                        + "    potential_programs  int NOT NULL,\n"
                        + "    programs            int NOT NULL"                      
                        + ");";
                
                Statement stmt = conn.createStatement();
                stmt.execute(projects);
                
                stmt = conn.createStatement();
                stmt.execute(artefacts);
                
                stmt = conn.createStatement();
                stmt.execute(virtualNodes);
                
                stmt = conn.createStatement();
                stmt.execute(relationships);
                
                stmt = conn.createStatement();
                stmt.execute(stats);                
            }
                        
            this.connection = conn;
            this.connection.setAutoCommit(true);
            
            this.allArtefacts = getArtefacts(); 
            this.allVirtualNodes = new HashMap<String, String>();
            getVirtualNodes(allVirtualNodes::put);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}

	private Map<String, Artefact> getArtefacts() throws SQLException {
		Map<String, Artefact> result = new HashMap<String, Artefact>();     
		PreparedStatement allArtefactsStm = connection.prepareStatement("SELECT id, type, category, name, project_id FROM artefacts");
		allArtefactsStm.execute();
		ResultSet rs = allArtefactsStm.getResultSet();
		while (rs.next()) {
			String id = rs.getString(1);
			String type = rs.getString(2);
			String category = rs.getString(3);
			String name = rs.getString(4);
			String projectId = rs.getString(5);
			result.put(id, new Artefact(new Project(projectId), id, type, category, name));
		}
		allArtefactsStm.close();
		return result;
	}
	
	public void getVirtualNodes(BiConsumer<String, String> consumer) {
		try (PreparedStatement allVirtualStm = connection.prepareStatement("SELECT id, kind FROM virtual_nodes")) {
			allVirtualStm.execute();
			ResultSet rs = allVirtualStm.getResultSet();
			while (rs.next()) {
				String id = rs.getString(1);
				String kind = rs.getString(2);
				consumer.accept(id, kind);
			}
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

	@Nonnull
	public static String getConnectionString(File file) {
		return "jdbc:sqlite:" + file.getAbsolutePath();
	}
	
	@Nonnull
	public Map<? extends String, ? extends Artefact> getAllArtefacts() {
		return allArtefacts;
	}

	@Nonnull
	public void getProjectArtefacts(String projectId, BiConsumer<String, Artefact> consumer) {
		allArtefacts.forEach((id, a) -> {
			if (a.getProject().getId().equals(projectId)) {
				consumer.accept(id, a);
			}
		});
	}

	
	public void getRelationshipsByType(@Nonnull RelationshipConsumer consumer, Relationship... relationship) {
		try {
			String types = Arrays.stream(relationship).map(r -> "'" + r.getKind() + "'").collect(Collectors.joining(","));
			PreparedStatement stm = connection.prepareStatement("SELECT source, target, type FROM relationships WHERE type in (" + types + ")");
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


	public List<Project> searchProjects(String value) {
		// TODO: Do this better. The db should have the notion of project
		return getAllArtefacts().values().stream().
			filter(a -> a.getId().toLowerCase().contains(value.toLowerCase())).
			map(a -> a.getId()).
			filter(p -> p.contains("/")).
			map(p -> p.split("/")[0] + "/" + p.split("/")[1]).
			distinct().
			map(p -> new Project(p)).
			collect(Collectors.toList());
	}
	
	@CheckForNull
	public void addRelationship(@Nonnull String sourceId, @Nonnull String targetId, @Nonnull Relationship type) {		
		try {
			Preconditions.checkState(allArtefacts.containsKey(sourceId) || allVirtualNodes.containsKey(sourceId));
			Preconditions.checkState(allArtefacts.containsKey(targetId) || allVirtualNodes.containsKey(targetId) );						

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
	public void addArtefact(@Nonnull Project project, @Nonnull String id, @Nonnull String type, @Nonnull String category, @Nonnull String name) {
		try {
			if (allArtefacts.containsKey(id)) {				
				return;
			}
						
			// We can insert
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO artefacts(id, type, category, name, project_id) VALUES (?, ?, ?, ?, ?)");
			preparedStatement.setString(1, id);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, category);
			preparedStatement.setString(4, name);
			preparedStatement.setString(5, project.getId());
			preparedStatement.executeUpdate();
			preparedStatement.close();			
			
			allArtefacts.put(id, new Artefact(project, id, type, category, name));
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}

	@CheckForNull
	public void addVirtualNode(@Nonnull String id, @Nonnull String kind) {
		if (this.allVirtualNodes.containsKey(id))
			return;
		
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO virtual_nodes(id, kind) VALUES (?, ?)");
			preparedStatement.setString(1, id);
			preparedStatement.setString(2, kind);
			preparedStatement.executeUpdate();
			preparedStatement.close();			
			allVirtualNodes.put(id, kind);
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}

	
	@CheckForNull
	public void addProject(@Nonnull String id, @Nonnull String url) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO projects(id, url) VALUES (?, ?)");
			preparedStatement.setString(1, id);
			preparedStatement.setString(2, url);
			preparedStatement.executeUpdate();
			preparedStatement.close();			
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}
	
	private void addFileStats(PerFile f) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO stats(filename, type, potential_programs, programs) VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, f.getPath().toString());
			preparedStatement.setString(2, f.getType());
			preparedStatement.setInt(3, f.getPotentialPrograms());
			preparedStatement.setInt(4, f.getPrograms());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}

	public void dump(@Nonnull RelationshipsGraph graph, RecoveryStats.Composite composite) {
		for (Project p : graph.getProjects()) {
			addProject(p.getId(), p.getURL());
		}
		
		for (Node node : graph.getNodes()) {
			if (node instanceof ArtefactNode) {
				Artefact artefact = ((ArtefactNode) node).getArtefact();
				addArtefact(artefact.getProject(), artefact.getId(), artefact.getType(), artefact.getCategory(), artefact.getName());
			} else if (node instanceof VirtualNode) {
				VirtualNode vn = (VirtualNode) node;
				addVirtualNode(vn.getId(), vn.getKind());
			}
		}
		
		for (Edge edge : graph.getEdges()) {
			for (Relationship relationship : edge.getTypes()) {
				addRelationship(edge.getSourceId(), edge.getTargetId(), relationship);				
			}
		}		
		
		for (PerFile f : composite.getSingleStats()) {
			addFileStats(f);
		}
	}

	@FunctionalInterface
	public static interface RelationshipConsumer {
		public void accept(String source, String target, Relationship type);
	}


}