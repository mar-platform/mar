package mar.restservice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Records information who is using the system.
 */
public class HBaseLog extends HBaseModelAccessor {

	private DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss::SS");

	public void addLog(@Nonnull String ip, LocalDateTime date, String url)  {
		String key = ip + "::" + dateformat.format(date);
		try {
			Table log = getConnection().getTable(TableName.valueOf("access_log"));			
			putLog(url, log, key);
		} catch (IOException e) {
			try {
				Admin admin = getConnection().getAdmin();
	
				// Instantiating table descriptor class
				HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf("access_log"));
				// Adding column families to table descriptor
				HColumnDescriptor descriptor = new HColumnDescriptor("data");
				tableDescriptor.addFamily(descriptor);
	
				// Execute the table through admin
				admin.createTable(tableDescriptor);

				// Try again now
				Table log = getConnection().getTable(TableName.valueOf("access_log"));
				putLog(url, log, key);
			} catch (IOException e2) {
				e2.printStackTrace();
				return;
			}

		}
	}

	private void putLog(String url, Table log, String key) throws IOException {
		Put put = new Put(Bytes.toBytes(key));
		put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("url"), Bytes.toBytes(url));
		log.put(put);
	}
	
}
