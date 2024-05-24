package mar.restservice.scoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import mar.restservice.IPartition;
import mar.restservice.partitions.PartitionTargetSource;

public class BM25ScoreCalculator {
	private final Object2DoubleMap<String> scores = new Object2DoubleOpenHashMap<String>(1024);
	private final HashMap<String,List<Double>> divided_scores = new HashMap<String,List<Double>>();
	private final IPartition partition = new PartitionTargetSource();//new PartitionByLen(3);
	
	/** Hyper-parameters */
	private final double k = 0.1; // 0.1
	private final double b = 0.8;// 0.8 0.75;

	private final double average;
	private final long numDocuments;

	public BM25ScoreCalculator(GlobalStats stats) {
		this.average = stats.average;
		this.numDocuments = stats.numDocuments;
	}

	/**
	 * This method should be called once for each path of the query, and for each
	 * document that contains the path.
	 * 
	 * @param path                    A path in the query for which there exists at
	 *                                least one document containing it
	 * @param docName                 The name of the document containing the path
	 * @param totalDocsContainingPath The total number of documents that contains
	 *                                the path
	 * @param pathOcurrencesInQuery   The number of ocurrences of the path in the
	 *                                query
	 * @param pathOcurrencesInDoc     The number of ocurrences of the path in the
	 *                                document
	 * @param totalPathsInDoc         The total number of paths in the document
	 */
	public void addPath(String path, String docName, int totalDocsContainingPath, int pathOcurrencesInQuery,
			int pathOcurrencesInDoc, int totalPathsInDoc) {
		final int c_w_q = pathOcurrencesInQuery;
		final int c_w_d = pathOcurrencesInDoc;
		final int n_docs_t = totalDocsContainingPath;
		final int doc_len = totalPathsInDoc;

		double old_score = scores.computeDoubleIfAbsent(docName, (k) -> 0.0d);
		double new_score = old_score + ((c_w_q * (k + 1) * c_w_d) / (c_w_d + k * (1 - b + b * (doc_len / average))))
				* Math.log((numDocuments + 1) / n_docs_t);
//			double new_score = old_score +
//					((c_w_q * (k+1) * c_w_d)/
//					(c_w_d + k*(1-b+b*(doc_len/average)))) * Math.log(((numDocuments - n_docs_t + 0.5)/(n_docs_t + 0.5))); //+ 1, TAKE CARE
		scores.put(docName, new_score);
		

		// This is part of the neural scorer which we don't use and in fact partition.getPartition with paths of type [(EGenericType)]
		if (false) {
			//TO DO: THIS IS NOT A GOOD WAY TO COMPUTE THE LENGTH
			List<Double> list = null;
			if (divided_scores.containsKey(docName)) {
				list = divided_scores.get(docName);
			} else {
				list = new LinkedList<Double>();
				for (int i = 0; i < partition.getNumPartitions() ; ++i)
					list.add(0.0);
				divided_scores.put(docName, list);
			}
			int pos = partition.getPartition(path);
			Double oldScore = list.get(pos);
			list.set(pos, oldScore + ((c_w_q * (k + 1) * c_w_d) / (c_w_d + k * (1 - b + b * (doc_len / average))))
					* Math.log((numDocuments + 1) / n_docs_t));
		}
		
	}

	public void addPathApproximate(String path, String docName, int totalDocsContainingPath, float pathOcurrencesInQuery,
			int pathOcurrencesInDoc, int totalPathsInDoc) {
		final float c_w_q = pathOcurrencesInQuery;
		final int c_w_d = pathOcurrencesInDoc;
		final int n_docs_t = totalDocsContainingPath;
		final int doc_len = totalPathsInDoc;

		double old_score = scores.computeDoubleIfAbsent(docName, (k) -> 0.0d);
		double new_score = old_score + ((c_w_q * (k + 1) * c_w_d) / (c_w_d + k * (1 - b + b * (doc_len / average))))
				* Math.log((numDocuments + 1) / n_docs_t);
//			double new_score = old_score +
//					((c_w_q * (k+1) * c_w_d)/
//					(c_w_d + k*(1-b+b*(doc_len/average)))) * Math.log(((numDocuments - n_docs_t + 0.5)/(n_docs_t + 0.5))); //+ 1, TAKE CARE
		scores.put(docName, new_score);
	}
		
		
	@NonNull
	public Map<String, Double> getScores() {
		return scores;
	}
	
	public Map<String,List<Double>> getPartitionedScores(){
		return divided_scores;
	}
	
	public static class GlobalStats {
		public final long numDocuments;
		public final double average;
		
		public GlobalStats(long numDocuments, double average) {
			this.numDocuments = numDocuments;
			this.average = average;
		}
	}
	
}