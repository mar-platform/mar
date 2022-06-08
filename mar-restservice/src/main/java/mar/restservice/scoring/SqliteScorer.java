package mar.restservice.scoring;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicInteger;
import edu.umd.cs.findbugs.annotations.NonNull;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mar.model2graph.PathComputation;
import mar.paths.PartitionedPathMap;
import mar.restservice.IScorer;
import mar.restservice.Profiler;
import mar.restservice.scoring.BM25ScoreCalculator.GlobalStats;
import mar.sqlite.SqliteIndexDatabase;
import mar.sqlite.SqliteIndexDatabase.Stats;
import mar.sqlite.SqlitePathRetriever;

public class SqliteScorer implements IScorer {

	@NonNull
	private PathComputation pathComputation;
	@Nonnull
	private SqliteIndexDatabase database;

	public SqliteScorer(@NonNull PathComputation pathComputation, @NonNull SqliteIndexDatabase database) {
		this.pathComputation = pathComputation;	
		this.database = database;
	}

	protected PartitionedPathMap computeParticionedPaths(@NonNull Resource r) {
		return pathComputation.getListOfPaths(r).toMapParticionedPaths();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Double> score(Resource r, Profiler profiler) throws IOException {	
		profiler.start();
			Set<String> stpw = getStopWords();
		profiler.stop("stop-words");
	    
	    //global params
		GlobalStats stats = getGlobalStats();
	    		
		profiler.start();
			PartitionedPathMap hm = computeParticionedPaths(r);
			Object2IntOpenHashMap<String> flattenedPathCounts = new Object2IntOpenHashMap<>(hm.size());
			flattenedPathCounts.defaultReturnValue(0);
			hm.forEach((k, h) -> {
				h.forEach((k2, c) -> {
					String path = k + k2;
					flattenedPathCounts.addTo(path, c);
				});
			});
		profiler.stop("paths");
		
		profiler.start();
			BM25ScoreCalculator calculator = new BM25ScoreCalculator(stats);		    
		
			//System.out.println("Total Query paths: " + hm.size());
			AtomicInteger counter = new AtomicInteger();
			SqlitePathRetriever retriever = new SqlitePathRetriever(database);
			retriever.retrieve(r, hm, (path, docId, numDocsWithPath, nTokens, nOccurences) -> {
				int pathOccurencesInQuery = flattenedPathCounts.getInt(path);
				//System.out.println(path + " " + docId);
				calculator.addPath(path, docId, numDocsWithPath, pathOccurencesInQuery, nOccurences, nTokens);
				counter.incrementAndGet();
			});
			
			//System.out.println("Num. paths = " + counter.get());
			
		profiler.stop("score");

		profiler.toOutput(System.out);
		
		return calculator.getScores();
	}

	private Set<String> getStopWords() {
		return Collections.emptySet();
	}

	protected GlobalStats getGlobalStats() throws IOException {			
		try {
			Stats stats = database.getStats();
			if (stats == null) {
				throw new IllegalStateException("Table stats empty");			
			}

			long gr    = stats.totalDocuments;
			double avg = stats.totalTokens / (double) gr;
			return new GlobalStats(gr, avg);
		} catch (SQLException e) {
			throw new IOException(e);			
		}
	}

	@Override
	public Set<String> getStopWords(String model) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, List<Double>> partitionedScore(Resource r, Profiler profiler) throws IOException {
		throw new UnsupportedOperationException();
	}


}
