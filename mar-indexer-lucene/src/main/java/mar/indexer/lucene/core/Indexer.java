package mar.indexer.lucene.core;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.emf.ecore.resource.Resource;

import mar.model2graph.IMetaFilter;
import mar.model2graph.MetaFilter;
import mar.paths.PathFactory;

public class Indexer implements Closeable{
	
	@Nonnull
	private final IndexWriter writer;
	
	public Indexer(@Nonnull String pathIndex) throws IOException {
		Directory indexDirectory = FSDirectory.open(Paths.get(pathIndex));
		IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
		writer = new IndexWriter(indexDirectory,conf);
	}
	
	public void close() throws IOException {
		writer.close();
	}
	
	public void indexModel(String id, String type, Resource r, 
			IMetaFilter mf, PathFactory pf) throws IOException {
		Document d = LuceneUtils.model2document(id, type, r, mf, pf);
		writer.addDocument(d);
	}
	
}
