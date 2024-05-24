package mar.indexer.embeddings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

public class WordList {
	private Map<String, List<String>> impl = new HashMap<>();
	
	public static final String ALL = "all";
	
	public WordList(String... categories) {
		for (String str : categories) {
			impl.put(str, new ArrayList<>(16));
		}
		impl.put(ALL, new ArrayList<>(64));
	}
	
	public void add(String value) {
		add(ALL, value);
	}

	public void add(String category, String value) {
		List<String> words = impl.get(category);
		words.add(value);
	}

	public List<? extends String> all() {
		return impl.get(ALL);
	}

	public List<? extends String> fromCategory(String category) {
		List<String> words = impl.get(category);
		Preconditions.checkState(words != null, "No words in category: " + category);
		return words;
	}

}
