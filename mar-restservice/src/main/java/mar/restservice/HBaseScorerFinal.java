package mar.restservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.model2graph.PathComputation;
import mar.paths.PairInformation;
import mar.paths.PairSerializer;
import mar.paths.PathMapSerializer;
import mar.restservice.BM25ScoreCalculator.GlobalStats;

public class HBaseScorerFinal extends AbstractHBaseAccess implements IScorer {

	@NonNull
	private PathComputation pathComputation;
	
	private HashMap<String, Set<String>> stopwords = null;
	private String model;
	
	
	public HBaseScorerFinal(@NonNull PathComputation pathComputation, @NonNull String model) {
		this.pathComputation = pathComputation;	
		this.model = model;
		//change if you want to execute in eclipse

		stopwords = new HashMap<>();
		
		Set<String> stopwEcore = null;
		
		try {
			stopwEcore = initializeStopWords("ecore");
			//check if the sp are obtained
		} catch (IOException e) {
			System.out.println("Error when sptable is accessed");
			stopwEcore = new HashSet<>();
		}
		
//		stopwEcore.add("(-1,upperBound,EReference)");
//		stopwEcore.add("(-1,upperBound,EReference,containment,true)");
//		stopwEcore.add("(-1,upperBound,EReference,eContainingClass,EClass,abstract,false)");
//		stopwEcore.add("(-1,upperBound,EReference,eType,EClass,abstract,false)");
//		stopwEcore.add("(-1,upperBound,EReference,lowerBound,0)");
//		stopwEcore.add("(0,lowerBound,EAttribute)");
//		stopwEcore.add("(0,lowerBound,EAttribute,eContainingClass,EClass,abstract,false)");
//		stopwEcore.add("(0,lowerBound,EAttribute,eType,EDataType)");
//		stopwEcore.add("(0,lowerBound,EAttribute,upperBound,1)");
//		stopwEcore.add("(0,lowerBound,EReference)");
//		stopwEcore.add("(0,lowerBound,EReference,containment,true)");
//		stopwEcore.add("(0,lowerBound,EReference,eContainingClass,EClass,abstract,false)");
//		stopwEcore.add("(0,lowerBound,EReference,eType,EClass,abstract,false)");
//		stopwEcore.add("(0,lowerBound,EReference,eType,EClass,eStructuralFeatures,EAttribute,upperBound,1)");
//        stopwEcore.add("(0,lowerBound,EReference,upperBound,-1)");
//        stopwEcore.add("(1,upperBound,EAttribute)");
//        stopwEcore.add("(1,upperBound,EAttribute,eContainingClass,EClass,abstract,false)");
//        stopwEcore.add("(1,upperBound,EAttribute,eType,EDataType)");
//        stopwEcore.add("(1,upperBound,EAttribute,lowerBound,0)");
//        stopwEcore.add("(1,upperBound,EReference)");
//        stopwEcore.add("(1,upperBound,EReference,eContainingClass,EClass,abstract,false)");
//        stopwEcore.add("(EDataType)");
//        stopwEcore.add("(false,abstract,EClass)");
//        stopwEcore.add("(false,abstract,EClass,ePackage,EPackage,eClassifiers,EClass,abstract,false)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EAttribute,eType,EDataType)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EAttribute,lowerBound,0)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EAttribute,upperBound,1)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EReference,containment,true)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EReference,eType,EClass,abstract,false)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EReference,lowerBound,0)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EReference,upperBound,-1)");
//        stopwEcore.add("(false,abstract,EClass,eStructuralFeatures,EReference,upperBound,1)");
//        stopwEcore.add("(true,containment,EReference)");
//        stopwEcore.add("(true,containment,EReference,eContainingClass,EClass,abstract,false)");
//        stopwEcore.add("(true,containment,EReference,eType,EClass,abstract,false)");
//        stopwEcore.add("(true,containment,EReference,lowerBound,0)");
//        stopwEcore.add("(true,containment,EReference,upperBound,-1)");
        
        stopwords.put("ecore", stopwEcore);
        
        Set<String> stopwSM = new HashSet<>();
        
        stopwSM.add("(external,kind,Transition)");
        stopwSM.add("(external,kind,Transition,container,Region,subvertex,Pseudostate,kind,initial)");
        stopwSM.add("(external,kind,Transition,container,Region,transition,Transition,kind,external)");
        stopwSM.add("(external,kind,Transition,source,Pseudostate,kind,initial)");
        stopwSM.add("(initial,kind,Pseudostate)");
        stopwSM.add("(initial,kind,Pseudostate,container,Region,transition,Transition,kind,external)");
        
        stopwords.put("uml_sm", stopwSM);
        
        stopwords.put("uml", new HashSet<>());
		
	}

	protected Map<String, Map<String,Integer>> computeParticionedPaths(@NonNull Resource r) {
		return pathComputation.getListOfPaths(r).toMapParticionedPaths();
	}
	
	protected Table getGlobal(String model) throws IOException {
		Connection connection = getConnection();
		return connection.getTable(TableName.valueOf("global_st_"+model));
	}
	
