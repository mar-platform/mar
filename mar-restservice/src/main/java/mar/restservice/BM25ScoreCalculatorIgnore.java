package mar.restservice;

import java.util.List;

public class BM25ScoreCalculatorIgnore extends BM25ScoreCalculator{
	
	private final List<? extends String> ignored;
	
	public BM25ScoreCalculatorIgnore(GlobalStats stats, List<? extends String> ignored) {
		super(stats);
		this.ignored = ignored;
	}
	
	@Override
	public void addPath(String path, String docName, int totalDocsContainingPath, int pathOcurrencesInQuery,
			int pathOcurrencesInDoc, int totalPathsInDoc) {
		if (!ignored.contains(docName)) {
			super.addPath(path, docName, totalDocsContainingPath, pathOcurrencesInQuery,
					pathOcurrencesInDoc, totalPathsInDoc);
		}
	}

}
