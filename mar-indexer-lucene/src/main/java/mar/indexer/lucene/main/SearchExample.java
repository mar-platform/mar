package mar.indexer.lucene.main;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import mar.indexer.lucene.core.LuceneUtils;
import mar.indexer.lucene.core.Searcher;
import mar.paths.PathFactory;

public class SearchExample {
	
	public static void main(String[] args) throws IOException, ParseException {

		if (args.length != 2) {
			System.err.println("The inverted index path is needed");
			System.err.println("The model type is needed");
			return;
		}
		String pathIndex = args[0];
		String modelType = args[1];
		
		PathFactory pf = new PathFactory.EcoreTokenizer();
		if (modelType.equals("ecore")) {
			pf = new PathFactory.EcoreTokenizer();
		}else if (modelType.equals("uml")) {
			pf = new PathFactory.EcoreTokenizer();
		}
		
		Searcher searcher = new Searcher(pathIndex);
		String query = "hospital patient state asdasdasd";
		TopDocs td = searcher.topDocs(query, pf);
		for (ScoreDoc sd : td.scoreDocs) {
			System.out.println(searcher.getDoc(sd.doc).getField(LuceneUtils.ID).getCharSequenceValue());
		}

	}
}
