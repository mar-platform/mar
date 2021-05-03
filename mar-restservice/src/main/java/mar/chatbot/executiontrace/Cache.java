package mar.chatbot.executiontrace;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import mar.chatbot.elements.ElementsSet;
import mar.chatbot.elements.IElement;


public class Cache {
	
	private LoadingCache<Integer, ExecutionGraph> cache;
	private AtomicInteger lastKey = new AtomicInteger(0);
	
	public Cache() {
		CacheLoader<Integer, ExecutionGraph> cacheLoader = new CacheLoader<Integer, ExecutionGraph>() {
			@Override
			public ExecutionGraph load(Integer arg0) throws Exception {
				return new ExecutionGraph();
			}
		};
		cache = CacheBuilder.newBuilder()
			       .maximumSize(1000)
			       .expireAfterAccess(10, TimeUnit.MINUTES)
			       .build(cacheLoader);
		
	}
	
	public void addStep(Map<String, Double> results, IElement query, int key) {
		ExecutionGraph graph = getGraph(key);
		graph.addStep(results, query);
		cache.put(key, graph);
	}
	
	public ElementsSet getGlobalQuery(int key) {
		ExecutionGraph graph = getGraph(key);
		return graph.getGlobalQuery();
	}
	
	public ExecutionGraph getGraph(int key) {
		ExecutionGraph graph = cache.getUnchecked(key);
		return graph;
	}
	
	public int getKey() {
		int key = lastKey.get();
		lastKey.accumulateAndGet(1,  new IntBinaryOperator() {
			@Override
			public int applyAsInt(int arg0, int arg1) {
				return arg0 + arg1;
			}
		});
		return key;
	}
	
}
