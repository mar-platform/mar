package mar.restservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.eclipse.emf.ecore.resource.Resource;

import mar.model2graph.MetaFilter;
import mar.neural.search.core.Embeddings;
import mar.neural.search.core.EmbeddingsSerializer;
import mar.neural.search.core.GetEmbeddings;

public class HBaseNeuralScorer extends AbstractHBaseAccess implements IScorer{
	
	private HBaseScorerFinal hsf;

	public HBaseNeuralScorer(HBaseScorerFinal hsf) {
		super();
		this.hsf = hsf;
	}

	@Override
	public Map<String, Double> score(Resource r, Profiler profiler) throws IOException {
		Map<String,Double> nn = hsf.sortedScore(r, 100);
		//get embedding
		GetEmbeddings getEmb = new GetEmbeddings();
		Embeddings e_query = getEmb.getEmbeddings(r, MetaFilter.getEcoreFilter());
		
		Map<String,Double> result = new HashMap<>();
		for (String modelId : nn.keySet()) {
			//access2hbase
			Embeddings emb = getHbaseEmbeddings(modelId);
			result.put(modelId, e_query.cosSim(emb));
		}
		
		
		return result;
	}

	@Override
	public Set<String> getStopWords(String model) throws IOException {
		return hsf.getStopWords(model);
	}
	
	protected Table getIndex(String model) throws IOException {
		Connection connection = getConnection();
		return connection.getTable(TableName.valueOf("inverted_index_"+model));
	}
	
	private Embeddings getHbaseEmbeddings(String modelId) throws IOException {
		Connection connection = getConnection();
		Table docs_info = connection.getTable(TableName.valueOf("docs_info_"+hsf.getModel()));
		
		Get get = new Get(modelId.getBytes());
		get.addColumn("information".getBytes(),"embeddings".getBytes());
		
		Result r = docs_info.get(get);
		
		Embeddings res = EmbeddingsSerializer.deserialize(r.getValue("information".getBytes(),"embeddings".getBytes()));
		
		docs_info.close();
		
		return res;
		
	}

	@Override
	public Map<String, List<Double>> partitionedScore(Resource r, Profiler profiler) throws IOException {
		return hsf.partitionedScore(r, profiler);
	}
	

}
