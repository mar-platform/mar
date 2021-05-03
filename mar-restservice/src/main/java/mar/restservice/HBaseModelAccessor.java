package mar.restservice;

import java.io.IOException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

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
	
	public String getModel(String id) throws IOException {
		return getModel(getDocsInfo(), id);
	}
	
	private String getModel(Table docs_info, String id) throws IOException {
		Get get = new Get(id.getBytes());
		get.addColumn("content".getBytes(), "rawcontent".getBytes());

		Result r = docs_info.get(get);

		if (r.isEmpty())
			return "Not found";

		byte[] content = r.value();

		return Bytes.toString(content);
	}
}
