package mar.sqlite;

import java.sql.SQLException;
import java.util.List;

import mar.indexer.AbstractIndexer;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.spark.indexer.CompositeKey;
import mar.spark.indexer.IError;
import mar.spark.indexer.IModelPaths;
import mar.spark.indexer.LoadedModel;
import mar.spark.indexer.ModelOrigin;
import mar.spark.indexer.Value;
import scala.Tuple2;

public class SqliteIndexer extends AbstractIndexer {

	public void index(SqliteIndexDatabase db, List<ModelOrigin> models) {
		long totalTokens    = 0;
		long totalDocuments = 0;
		
		for (ModelOrigin origin : models) {
			try {
				System.out.println("Processing: " + origin.getModelId());
				LoadedModel m = toResource(origin);
				IModelPaths p = toPathMap(m);
				if (p instanceof IError)
					continue;

				Tuple2<IModelPaths, Integer> count = toModelCount(p);
				List<Tuple2<CompositeKey, Value>> values = toKeyValue(count);
				
				for (Tuple2<CompositeKey,Value> t : values) {
					CompositeKey key = t._1;
					Value value = t._2;
					
					//System.out.println(key.getPart1() + key.getPart2() + " = " + value.getNocurrences() + " " + value.getNtokens());
					db.add(key.getPart1() + key.getPart2(), value.getDocId(), value.getNocurrences(), value.getNtokens());
				}
				
				totalTokens    += count._2;
				totalDocuments += 1;
				
				m.resource.unload();
			} catch (InvalidJobSpecification e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			db.updateDocCounts();
			db.addGlobalStats(totalTokens, totalDocuments);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
