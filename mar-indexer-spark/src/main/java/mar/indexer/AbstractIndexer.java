package mar.indexer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;

import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.ModelLoader;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.model2graph.PathComputation;
import mar.paths.PartitionedPathMap;
import mar.spark.indexer.CompositeKey;
import mar.spark.indexer.ErrorModel;
import mar.spark.indexer.ErrorModelPaths;
import mar.spark.indexer.IError;
import mar.spark.indexer.IModelPaths;
import mar.spark.indexer.LoadedModel;
import mar.spark.indexer.ModelOrigin;
import mar.spark.indexer.ModelPaths;
import mar.spark.indexer.Value;
import scala.Tuple2;

public class AbstractIndexer {
	
	private static Logger log = org.apache.log4j.LogManager.getLogger(AbstractIndexer.class);
	
	@Nonnull
	protected static LoadedModel toResource(@Nonnull ModelOrigin model) {
		try {
			SingleIndexJob repoConf = model.getRepoConf();
			ModelLoader loader = repoConf.getModelLoader();
			Resource r = loader.load(model.getModelFile());
			return new LoadedModel(r, model);
		} catch (Exception e) {
			log.error("Could not load: " + model.getModelFile(), e);
			return new ErrorModel(e, model);
		}
	}
	
	@Nonnull
	protected static IModelPaths toPathMap(@Nonnull LoadedModel m) throws InvalidJobSpecification {
		if (!m.isSuccess())
			return new ErrorModelPaths((IError) m);

		SingleIndexJob repoConf = m.getOrigin().getRepoConf();
		PathComputation pathComputation = repoConf.toPathComputation();
		log.info("Creating pathmap for " + m.resource.getURI());
		try {
			PartitionedPathMap paths = pathComputation.getListOfPaths(m.resource).toMapParticionedPaths();		
			return new ModelPaths(paths, m.getOrigin());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ErrorModelPaths(new ErrorModel(e, m.getOrigin()));
		} finally {
			m.resource.unload();
		}
	}
	
	/**
	 * Returns a pair associating the model as paths (IModelPaths) and its token count. 
	 */
	protected static Tuple2<IModelPaths, Integer> toModelCount(@Nonnull IModelPaths paths) {		
		PartitionedPathMap mapTokens = ((ModelPaths) paths).pathMap;

		int totalTokens = 0;
		Iterator<Entry<String, Map<String, Integer>>> it = mapTokens.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, Integer>> pair = it.next();
			Map<String, Integer> second_part = pair.getValue();
			Iterator<Entry<String, Integer>> it2 = second_part.entrySet().iterator();
			while (it2.hasNext()) {
				Entry<String, Integer> pair2 = it2.next();
				int ntokens = pair2.getValue();
				totalTokens = totalTokens + ntokens;
			}
		}

		return Tuple2.apply(paths, totalTokens);		
	}
	
	public static List<Tuple2<CompositeKey, Value>> toKeyValue(@Nonnull Tuple2<IModelPaths, Integer> pathCount) {		
		IModelPaths p = pathCount._1;
		
		PartitionedPathMap mapTokens = ((ModelPaths) p).pathMap;
		ModelOrigin origin = ((ModelPaths) p).origin;
		String docId = origin.getModelId();
		
		List<Tuple2<CompositeKey, Value>> result = new ArrayList<>(mapTokens.size());
		
		// count tokens
		int totalTokens = pathCount._2;

		// write context
		Iterator<Entry<String, Map<String, Integer>>> it = mapTokens.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Map<String, Integer>> pair = it.next();
			Map<String, Integer> second_part = pair.getValue();
			String commom = pair.getKey();
			Iterator<Entry<String, Integer>> it2 = second_part.entrySet().iterator();

			while (it2.hasNext()) {
				Entry<String, Integer> pair2 = it2.next();
				String full = commom + pair2.getKey();

				// ignore stop words
				if (false) //stopwords.contains(full))
					continue;

				int nTokens = totalTokens;
				int nOcurrences = pair2.getValue();
				
				Value v = new Value(docId, nTokens, nOcurrences);

				CompositeKey key = new CompositeKey();
				key.setPart1(commom);
				key.setPart2(pair2.getKey());

				// context.write(key, v);
				result.add(Tuple2.apply(key, v));
			}
		}
					
		return result;
	}
}
