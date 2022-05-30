package mar.spark.indexer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.storage.StorageLevel;

import mar.indexer.AbstractIndexer;
//import mar.neural.search.core.Embeddings;
//import mar.neural.search.core.EmbeddingsSerializer;
//import mar.neural.search.core.GetEmbeddings;
import mar.paths.PairInformation;
import mar.paths.PartitionedPathMap;
import mar.paths.PathMapSerializer;
import scala.Tuple2;

public class SparkIndexer extends AbstractIndexer implements Closeable {

	private static Logger log = org.apache.log4j.LogManager.getLogger(SparkIndexer.class);

	// TODO: Compute this correctly
	private static Set<String> stoppaths = new HashSet<String>();

	private IndexMode mode;

	@Nonnull
	private final String modelType;
	
	public SparkIndexer(@Nonnull String modelType, @Nonnull IndexMode mode) throws IOException {
		this.mode = mode;	
		this.modelType = modelType;
	}
	
	public void configure(@Nonnull JavaRDD<ModelOrigin> files) {
		// Spark is a bit dumb because a reference to a field means trying to serialize SparkIndexer, so making this local to avoid serialization issues 
		final String modelType = this.modelType;
		
		JavaRDD<LoadedModel> models = files.map(f -> toResource(f));
		JavaRDD<IModelPaths> paths 	= models.map(m -> toPathMap(m));	
		paths = paths.filter(p -> !(p instanceof IError)); // TODO: Report the errors some how (in another table in HBase?)
		paths = paths.persist(StorageLevel.MEMORY_AND_DISK());
		
		final long number_models = paths.count();
		
		//stop paths only if the mode is full
		
		if (mode == IndexMode.FULL) {
			JavaRDD<Tuple2<IModelPaths, Integer>> counts = paths.map(p -> toModelCount(p));
			JavaPairRDD<CompositeKey, Value> kv = counts.flatMapToPair(p -> toKeyValue(p).iterator());
			JavaPairRDD<CompositeKey, Iterable<Value>> byKey = kv.groupByKey();
			//log.info("Storing with partitions = " + byKey.getNumPartitions());
			
			
			byKey.foreachPartition(iterator -> {
				try(Connection connection = getConnection()) {
					log.info("Storing...");
					String tableName = TableNameUtils.getStopPaths(modelType);
					BufferedMutator mutator = connection.getBufferedMutator(TableName.valueOf(tableName));

					iterator.forEachRemaining(tuple -> {
						try {
							storeStopPaths(tuple._1, tuple._2, connection, mutator, number_models);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
					
					mutator.flush();
				}
			});
			
			try(Connection connection = getConnection()){
				updateMeta(modelType, connection);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}	
		}
	
		try {
			try(Connection connection = getConnection()) {				
				stoppaths = getStopPaths(modelType, connection);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		JavaRDD<Tuple2<IModelPaths, Integer>> counts2 = paths.map(p -> toModelCountStopPaths(p));
		
		//put this here in order to force the persist transformation
		counts2.foreachPartition(iterator -> {
			try(Connection connection = getConnection()) {					
				iterator.forEachRemaining(c -> {
					try {
						storeInDocsInfo(c, modelType, connection);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			}
		});
		
		JavaPairRDD<CompositeKey, Value> kv2 = counts2.flatMapToPair(p -> toKeyValue(p).iterator());
		JavaPairRDD<CompositeKey, Iterable<Value>> byKey2 = kv2.groupByKey();
		
		byKey2.foreachPartition(iterator -> {
			try(Connection connection = getConnection()) {
				log.info("Storing...");
				String tableName = TableNameUtils.getInvertedIndex(modelType);
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
		
//		models.foreachPartition(iterator -> {
//			try(Connection connection = getConnection()) {
//				String tableName = "docs_info_" + "ecore";
//				BufferedMutator mutator = connection.getBufferedMutator(TableName.valueOf(tableName));
//				
//				iterator.forEachRemaining(m -> {
//					try {
//						storeEmbeddings(m, mutator);
//					}catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//				});
//				
//				mutator.flush();
//			}
//		});
		

		
				
		
		
		try {
			try(Connection connection = getConnection()) {				
				long totalIndexedTokens = counts2.map(t -> t._2).reduce(Integer::sum);
				long totalDocuments = number_models; // TODO: Pass total documents as parameter
				log.info("Total tokens: " + totalIndexedTokens);
				log.info("Total documents: " + totalDocuments);
				storeGlobal(totalIndexedTokens, totalDocuments, modelType, connection);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Connection getConnection() throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "zoo");
		Connection connection = ConnectionFactory.createConnection(conf);
		return connection;
	}
	
	public static int min_df = 1;
//
//	private static void storeInHbase(CompositeKey key, Iterable<Value> values) throws IOException {
//		Configuration conf = HBaseConfiguration.create();
//		conf.set("hbase.zookeeper.quorum", "zoo");
//		Connection connection = ConnectionFactory.createConnection(conf);
//		storeInHbaseWithConnection(key, values, connection);
//		connection.close();
//	}
	
	private static void storeInHbaseWithConnection(CompositeKey key, Iterable<Value> values, Connection connection, BufferedMutator mutator) throws IOException {   		
	
 		int cont = 0;

 		HashMap<String,PairInformation> hm = new HashMap<String,PairInformation>();
 			
 		byte[] DOC = "doc".getBytes();
 		byte [] ROW = key.getPart1().getBytes();
 		byte [] COLUMN = key.getPart2().getBytes();
 		 		
 		
 		
 		//calculate the hashmap
		for (Value v : values) {
			int ntokens_doc = v.getNtokens();
			int nocurrences = v.getNocurrences();
			String doc = v.getDocId();
			PairInformation li = new PairInformation(nocurrences, ntokens_doc);
			hm.put(doc,li);
			cont = cont + 1;
		}
		
 		
		
 		
		int min_df = 1;
		if (cont >= min_df) {
			//log.info("Storing " + key.getPart1() + "/" + key.getPart2() + " => " + hm.size());

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			byte[] array = PathMapSerializer.serialize(hm);
			
			Put put = new Put(ROW);
		
			put.addColumn(DOC, COLUMN, array);
		
			
			mutator.mutate(put);			
		}		

	}
	
	
	private static void storeStopPaths(CompositeKey key, Iterable<Value> values, Connection connection, BufferedMutator mutator, Long total) throws IOException {   		

 		byte [] ROW = (key.getPart1()+key.getPart2()).getBytes();

 		//calculate the hashmap
 		int cont = 0;
		for (Value v : values) {
			cont = cont + 1;
		}

		if (cont >= (0.7 * total)) {
			//log.info("Storing " + key.getPart1() + "/" + key.getPart2() + " => " + hm.size());
			Put put = new Put(ROW);
			put.addColumn("sp".getBytes(), "sp".getBytes(), "sp".getBytes());
			mutator.mutate(put);			
		}		

	}

	private static void storeGlobal(long totalIndexedTokens, long totalDocuments, String modelType, Connection connection) throws IOException {
		TableName tableName = TableName.valueOf(TableNameUtils.getGlobalStats(modelType));
		Table table = connection.getTable(tableName);
		
		Get get = new Get("stats".getBytes());
		Result r = table.get(get);
		
		long ntokens = 0;
		long ndocs = 0;
		
		if (!r.isEmpty()) {
			ntokens = Bytes.toLong(r.getValue("stats".getBytes(), "nTokens".getBytes()));
			ndocs = Bytes.toLong(r.getValue("stats".getBytes(), "ndocs".getBytes()));
		}

		Put put = new Put("stats".getBytes());
		put.addColumn("stats".getBytes(), "nTokens".getBytes(), Bytes.toBytes(totalIndexedTokens + ntokens));
		put.addColumn("stats".getBytes(), "ndocs".getBytes(), Bytes.toBytes(totalDocuments + ndocs));

		table.put(put);
	}
	
	private static Set<String> getStopPaths(String modelType, Connection connection) throws IOException {
		TableName tableName = TableName.valueOf(TableNameUtils.getStopPaths(modelType));
		Table table = connection.getTable(tableName);
		
		Set<String> sp = new HashSet<String>();
		Scan scan = new Scan();
		
		ResultScanner scanner = table.getScanner(scan);
	     for (Result result = scanner.next(); result != null; result = scanner.next()) {
	    	 sp.add(Bytes.toString(result.getRow()));
	     }
	     
	     return sp;
	}
	
	private static void updateMeta(String modelType, Connection connection) throws IOException {
		TableName tableName = TableName.valueOf(TableNameUtils.getMetaTable(modelType));
		Table table = connection.getTable(tableName);
		
		Put put = new Put(modelType.getBytes());
		put.addColumn("tables".getBytes(),"inverted_index".getBytes(), TableNameUtils.getInvertedIndex(modelType).getBytes());
		put.addColumn("tables".getBytes(),"global_st".getBytes(), TableNameUtils.getGlobalStats(modelType).getBytes());
		put.addColumn("tables".getBytes(),"docs_info".getBytes(), TableNameUtils.getDocsInfo(modelType).getBytes());
		
		table.put(put);
		table.close();
	}

	
	private static void storeInDocsInfo(Tuple2<IModelPaths, Integer> c, String modelType, Connection connection) throws IOException {
		ModelPaths p = (ModelPaths) c._1;
		int totalTokens = c._2;

		putInDocsInfo(TableNameUtils.getDocsInfo(modelType), totalTokens, p, connection);
		putInDocsInfo(TableNameUtils.getDocsInfo(), totalTokens, p, connection);
	}
	
	private static void putInDocsInfo(String tableName, int totalTokens, ModelPaths p, Connection connection) throws IOException {
		String metadata = p.origin.getMetadata();
		byte[] modelId = p.origin.getModelId().getBytes();
		
		Put put = new Put(modelId);
		put.addColumn("information".getBytes(),"nTokens".getBytes(), Bytes.toBytes(totalTokens));
		put.addColumn("information".getBytes(), "metadataDocument".getBytes(), 
				metadata == null ? "".getBytes() : Bytes.toBytes(metadata));
		
//		GetEmbeddings ge = new GetEmbeddings();
//		Embeddings lf = ge.getEmbeddings(p.resource, MetaFilter.getEcoreFilter());
//		byte[] lfsel = EmbeddingsSerializer.serialize(lf);
//		put.addColumn("information".getBytes(),"embeddings".getBytes(), lfsel);
		

		Table table = connection.getTable(TableName.valueOf(tableName));
		table.put(put);
		
		table.close();		
	}
	
//	private static void storeEmbeddings(LoadedModel lm, BufferedMutator mutator) throws IOException {
//		if (!(lm instanceof ErrorModel)) {
//			Put put = new Put(lm.getOrigin().getModelId().getBytes());
//			GetEmbeddings ge = new GetEmbeddings();
//			Embeddings lf = ge.getEmbeddings(lm.resource, MetaFilter.getEcoreFilter());
//			byte[] lfsel = EmbeddingsSerializer.serialize(lf);
//			put.addColumn("information".getBytes(),"embeddings".getBytes(), lfsel);
//			mutator.mutate(put);
//		}
//	}
//	
	
	
	private static Tuple2<IModelPaths, Integer> toModelCountStopPaths(@Nonnull IModelPaths paths) {		
		PartitionedPathMap mapTokens = ((ModelPaths) paths).pathMap;
		ModelOrigin origin = ((ModelPaths) paths).origin;
		//String docId = origin.getModelId();
		
		PartitionedPathMap ppm_new = new PartitionedPathMap();
		IModelPaths paths_new = new ModelPaths(ppm_new, origin);
		


		int totalTokens = 0;
		Iterator<Entry<String, Map<String, Integer>>> it = mapTokens.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, Integer>> pair = it.next();
			Map<String, Integer> second_part = pair.getValue();
			Iterator<Entry<String, Integer>> it2 = second_part.entrySet().iterator();
			String commom = pair.getKey();
//		        boolean oneContainsName = commom.contains(",name,");
			while (it2.hasNext()) {
				Entry<String, Integer> pair2 = it2.next();
				String full = commom + pair2.getKey();

				int ntokens = pair2.getValue();
				// ignore stop paths
				if (stoppaths.contains(full))
					continue;
				
				totalTokens = totalTokens + ntokens;
				if (ppm_new.containsKey(pair.getKey())) {
					ppm_new.get(pair.getKey()).put(pair2.getKey(), pair2.getValue());
				} else {
					HashMap<String, Integer> nhm = new HashMap<String, Integer>();
					nhm.put(pair2.getKey(), pair2.getValue());
					ppm_new.put(pair.getKey(),nhm);
				}
				
			}
		}

		return Tuple2.apply(paths_new, totalTokens);		
	}
	

	@Override
	public void close() throws IOException {
		//connection.close();
	}
}
