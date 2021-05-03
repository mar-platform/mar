package mar.paths;

public class PairInformation {
	
	private int nocurrences;
	
	private int nTokensDoc;
	
	public PairInformation() {
		
	}

	public PairInformation(int nocurrences, int nTokensDoc) {
		super();
		this.nocurrences = nocurrences;
		this.nTokensDoc = nTokensDoc;
	}

	public int getNocurrences() {
		return nocurrences;
	}

	public void setNocurrences(int nocurrences) {
		this.nocurrences = nocurrences;
	}

	public int getnTokensDoc() {
		return nTokensDoc;
	}

	public void setnTokensDoc(int nTokensDoc) {
		this.nTokensDoc = nTokensDoc;
	}
	
	
}