	protected Table getStopPathsTable(String model) throws IOException {
		Connection connection = getConnection();
		return connection.getTable(TableName.valueOf("stop_paths_"+model));
	}
	
	protected Table getIndex(String model) throws IOException {
		Connection connection = getConnection();
		return connection.getTable(TableName.valueOf("inverted_index_"+model));
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> score(Resource r, Profiler profiler) throws IOException {
		/*
		Kryo kryo = new Kryo();
 		kryo.register(PairInformation.class, new PairSerializer());
 		@SuppressWarnings("rawtypes")
		MapSerializer serializer = new MapSerializer();
 		// serializer.setKeyClass(String.class);
 		serializer.setKeyClass(String.class, kryo.getSerializer(String.class));
 		serializer.setKeysCanBeNull(false);
 		serializer.setValueClass(PairInformation.class, new PairSerializer());
 		serializer.setValuesCanBeNull(false);
 		kryo.register(HashMap.class, serializer);
		*/
 		
		//stopwords = new HashSet<String>();
		
		profiler.start();
			Set<String> stpw = getStopWords(model);
		profiler.stop("stop-words");				
			//stopwords = new HashSet<String>();
		
		
		//Connection connection = ConnectionFactory.createConnection(conf);
		
		Table inverted_index = getIndex(model);
		//Table docs_info = getDocsInfo();
		//Table global_st = getGlobal_st();
	    
	    //global params
		GlobalStats stats = getGlobalStats(model);
	    		
		profiler.start();
	    	Map<String, Map<String,Integer>> hm = computeParticionedPaths(r);
		profiler.stop("paths");
		
		
		
		profiler.start();
			final byte[] DOC = "doc".getBytes();
		    List<Get> gets = new LinkedList<Get>();
		    for (Entry<String,Map<String,Integer>> tokenPair : hm.entrySet()) {
//		    	if(stopwords.contains(tokenPair.getKey()))
//		    		continue;
		    	Get get = new Get(tokenPair.getKey().getBytes());
		    	//added max_ver in order to support incremental index
		    	get.setMaxVersions(5);
		    	
		    	tokenPair.getValue().forEach((a,b) -> {
		    		String full = tokenPair.getKey() + a;
		    		if (!stpw.contains(full)) 
		    			get.addColumn(DOC, Bytes.toBytes(a));
		    	});
		    	gets.add(get);
		    }
		
	    profiler.stop("prepare-gets");
	    
	    profiler.start();
	    	Result[] results = inverted_index.get(gets);
	    profiler.stop("get");
	    
	    
	    
	    profiler.start();
	    
	    	
		    BM25ScoreCalculator calculator = new BM25ScoreCalculator(stats);		    
	    	//CountScoreCalculator calculator = new CountScoreCalculator();
	    	
		    for (Result result : results) {			
		    	//no results, go on
		    	if (result.isEmpty())
		    		continue;
		    	
		    	String part1 = Bytes.toString(result.getRow());
		    	
		    	NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> mapAllVersions = result.getMap();
		    	NavigableMap<byte[], NavigableMap<Long, byte[]>> colsAllVersions = mapAllVersions.get(DOC);
		    	
		    	Map<String,Integer> parts2 = hm.get(part1);
		    	
		    	parts2.forEach((key,value) -> {
		    		String full = part1 + key;
		    		NavigableMap<Long, byte[]> doc_nocur_ntokens = colsAllVersions.get(key.getBytes());
		    		
		    		if (doc_nocur_ntokens!=null) {
		    			doc_nocur_ntokens.forEach((version,dnn) -> {
			    			Map<String, PairInformation>  mw=null;
			    			try {
//			    				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(doc_nocur_ntokens);
//			    				Input input = new Input(byteArrayInputStream,doc_nocur_ntokens.length);
//			    				mw = (Map<String, PairInformation>) kryo.readObject(input, HashMap.class);
//			    				input.close();
			    				
			    				mw = PathMapSerializer.deserialize(dnn);
			    			} catch (Exception e) {
								e.printStackTrace();
							}
			    			
			    			int n_docs_t = mw.size();
			    			
			    			
			    			mw.forEach((doc,nocur_ntokens)-> {
			    				
			    				int nocur = nocur_ntokens.getNocurrences();
			    				int ntokens = nocur_ntokens.getnTokensDoc();

			    				calculator.addPath(full, doc, n_docs_t, value, nocur, ntokens);
			    				//calculator.addPath(full, doc);
			    			});
			    		});
		    		}
		    		
		    	});
		    	
		    	
		    	
			}
		profiler.stop("score");

		profiler.toOutput(System.out);
		
		return calculator.getScores();
	}

	@Override
	public Set<String> getStopWords(String model) throws IOException {
		if (stopwords.containsKey(model))
			return stopwords.get(model);
		return Collections.emptySet();
	}

	protected GlobalStats getGlobalStats(String model) throws IOException {		
		Table global_st = getGlobal(model);

		Get get_stats = new Get("stats".getBytes());
	    Result result_stats = global_st.get(get_stats);
	    
	    if (result_stats.isEmpty()) 
	    	throw new IllegalStateException("Table stats empty");
	    	
    	Map<byte[],byte[]> stats = result_stats.getFamilyMap("stats".getBytes());
    	long nTokens = Bytes.toLong(stats.get("nTokens".getBytes()));
    	long gr  = Bytes.toLong(stats.get("ndocs".getBytes()));
    	double avg = (double) nTokens / (double) gr;
    	
    	global_st.close();
    
	    
	    return new GlobalStats(gr, avg);
	}
	
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	private Set<String> initializeStopWords(String model) throws IOException {
		Table sptable = getStopPathsTable(model);
		
		Scan scan = new Scan();
		
		ResultScanner scanner = sptable.getScanner(scan);
		
		Set<String> sw = new HashSet<String>();
	    
		for (Result result = scanner.next(); result != null; result = scanner.next()) {
			sw.add(Bytes.toString(result.getRow()));
		}
		
		sptable.close();
		
		return sw;
		
		
		
	}

	@Override
	public Map<String, List<Double>> partitionedScore(Resource r, Profiler profiler) throws IOException {

		
		profiler.start();
			Set<String> stpw = getStopWords(model);
		profiler.stop("stop-words");				

		
		
		//Connection connection = ConnectionFactory.createConnection(conf);
		
		Table inverted_index = getIndex(model);
		//Table docs_info = getDocsInfo();
		//Table global_st = getGlobal_st();
	    
	    //global params
		GlobalStats stats = getGlobalStats(model);
	    		
		profiler.start();
	    	Map<String, Map<String,Integer>> hm = computeParticionedPaths(r);
		profiler.stop("paths");
		
		
		
		profiler.start();
			final byte[] DOC = "doc".getBytes();
		    List<Get> gets = new LinkedList<Get>();
		    for (Entry<String,Map<String,Integer>> tokenPair : hm.entrySet()) {
//		    	if(stopwords.contains(tokenPair.getKey()))
//		    		continue;
		    	Get get = new Get(tokenPair.getKey().getBytes());
		    	//added max_ver in order to support incremental index
		    	get.setMaxVersions(5);
		    	
		    	tokenPair.getValue().forEach((a,b) -> {
		    		String full = tokenPair.getKey() + a;
		    		if (!stpw.contains(full)) 
		    			get.addColumn(DOC, Bytes.toBytes(a));
		    	});
		    	gets.add(get);
		    }
		
	    profiler.stop("prepare-gets");
	    
	    profiler.start();
	    	Result[] results = inverted_index.get(gets);
	    profiler.stop("get");
	    
	    
	    
	    profiler.start();
	    
	    	
	    BM25ScoreCalculator calculator = new BM25ScoreCalculator(stats);		    
    		//CountScoreCalculator calculator = new CountScoreCalculator();		    
		    
		    for (Result result : results) {			
		    	//no results, go on
		    	if (result.isEmpty())
		    		continue;
		    	
		    	String part1 = Bytes.toString(result.getRow());
		    	
		    	NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> mapAllVersions = result.getMap();
		    	NavigableMap<byte[], NavigableMap<Long, byte[]>> colsAllVersions = mapAllVersions.get(DOC);
		    	
		    	Map<String,Integer> parts2 = hm.get(part1);
		    	
		    	parts2.forEach((key,value) -> {
		    		String full = part1 + key;
		    		NavigableMap<Long, byte[]> doc_nocur_ntokens = colsAllVersions.get(key.getBytes());
		    		
		    		if (doc_nocur_ntokens!=null) {
		    			doc_nocur_ntokens.forEach((version,dnn) -> {
			    			Map<String, PairInformation>  mw=null;
			    			try {
//			    				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(doc_nocur_ntokens);
//			    				Input input = new Input(byteArrayInputStream,doc_nocur_ntokens.length);
//			    				mw = (Map<String, PairInformation>) kryo.readObject(input, HashMap.class);
//			    				input.close();
			    				
			    				mw = PathMapSerializer.deserialize(dnn);
			    			} catch (Exception e) {
								e.printStackTrace();
							}
			    			
			    			int n_docs_t = mw.size();
			    			
			    			
			    			mw.forEach((doc,nocur_ntokens)-> {
			    				
			    				int nocur = nocur_ntokens.getNocurrences();
			    				int ntokens = nocur_ntokens.getnTokensDoc();
			    				//System.out.println(doc + Double.toString(ntokens));
			    				//calculator.addPath(full, doc, n_docs_t, value, nocur.intValue(), ntokens.intValue());
			    				calculator.addPath(full, doc, n_docs_t, value, nocur, ntokens);
			    				//calculator.addPath(full, doc);
			    				//debug.addDocPath(doc, full);
			    			});
			    		});
		    		}
		    		
		    	});
		    	
		    	
		    	
			}
		profiler.stop("score");

		profiler.toOutput(System.out);
		
		return calculator.getPartitionedScores();
	}


}
