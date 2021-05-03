package mar.neural.search.core;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EmbeddingsSerializer {
	
	public static byte[] serialize(Embeddings embs) {
		return embs.getEmb().toString().getBytes();
	}
	
	public static Embeddings deserialize(byte[] emb) {
		String s = new String(emb);
		Gson gson = new Gson();
		Embeddings embsobject = new Embeddings(gson.fromJson(s,
	    		new TypeToken<List<Double>>(){}.getType()));
		return embsobject;
	}
}
