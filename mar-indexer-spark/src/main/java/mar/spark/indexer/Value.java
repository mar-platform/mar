package mar.spark.indexer;

import java.io.Serializable;

import javax.annotation.Nonnull;

public class Value implements Serializable {
	private static final long serialVersionUID = -5692076661834118217L;

	@Nonnull
	private final String docId;	
	private final int ntokens;	
	private final int nocurrences;

	public Value(@Nonnull String docId, int ntokens, int nocurrences) {
		this.docId = docId;
		this.ntokens = ntokens;
		this.nocurrences = nocurrences;
	}
	
	@Nonnull
	public String getDocId() {
		return docId;
	}

	public int getNtokens() {
		return ntokens;
	}
	
	public int getNocurrences() {
		return nocurrences;
	}

}
