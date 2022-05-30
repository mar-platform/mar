package mar.sqlite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;

import mar.paths.PartitionedPathMap;

public class SqlitePathRetriever {

	private SqliteIndexDatabase db;

	public SqlitePathRetriever(SqliteIndexDatabase db) {
		this.db = db;
	}
	
	public void retrieve(Resource r, PartitionedPathMap paths, SqlitePathConsumer consumer) throws IOException {
		List<List<String>> subqueries = new ArrayList<>(paths.size()); 
		List<String> newList = new ArrayList<>();
		
		paths.forEach((head, rest_list) -> {
			rest_list.forEach((rest, count) -> {
				String p = head + rest;
				p = p.replace("'", "''");
				String s = "select path, doc_id, n_docs_t, n_occurences, n_tokens from mar_index where path = '" + p + "'";
				newList.add(s);
				if (newList.size() == 500) {
					subqueries.add(new ArrayList<>(newList));
					newList.clear();
				}							
			});
			
		});
		
		if (! newList.isEmpty())
			subqueries.add(newList);
				
		for (List<String> list : subqueries) {
			String full = list.stream().collect(Collectors.joining("\nunion all\n"));
			try {
				db.getModels(full, consumer);
			} catch (SQLException e) {
				throw new IOException(e);
			}		
		}		
	}
	

	
}
