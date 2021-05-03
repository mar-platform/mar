package mar.spark.indexer.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import mar.spark.indexer.TableNameUtils;

public class IndexReader implements AutoCloseable {

	@Nonnull
	private String modelType;
	private Map<String, String> idToDocument;
	private int countPaths;
	private int countDocs;

	public IndexReader(String modelType) throws IOException {
		this.modelType = modelType;
		doRead();
	}
	
	private void doRead() throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "zoo");
		Connection connection = ConnectionFactory.createConnection(conf);

		countDocs = countTable(connection.getTable(TableName.valueOf(TableNameUtils.getDocsInfo(modelType))));
		countPaths = countTable(connection.getTable(TableName.valueOf(TableNameUtils.getInvertedIndex(modelType))));
		idToDocument = new HashMap<>();
		
		// Check ids
		Scan scan = new Scan();
		ResultScanner scanner = connection.getTable(TableName.valueOf(TableNameUtils.getDocsInfo(modelType)))
				.getScanner(scan);
		try {
			for (Result result = scanner.next(); (result != null); result = scanner.next()) {
				String id = Bytes.toString(result.getRow());
				final byte[] DOC = "information".getBytes();
				// final byte[] URL = "origin_url".getBytes();
				final byte[] METADATA = "metadataDocument".getBytes();
				
				// System.out.println(id);
							
				byte[] v = result.getValue(DOC, METADATA);
				if (v != null) {
					String metadata = Bytes.toString(v);
					idToDocument.put(id, metadata);
				} else {
					idToDocument.put(id, "");
				}

			}
		} finally {
			scanner.close();
		}		
	}

	public int getCountDocs() {
		return countDocs;
	}
	
	public int getCountPaths() {
		return countPaths;
	}
	
	/**
	 * @return A map <id, metadata> where value will be empty if no metadata is available.
	 */
	public Map<String, String> getDocuments() {
		return idToDocument;
	}
	
	private int countTable(Table table) throws IOException {
		int count = 0;
		Scan scan = new Scan();
		ResultScanner scanner = table.getScanner(scan);
		try {
			for (Result result = scanner.next(); (result != null); result = scanner.next()) {
				count++;
			}
		} finally {
			scanner.close();
		}
		return count;
	}
	
	@Override
	public void close() throws Exception {
		// Nothing	
	}
}
