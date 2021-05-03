package mar.restservice.chatbot;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import mar.chatbot.elements.SingleElement;
import mar.paths.PairInformation;
import mar.paths.PathMapSerializer;
import mar.restservice.AbstractHBaseAccess;
import mar.restservice.BM25ScoreCalculator;
import mar.restservice.BM25ScoreCalculator.GlobalStats;

public class HBaseChatBotScorer extends AbstractHBaseAccess implements IChatBotScorer{
	
	private String model;

	public HBaseChatBotScorer(String model) {
		super();
		this.model = model;
	}


	@Override
	public Map<String, Double> scoreSingle(SingleElement single) throws IOException {
		final byte[] DOC = "doc".getBytes();
		Get get = new Get(single.getPrefix().getBytes());
		Table inverted_index = getIndex(model);
		GlobalStats stats = getGlobalStats(model);
		BM25ScoreCalculator calculator = new BM25ScoreCalculator(stats);
		Result r = inverted_index.get(get);
		
		if (!r.isEmpty()) {
			Map<byte[], byte[]> itmap = r.getFamilyMap(DOC);
			for (Entry<byte[], byte[]> entry : itmap.entrySet()) {
				if (!")".equals( Bytes.toString(entry.getKey())))
					continue;
				String full = single.getPrefix() + Bytes.toString(entry.getKey());
    			Map<String, PairInformation>  mw=null;
    			try {
    				mw = PathMapSerializer.deserialize(entry.getValue());
    			} catch (Exception e) {
					e.printStackTrace();
				}
    			int n_docs_t = mw.size();
    			mw.forEach((doc,nocur_ntokens)-> {
    				int nocur = nocur_ntokens.getNocurrences();
    				int ntokens = nocur_ntokens.getnTokensDoc();
    				calculator.addPath(full, doc, n_docs_t, 1, nocur, ntokens);
    			});
			}
		}
		return calculator.getScores();
	}
	
	protected Table getIndex(String model) throws IOException {
		Connection connection = getConnection();
		return connection.getTable(TableName.valueOf("inverted_index_"+model));
	}
	protected Table getGlobal(String model) throws IOException {
		Connection connection = getConnection();
		return connection.getTable(TableName.valueOf("global_st_"+model));
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

}
