package mar.indexer.embeddings;

import java.util.Arrays;
import java.util.List;

public class IndexedPath implements Embeddable {

	private int seqId;
	private List<String> words;

	public IndexedPath(int seqId, String[] words) {
		this.seqId = seqId;
		this.words = Arrays.asList(words);
	}
	
	@Override
	public int getSeqId() {
		return seqId;
	}
	
	public List<? extends String> getWords() {
		return words;
	}
	
}
