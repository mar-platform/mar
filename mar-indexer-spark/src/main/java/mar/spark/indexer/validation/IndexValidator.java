package mar.spark.indexer.validation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

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

import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.spark.indexer.TableNameUtils;

/**
 * Validates the status of the database after the indexing process.
 * 
 * @author jesus
 */
public class IndexValidator {

	public static void main(String[] args) throws FileNotFoundException, IOException, InvalidJobSpecification {
		if (args.length < 2) {
			System.err.println("Usage: MarIndexValidator configuration-string repo-name");
			System.exit(1);
		}

		String configuration = args[0];
		String repoName = args[1];

		SingleIndexJob job = CmdOptions.readConfiguration(configuration, repoName);
		String type = job.getType();

		new IndexValidator().validate(job, type);
	}

	public void validate(SingleIndexJob job, String modelType) throws IOException {
		try(IndexReader reader = new IndexReader(modelType)) {
			int countDocs = reader.getCountDocs();
			int countPaths = reader.getCountPaths();
			Map<String, String> docs = reader.getDocuments();
		
			System.out.println("Total documents: " + countDocs);
			System.out.println("Total path-index: " + countPaths);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
