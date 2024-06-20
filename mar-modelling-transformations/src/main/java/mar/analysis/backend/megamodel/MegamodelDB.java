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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.analysis.backend.megamodel.stats.MegamodelStats;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.DuplicationRelationships;
import mar.analysis.megamodel.model.Project;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.ArtefactNode;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;
import mar.artefacts.graph.RecoveryStats;
import mar.artefacts.graph.RecoveryStats.PerFile;

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
            	
            	String projects = "CREATE TABLE IF NOT EXISTS projects (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    url           text NOT NULL"
                        + ");";
            	
                String artefacts = "CREATE TABLE IF NOT EXISTS artefacts (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    type          varchar(255) NOT NULL,\n"
                        + "    category      varchar(255) NOT NULL,\n"
                        + "    name          varchar(255) NOT NULL,\n"
                        + "    file_status   varchar(255) NOT NULL,\n"
                        + "    project_id    varchar(255)\n"
                        + ");";

                String virtualNodes = "CREATE TABLE IF NOT EXISTS duplication (\n"
                        + "    group_id      varchar(255) NOT NULL,\n"
                        + "    node_id       varchar(255) NOT NULL,"
                        + "    PRIMARY KEY (group_id, node_id)"                        
                        + ");";
                
                String relationships = "CREATE TABLE IF NOT EXISTS relationships (\n"
                        + "    source    varchar(255) NOT NULL,\n"  // FK (artefact)
                        + "    target    varchar(255) NOT NULL,\n"  // FK (artefact)
                        + "    type  varchar (255) NOT NULL"                      
                        + ");";

                String errors = "CREATE TABLE IF NOT EXISTS detected_errors (\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    type          varchar(255) NOT NULL,\n"
                        + "    cause         TEXT"                      
                        + ");";

                String ignored_files = "CREATE TABLE IF NOT EXISTS ignored_files(\n"
                        + "    id            varchar(255) PRIMARY KEY,\n"
                        + "    cause         TEXT"                      
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
                stmt.execute(errors);

                stmt = conn.createStatement();
                stmt.execute(ignored_files);

                stmt = conn.createStatement();
                stmt.execute(stats);                
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
		PreparedStatement allArtefactsStm = connection.prepareStatement("SELECT id, type, category, name, project_id, file_status FROM artefacts");
		allArtefactsStm.execute();
		ResultSet rs = allArtefactsStm.getResultSet();
		while (rs.next()) {
			String id = rs.getString(1);
			String type = rs.getString(2);
			String category = rs.getString(3);
			String name = rs.getString(4);
			String projectId = rs.getString(5);
			String fileStatus = rs.getString(6);
			result.put(id, new Artefact(new Project(projectId), id, type, category, name, fileStatus));
		}
		allArtefactsStm.close();
		return result;
	}

	public Map<String, Error> getErrorsExplicit() throws SQLException {
		Map<String, Error> result = new HashMap<String, Error>();     
		PreparedStatement allErrorsStm = connection.prepareStatement("SELECT id, type, cause FROM detected_errors WHERE type <> 'internal'");
		allErrorsStm.execute();
		ResultSet rs = allErrorsStm.getResultSet();
		while (rs.next()) {
			String id = rs.getString(1);
			String type = rs.getString(2);
			String cause = rs.getString(3);
			result.put(id, new Error(id, type, cause));
		}
		allErrorsStm.close();
		return result;
	}
	
	public Set<String> getIgnoredFileIds() throws SQLException {
		Set<String> result = new HashSet<String>();     
		PreparedStatement stm = connection.prepareStatement("SELECT id FROM ignored_files");
		stm.execute();
		ResultSet rs = stm.getResultSet();
		while (rs.next()) {
			String id = rs.getString(1);
			result.add(id);
		}
		stm.close();
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
	
	public Artefact getArtefactById(String nodeId) {
		Artefact artefact = allArtefacts.get(nodeId);
		Preconditions.checkState(artefact != null);
		return artefact;
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
	
	public void getRelationshipsFromSQL(String query, @Nonnull RelationshipConsumer consumer) {
		try {
			PreparedStatement stm = connection.prepareStatement(query);
			//"SELECT source, target, type FROM relationships WHERE type in (" + types + ")");
			stm.execute();
			ResultSet rs = stm.getResultSet();
	        //stm.getMetaData().getColumnName()
	       	        
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
			//filter(a -> a.getId().toLowerCase().contains(value.toLowerCase())).
			map(a -> a.getProject()).
			filter(p -> p.getId().toLowerCase().contains(value.toLowerCase())).
			distinct().
			collect(Collectors.toList());
	}


	public MegamodelStats getStats() {
		try {
			PreparedStatement artefactCount = connection.prepareStatement("select type, count(*) from artefacts group by type");
			ResultSet rs = artefactCount.executeQuery();
			MegamodelStats stats = new MegamodelStats();
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
	public void addArtefact(@Nonnull Project project, @Nonnull String id, @Nonnull String type, @Nonnull String category, @Nonnull String name, @Nonnull String fileStatus) {
		try {
			if (allArtefacts.containsKey(id)) {				
				return;
			}
						
			// We can insert
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO artefacts(id, type, category, name, project_id, file_status) VALUES (?, ?, ?, ?, ?, ?)");
			preparedStatement.setString(1, id);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, category);
			preparedStatement.setString(4, name);
			preparedStatement.setString(5, project.getId());
			preparedStatement.setString(6, fileStatus);
			preparedStatement.executeUpdate();
			preparedStatement.close();			
			
			allArtefacts.put(id, new Artefact(project, id, type, category, name, fileStatus));
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}

	public void addDuplicate(String groupId, String nodeId) {
		Preconditions.checkState(allArtefacts.containsKey(nodeId));

		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO duplication(group_id, node_id) VALUES (?, ?)");
			preparedStatement.setString(1, groupId);
			preparedStatement.setString(2, nodeId);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
	}	

	public DuplicationRelationships getDuplicates() {
		try {
			DuplicationRelationships dup = new DuplicationRelationships();
			PreparedStatement query = connection.prepareStatement("SELECT group_id, node_id FROM duplication");
			ResultSet rs = query.executeQuery();
			while (rs.next()) {
				String groupId = rs.getString(1);
				String nodeId = rs.getString(2);
				dup.addToGroup(groupId, nodeId);
			}
			return dup;
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

	private void addError(Error e) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO detected_errors(id, type, cause) VALUES (?, ?, ?)");
			preparedStatement.setString(1, e.getId());
			preparedStatement.setString(2, e.getType());
			preparedStatement.setString(3, e.getCause());
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);		
		}
	}
	
	private void addIgnored(Ignored i) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ignored_files(id, cause) VALUES (?, ?)");
			preparedStatement.setString(1, i.getId());
			preparedStatement.setString(2, i.getCause());
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);		
		}
	}

	public void dump(@Nonnull RelationshipsGraph graph, RecoveryStats.Composite composite, List<? extends Error> allErrors, List<Ignored> allIgnored) {
		for (Project p : graph.getProjects()) {
			addProject(p.getId(), p.getURL());
		}
		
		for (Node node : graph.getNodes()) {
			if (node instanceof ArtefactNode) {
				Artefact artefact = ((ArtefactNode) node).getArtefact();
				addArtefact(artefact.getProject(), artefact.getId(), artefact.getType(), artefact.getCategory(), artefact.getName(), artefact.getFileStatus());
			} else {
				throw new UnsupportedOperationException();
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
		
		for (Error error : allErrors) {
			addError(error);
		}
		
		for (Ignored ignored : allIgnored) {
			addIgnored(ignored);
		}
	}

	@FunctionalInterface
	public static interface RelationshipConsumer {
		public void accept(String source, String target, Relationship type);
	}

}