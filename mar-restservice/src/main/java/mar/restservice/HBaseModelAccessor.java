package mar.restservice;

import java.io.IOException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Table;

import mar.spark.indexer.TableNameUtils;

/**
 * Access information about models, metadata, etc.
 * 
 * @author jesus
 *
 */
public class HBaseModelAccessor extends AbstractHBaseAccess {

	/**
	 * We have two types of docs_info tables. A global table and a per-type
	 * table.
	 */
	protected Table getDocsInfo() throws IOException {
		String tableName = TableNameUtils.getDocsInfo();
		return getConnection().getTable(TableName.valueOf(tableName));
	}

	protected Table getDocsInfo(String modelType) throws IOException {
		String tableName = TableNameUtils.getDocsInfo(modelType);
		return getConnection().getTable(TableName.valueOf(tableName));
	}

}
