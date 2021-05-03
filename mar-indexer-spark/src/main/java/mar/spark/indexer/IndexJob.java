package mar.spark.indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.spark.indexer.validation.IndexReader;
import mar.validation.AnalyserMain;
import mar.validation.AnalysisDB;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "spark-indexer", mixinStandardHelpOptions = true, description = "Index models in HBase with Spark")
public class IndexJob implements Callable<Integer> {

	@Parameters(index = "0", description = "The configuration file.")
	private File configurationFile;
	
	@Option(required = true, names = { "-t", "--type" }, description = "The model type: ecore, genmymodel-bpmn, uml")
	private String modelType;

	@Option(required = false, names = { "-r", "--repository" }, description = "A specific repository in the configuration file")
	private String repository = null;

	@Option(required = false, names = { "-m", "--mode" }, description = "Execution mode. Full or incremental", converter=IndexModeConverter.class)
	private IndexMode mode = IndexMode.FULL;

	@Option(required = false, names = { "-s", "--split" }, description = "Split indexing")
	private Integer indexBySpliting;
	
	public static void main(String[] args) throws IOException, InvalidJobSpecification {
		int exitCode = new CommandLine(new IndexJob()).execute(args);
		System.exit(exitCode);
	}
	
	@Override
	public Integer call() throws Exception {	
	    IndexJobConfigurationData data = CmdOptions.readConfiguration(configurationFile);
	    List<SingleIndexJob> jobs;
	    if (repository == null) { 
	    	jobs = data.getRepositoriesOfType(modelType);
	    } else {
	    	SingleIndexJob repoConf = data.getRepo(repository);
	    	if (repoConf == null)
	    		throw new IllegalArgumentException("Repository " + repository + " not found.");
	    	jobs = Collections.singletonList(repoConf);
	    }
	    
	    if (jobs.isEmpty()) 
	    	throw new IllegalArgumentException("Can't find job for modelType = " + modelType + (repository == null ? "" : "and repository = " + repository));
	    		
		initModels();
    	    	    
	    SparkSession spark = createSession();    

	    final Map<String, String> existingDocuments;
	    if (mode == IndexMode.FULL) {
	    	// remove all tables
    		System.out.println("Creating tables...");
        	createTables(modelType, false); // TODO: Add a program argument
        	createMetaTable(modelType);
        	System.out.println("Created tables");
        	
        	existingDocuments = Collections.emptyMap();
    	} else if (mode == IndexMode.INCREMENTAL) {
	    	try(IndexReader reader = new IndexReader(modelType)) {
	    		existingDocuments = reader.getDocuments();
	    	}
	    } else {
	    	throw new IllegalStateException();
	    }
	    
	    JavaRDD<ModelOrigin> files = null;
	    for (SingleIndexJob repoConf : jobs) {
			String rootFolder = repoConf.getRootFolder();
		    Dataset<Row> validModels;
		    if (repoConf.usesFileList()) {
		    	validModels = spark.read().format("csv")
			    		  .option("sep", ",")
			    		  .option("inferSchema", "true")
			    		  .option("header", "false")
			    		  .load(repoConf.getFileList());
		    } else {	    
			    validModels = spark.read().format("jdbc")
			    	.option("driver", "org.sqlite.JDBC")
			    	.option("url", AnalysisDB.getConnectionString(repoConf.getModelDbFile()))
			    	.option("query", AnalysisDB.getValidModelsQuery())
			    	.load(); 
		    }
		    
		    JavaRDD<ModelOrigin> origins = validModels.toJavaRDD().map(r -> {
		    	String fileName = r.getString(0);
		    	String id       = fileName;
		    	String fullPath = rootFolder + File.separator + fileName;
		    	String metadata = null;
		    	if (r.size() > 1) {	    		
		    		id = r.getString(1);
		    		metadata = r.getString(2);
		    	}
		    	return new ModelOrigin(fullPath, id, repoConf, metadata);
		    });
		    
		    if (files == null) {
		    	files = origins;
		    } else {
		    	files = files.union(origins);
		    }
	    }
	    
	    // Filter out already read files
	    if (! existingDocuments.isEmpty()) {
	    	files = files.filter(o -> ! existingDocuments.containsKey(o.getModelId()));
	    }
	    
	    int partitions = 6;	    
	    if (indexBySpliting != null && indexBySpliting > 1) {
	    	List<List<ModelOrigin>> origins = new ArrayList<>();
	    	for(int i = 0; i < indexBySpliting; i++) {
	    		origins.add(new ArrayList<>());
	    	}
	    	int i = 0;
	    	Iterator<ModelOrigin> it = files.toLocalIterator();
	    	while (it.hasNext()) {
	    		origins.get(i % indexBySpliting).add(it.next());
	    		i++;
	    	}
	    	
	    	spark.stop();
	    	
	    	for (List<ModelOrigin> list : origins) {
	    		spark = createSession();
	    		JavaSparkContext ctx = new JavaSparkContext(spark.sparkContext());
	    		JavaRDD<ModelOrigin> partList = ctx.parallelize(list).repartition(partitions);
	    		try(SparkIndexer indexer = new SparkIndexer(modelType, mode)) {
					System.out.println("Start chunk!");
					indexer.configure(partList);
			    	System.out.println("Finish chunk!");
			    }	
	    		spark.stop();
	    		ctx.close();
			}
	    } else {
		    files = files.repartition(partitions);
		    try(SparkIndexer indexer = new SparkIndexer(modelType, mode)) {
				System.out.println("Start!");
				indexer.configure(files);
		    	System.out.println("Finish!");
		    }
	    }
		
		
//		if (mode.equals("full")) {
//			try(SparkIndexer indexer = new SparkIndexer("ecore", pathComputation)) {
//				System.out.println("Start!");
//				indexer.configure(files);
//		    	System.out.println("Finish!");
//		    }
//		} else {
//			try(SparkIndexerIncremental indexer = new SparkIndexerIncremental("ecore", pathComputation)) {
//				System.out.println("Start!");
//				indexer.configure(files);
//		    	System.out.println("Finish!");
//		    }
//		}
	    
	    spark.stop();
	    return 0;
	}

