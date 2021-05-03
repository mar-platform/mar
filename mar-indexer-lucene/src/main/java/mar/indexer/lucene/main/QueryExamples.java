package mar.indexer.lucene.main;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSortSortedNumericDocValuesRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

public class QueryExamples {
	public static void main(String[] args) {
		BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		
		Query content = new PhraseQuery("content", "hello world");
		Query fallbackQuery = LongPoint.newRangeQuery("size", 10, 20);
		Query popularity = LongPoint.newRangeQuery("popularity", 100, 1000);
		
		booleanQuery.add(content, Occur.SHOULD);
		booleanQuery.add(fallbackQuery, Occur.SHOULD);
		booleanQuery.add(popularity, Occur.SHOULD);
		
		Query definitive = booleanQuery.build();
		
	}
}
