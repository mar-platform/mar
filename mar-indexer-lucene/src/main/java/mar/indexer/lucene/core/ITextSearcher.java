package mar.indexer.lucene.core;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.TopDocs;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import mar.paths.PathFactory;

public interface ITextSearcher {

	@Nonnull
	TopDocs topDocs(String searchQuery, PathFactory pf) throws ParseException, IOException;

	@CheckForNull
	Document getDoc(int doc) throws IOException;

}