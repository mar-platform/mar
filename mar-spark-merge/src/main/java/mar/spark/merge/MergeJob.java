package mar.spark.merge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.NewHadoopRDD;
import org.apache.spark.sql.SparkSession;

import mar.paths.PairInformation;
import mar.paths.PathMapSerializer;

import scala.Tuple2;
import scala.collection.immutable.Stream;

public class MergeJob {
	
	private static Logger log = org.apache.log4j.LogManager.getLogger(MergeJob.class);

public static void main(String[] args) throws IOException {
		
	
	
		SparkSession spark = SparkSession.builder()
	    		.appName("ExampleHBASE")
	    		.config("spark.master", "local[6]")
	    		.config("spark.sql.suffle.partitions","6")
	    		.config("spark.serializer", "org.apache.spark.serializer.KryoSerializer") 
	    		.getOrCreate();
		
		
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "zoo");
		conf.set(TableInputFormat.INPUT_TABLE, "inverted_index_ecore");
		conf.set("hbase.mapreduce.scan.maxversions", "5");
		
		NewHadoopRDD<ImmutableBytesWritable, Result> hdr = new NewHadoopRDD<ImmutableBytesWritable, Result>(spark.sparkContext(), 
				TableInputFormat.class, ImmutableBytesWritable.class, Result.class, conf);

		
		JavaRDD<Tuple2<ImmutableBytesWritable,Result>>  hdrJava = hdr.toJavaRDD().repartition(6);
		
		JavaPairRDD<Coordinates, byte[]> kv = hdrJava.flatMapToPair(tuple -> toKeyValue(tuple).iterator());
		JavaPairRDD<Coordinates, Iterable<byte[]>> byKey = kv.groupByKey();
		
		JavaRDD<Tuple2<Coordinates, byte[]>> reduced = byKey.map(tuple ->{
			HashMap<String,PairInformation> hm = new HashMap<String,PairInformation>();
			for (byte[] map: tuple._2) {
				Map<String,PairInformation> toAdd = PathMapSerializer.deserialize(map);
				
				for (Entry<String,PairInformation> entry: toAdd.entrySet()) {
					hm.put(entry.getKey(), entry.getValue());
				}
				
			}
			return Tuple2.apply(tuple._1, PathMapSerializer.serialize(hm));
		});
		
		
		reduced.foreachPartition(iterator -> {
			
			try(Connection connection = getConnection()){
				String tableName = "inverted_index_ecore_2";
				BufferedMutator mutator = connection.getBufferedMutator(TableName.valueOf(tableName));

				iterator.forEachRemaining(tuple -> {
					try {
						storeInHbaseWithConnection(tuple._1, tuple._2, connection, mutator);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				
				mutator.flush();
			}
			
		});
		
		try(Connection connection = getConnection()){
			updateMeta(connection);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
		
	}
	
	private static List<Tuple2<Coordinates, byte[]>> toKeyValue(@Nonnull Tuple2<ImmutableBytesWritable, Result> tuple) {
		
		byte[] DOC = "doc".getBytes();
		Result columns = tuple._2;
		ImmutableBytesWritable row = tuple._1;
		String row_s = Bytes.toString(row.get());
		
		//family,column,timestamp
		NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> maps = columns.getMap();
		
		NavigableMap<byte[], NavigableMap<Long, byte[]>> cols = maps.get(DOC);
		
		List<Tuple2<Coordinates, byte[]>> list_tup = new LinkedList<Tuple2<Coordinates,byte[]>>();
		
		for (Entry<byte[], NavigableMap<Long, byte[]>> col : cols.entrySet()) {
			String fam = "doc";
			String col_s = Bytes.toString(col.getKey());
			
//			if (col.getValue().size() > 1) {
//				log.info("Versions > 1 ");
//			}
			
			
			for (Entry<Long, byte[]> val : col.getValue().entrySet()) {
				list_tup.add(Tuple2.apply(new Coordinates(fam, col_s, row_s), val.getValue()));
			}
			
			
			
		}
		
		return list_tup;
		
	}
	
	
	private static Connection getConnection() throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "zoo");
		Connection connection = ConnectionFactory.createConnection(conf);
		return connection;
	}
	
	private static void storeInHbaseWithConnection(Coordinates key, byte[] values, Connection connection, BufferedMutator mutator) throws IOException {   		

 			
 		byte[] DOC = key.getFamily().getBytes();
 		byte [] ROW = key.getRow().getBytes();
 		byte [] COLUMN = key.getColumn().getBytes();
 		
 		Put p = new Put(ROW);
 		p.addColumn(DOC, COLUMN, values);
 		
			
		mutator.mutate(p);			
		}
	
	private static void updateMeta(Connection connection) throws IOException {
		TableName tableName = TableName.valueOf("meta_table");
		Table table = connection.getTable(tableName);
		Put put = new Put("ecore".getBytes());
		put.addColumn("tables".getBytes(),"inverted_index".getBytes(), "inverted_index_ecore_2".getBytes());
		table.put(put);
		table.close();
		
		//delete old table
		tableName = TableName.valueOf("inverted_index_ecore");
		Admin admin = connection.getAdmin();
		if (admin.tableExists(tableName)) {
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
		}
		
		
	}

	}




