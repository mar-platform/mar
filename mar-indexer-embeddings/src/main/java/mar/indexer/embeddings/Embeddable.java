package mar.indexer.embeddings;

import java.util.List;

public interface Embeddable {

	int getSeqId();

	List<? extends String> getWords();

	default float[] getVector() {
		throw new UnsupportedOperationException();
	}
	
	default boolean isAlreadyEmbedded() {
		return false;
	}
	
}
