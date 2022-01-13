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
	
	private LoadingCache<Integer, Conversation> cache;
	private AtomicInteger lastKey = new AtomicInteger(0);
	
	public Cache() {
		CacheLoader<Integer, Conversation> cacheLoader = new CacheLoader<Integer, Conversation>() {
			@Override
			public Conversation load(Integer arg0) throws Exception {
				return new Conversation();
			}
		};
		cache = CacheBuilder.newBuilder()
			       .maximumSize(1000)
			       .expireAfterAccess(10, TimeUnit.MINUTES)
			       .build(cacheLoader);
		
	}
	
	public void addStep(Map<String, Double> results, IElement query, int key) {
		Conversation conversation = getConversation(key);
		ExecutionGraph graph = conversation.getGraph();
		graph.addStep(results, query);
		cache.put(key, conversation); // Is this needed??
	}
	
	public ElementsSet getGlobalQuery(int key) {
		ExecutionGraph graph = getConversation(key).getGraph();
		return graph.getGlobalQuery();
	}
	
	public Conversation getConversation(int key) {
		return cache.getUnchecked(key);
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

	public boolean hasKey(int key) {
		return cache.getIfPresent(key) != null;
	}
	
}
