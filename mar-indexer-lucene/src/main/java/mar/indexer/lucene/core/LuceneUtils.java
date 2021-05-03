package mar.indexer.lucene.core;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.eclipse.emf.ecore.resource.Resource;

import mar.model2graph.IMetaFilter;
import mar.model2text.Utils;
import mar.paths.PathFactory;

public class LuceneUtils {
	
	public static final String CONTENTS = "contents";
	public static final String ID = "id";
	public static final String TYPE = "type";	
	
	public static Document model2document(String id, String type, Resource r,  IMetaFilter mf, PathFactory pf) {
		
		String content = Utils.model2document(r, mf, pf);
		
		Document document = new Document();
		
		TextField contentField = new TextField(CONTENTS, content, Field.Store.YES);
		StoredField idField = new StoredField(ID, id);
		StoredField typeField = new StoredField(TYPE, type);

		document.add(contentField);
		document.add(idField);
		document.add(typeField);
				
		return document;
	}

	public static List<String> applyTokSwStem(String searchQuery, PathFactory pf) {
		return Utils.applyTokSwStem(searchQuery, pf);
	}
		
}
