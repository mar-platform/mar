package mar.restservice.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import mar.indexer.lucene.core.ITextSearcher;
import mar.indexer.lucene.core.LuceneUtils;
import mar.paths.PathFactory.DefaultPathFactory;
import mar.restservice.HBaseGetInfo;

/**
 * Implementation of the backbone of the search service, so that they are
 * reusable without interacting through the HTTP API.  
 * 
 * @author jesus
 *
 */
public class SearchService {

	@Nonnull
	private final ITextSearcher textSearcher;

	public SearchService(@Nonnull ITextSearcher textSearcher) {
		this.textSearcher = textSearcher;
	}

	/**
	 * Performs a text search using Lucene.
	 * 
	 * @param query A query in the form of keywords. It should also support Lucene syntax since it is passed directly, but this is not tested properly yet.
	 * @return The list of results
	 * @throws SearchException 
	 */
	public List<ResultItem> textSearch(@Nonnull String query) throws SearchException {
		try {
			TopDocs docs = textSearcher.topDocs(query, DefaultPathFactory.INSTANCE);
			List<ResultItem> items = new ArrayList<ResultItem>();
			for (ScoreDoc doc : docs.scoreDocs) {
				Document document = textSearcher.getDoc(doc.doc);
				String id = document.getField(LuceneUtils.ID).stringValue();
				String type = document.getField(LuceneUtils.TYPE).stringValue();
				items.add(new ResultItem(id, doc.score, type));				
			}
	
			// Update the information of each model in batch
			try(HBaseGetInfo info = new HBaseGetInfo()) {
				info.updateInformation(items);
			}
			
			return items;
		} catch (ParseException e) {
			throw new SearchException(e);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}
	
	
	public static class SearchException extends Exception {
		private static final long serialVersionUID = -5346601767973649199L;

		public SearchException(Throwable e) {
			super(e);
		}
		
	}
}
