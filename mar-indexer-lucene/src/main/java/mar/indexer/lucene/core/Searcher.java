package mar.indexer.lucene.core;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import mar.paths.PathFactory;

public class Searcher {
	
	@Nonnull
	private final IndexSearcher indexSearcher;
	@Nonnull
	private final QueryParser queryParser;
	
	public Searcher(@Nonnull String pathIndex) 
		      throws IOException {
		Directory indexDirectory = FSDirectory.open(Paths.get(pathIndex));
		DirectoryReader dr = DirectoryReader.open(indexDirectory);
		indexSearcher = new IndexSearcher(dr);
		queryParser = new QueryParser(LuceneUtils.CONTENTS, new WhitespaceAnalyzer());
	}
	
	@Nonnull
	public TopDocs topDocs (@Nonnull String searchQuery, @Nonnull PathFactory pf) throws ParseException, IOException {
		List<String> result = LuceneUtils.applyTokSwStem(searchQuery, pf);
		
		String queryResult = "";
		if (!result.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			String separator = "";
			for (String string : result) {
				builder.append(separator).append(string);
				separator = " ";
			}			 
			queryResult = builder.toString();
		}

		Query query = queryParser.parse(queryResult);
		return indexSearcher.search(query, 1000);
	}
	
	public Document getDoc(int doc) throws IOException {
		return indexSearcher.doc(doc);
	}

}
