package mar.restservice.scoring;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

import edu.umd.cs.findbugs.annotations.NonNull;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mar.embeddings.scorer.PathRetriever;
import mar.embeddings.scorer.PathRetriever.SimilarPath;
import mar.model2graph.PathComputation;
import mar.paths.PartitionedPathMap;
import mar.paths.PathParser;
import mar.restservice.IScorer;
import mar.restservice.Profiler;
import mar.restservice.scoring.BM25ScoreCalculator.GlobalStats;
import mar.sqlite.SqliteIndexDatabase;
import mar.sqlite.SqliteIndexDatabase.Stats;
import mar.sqlite.SqlitePathRetriever;

public class VectorizedPathScorer implements IScorer {

	@NonNull
	private PathComputation pathComputation;
	@Nonnull
	private SqliteIndexDatabase database;
	@NonNull
	private PathRetriever vectorizedPathRetriever;

	public VectorizedPathScorer(@NonNull PathComputation pathComputation, @NonNull SqliteIndexDatabase database, @NonNull PathRetriever pathRetriever) {
		this.pathComputation = pathComputation;	
		this.database = database;
		this.vectorizedPathRetriever = pathRetriever;
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
			
			Collection<String> paths = flattenedPathCounts.keySet();
			Map<String, List<SimilarPath>> associatedPaths = new HashMap<>(paths.size());
			for (String path : paths) {
				//String[] words = PathParser.INSTANCE.toAttributeValues(path);
				String[] words = PathParser.INSTANCE.toFullPath(path);
				List<SimilarPath> pathIndexes = vectorizedPathRetriever.retrieve(words);
				
				associatedPaths.put(path, pathIndexes);
			}
			System.out.println("\n");
			
			SqlitePathRetriever retriever = new SqlitePathRetriever(database);

			associatedPaths.forEach((originalPath, similarPaths) -> {
				System.out.println(originalPath);
				for (SimilarPath sPaths : similarPaths) {					
					try {
						int[] pathIds = sPaths.getPathIds();
						
						//System.out.println("Original: " + originalPath + " " + IntStream.of(pathIds).mapToObj(Integer::toString).collect(Collectors.joining(",")) );
						retriever.retrieve(r, pathIds, (path, docId, numDocsWithPath, nTokens, nOccurences) -> {
							int pathOccurencesInQuery = flattenedPathCounts.getInt(path);
							
							// When the path doesn't appear in the original query, what we do is to use the similarity of score to weight the number of occurrences
							float poq_float = pathOccurencesInQuery;
							if (pathOccurencesInQuery == 0) {
								poq_float = sPaths.getScore();
							}
							
							System.out.println(" - " + path + "  " + pathOccurencesInQuery + " " + numDocsWithPath + " " + nTokens + " " + nOccurences);
							calculator.addPathApproximate(path, docId, numDocsWithPath, poq_float, nOccurences, nTokens);
						});
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
				}
				
			});
			
			
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
