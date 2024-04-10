package mar.indexer.embeddings;

import java.util.List;

public interface Embeddable {

	int getSeqId();

	List<? extends String> getWords();

}