	private SparkSession createSession() {
		SparkSession spark = SparkSession.builder()
	    		.appName("MarIndexer")
	    		.config("spark.master", "local[6]")
	    		.config("spark.sql.shuffle.partitions","6")
	    		.getOrCreate();
		return spark;
	}

	/* pp */ static void initModels() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("ecore", new EcoreResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("xmi", new XMIResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("uml", new UMLResourceFactoryImpl());
    	EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);	
	}

	private static void createTables(@Nonnull String modelType, boolean recreateGlobalDocsInfo) throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "zoo");
		Connection connection = ConnectionFactory.createConnection(conf);
		Admin admin = connection.getAdmin();
		
		createDocsInfo(TableNameUtils.getDocsInfo(modelType), admin);
		TableName globalDocsInfo = TableName.valueOf(TableNameUtils.getDocsInfo());
		if (recreateGlobalDocsInfo || ! admin.tableExists(globalDocsInfo))
		{
			createDocsInfo(TableNameUtils.getDocsInfo(), admin);
		}
		
		// global_st
		{
			TableName tableName = TableName.valueOf(TableNameUtils.getGlobalStats(modelType));
			if (admin.tableExists(tableName)) {
				admin.disableTable(tableName);
				admin.deleteTable(tableName);
			}
			
			HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
			tableDescriptor.addFamily(new HColumnDescriptor("stats").setMaxVersions(1));
			admin.createTable(tableDescriptor);
			/*
			TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);
			builder.setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("stats".getBytes()).setMaxVersions(1).build());
			admin.createTable(builder.build());
			*/
		}

		// inverted_index
		{
			TableName tableName = TableName.valueOf(TableNameUtils.getInvertedIndex(modelType));
			if (admin.tableExists(tableName)) {
				admin.disableTable(tableName);
				admin.deleteTable(tableName);
			}
			
			HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
			tableDescriptor.addFamily(new HColumnDescriptor("doc").setMaxVersions(5));
			admin.createTable(tableDescriptor);
			/*
			TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);
			builder.setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("doc".getBytes())
					.setMaxVersions(1)
					// Try
					// #,compression='GZ',bloom_filter_type='ROWCOL',block_cache_enabled=True
					.build());
			admin.createTable(builder.build());
			*/
		}
		
		{
			TableName tableName = TableName.valueOf("inverted_index_" + modelType + "_2");
			if (admin.tableExists(tableName)) {
				admin.disableTable(tableName);
				admin.deleteTable(tableName);
			}
			
			HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
			tableDescriptor.addFamily(new HColumnDescriptor("doc").setMaxVersions(1));
			admin.createTable(tableDescriptor);
			/*
			TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);
			builder.setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("doc".getBytes())
					.setMaxVersions(1)
					// Try
					// #,compression='GZ',bloom_filter_type='ROWCOL',block_cache_enabled=True
					.build());
			admin.createTable(builder.build());
			*/
		}
		
		{
			TableName tableName = TableName.valueOf(TableNameUtils.getStopPaths(modelType));
			if (admin.tableExists(tableName)) {
				admin.disableTable(tableName);
				admin.deleteTable(tableName);
			}
			
			HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
			tableDescriptor.addFamily(new HColumnDescriptor("sp").setMaxVersions(1));
			admin.createTable(tableDescriptor);
			/*
			TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);
			builder.setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder("doc".getBytes())
					.setMaxVersions(1)
					// Try
					// #,compression='GZ',bloom_filter_type='ROWCOL',block_cache_enabled=True
					.build());
			admin.createTable(builder.build());
			*/
		}
			
	}

	private static void createDocsInfo(String tableName_, Admin admin) throws IOException {
		TableName tableName = TableName.valueOf(tableName_);
		if (admin.tableExists(tableName)) {
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
		}
		
		HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
		tableDescriptor.addFamily(new HColumnDescriptor("information").setMaxVersions(1));
		tableDescriptor.addFamily(new HColumnDescriptor("content").setMaxVersions(1));
		tableDescriptor.addFamily(new HColumnDescriptor("flags").setMaxVersions(1));
		admin.createTable(tableDescriptor);		
	}
	
	private static void createMetaTable(@Nonnull String modelType) throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "zoo");
		Connection connection = ConnectionFactory.createConnection(conf);
		Admin admin = connection.getAdmin();

		//this table will contain information about tables
		//row -> type of model: ecore, uml, bpmn, etc
		//cf = tables
		//qualifier belongs to {docs_info, inverted_index, global_st}
		TableName tableName = TableName.valueOf(TableNameUtils.getMetaTable(modelType));
		if (! admin.tableExists(tableName)) {
			HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
			tableDescriptor.addFamily(new HColumnDescriptor("tables").setMaxVersions(1));
			admin.createTable(tableDescriptor);
		}
	}

	private static class IndexModeConverter implements ITypeConverter<IndexMode> {

		@Override
		public IndexMode convert(String value) throws Exception {
			if ("incremental".equals(value))
				return IndexMode.INCREMENTAL;
			if ("full".equals(value))
				return IndexMode.FULL;
			return null;
		}
		
	}
}
