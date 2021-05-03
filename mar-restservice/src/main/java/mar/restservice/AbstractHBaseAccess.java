package mar.restservice;

import java.io.Closeable;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractHBaseAccess implements Closeable {

	protected Configuration conf = HBaseConfiguration.create();
	private Connection connection = null;	

	public AbstractHBaseAccess() {
		String zooIp = getZooHostOrDefault();		
		//conf.set("hbase.zookeeper.quorum", zooIp);
		conf.set("hbase.zookeeper.quorum", zooIp);
		
		conf.set("hbase.rpc.timeout", "1800000");
		conf.set("hbase.client.scanner.timeout.period", "1800000");
	}
	
	
	@NonNull
	private String getZooHostOrDefault() {
		String zooIp = System.getenv("ZOO_HOST");
		if (zooIp == null) {
			zooIp = "zoo";
		}
		return zooIp;
	}

	@NonNull
	protected Connection getConnection() throws IOException {
		if (connection == null) {
			connection = ConnectionFactory.createConnection(conf);
		}
		return connection;
	}
	
	@Override
	public void close() throws IOException {
		if (connection != null)
			connection.close();
	}
	
}
