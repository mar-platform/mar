package mar.indexer.embeddings;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.resource.Resource;

import com.clearspring.analytics.util.Preconditions;

import mar.embeddings.IndexedDB.IndexedModel;
import mar.modelling.loader.ILoader;

public class WordedModel implements Embeddable {

	private WordList words;
	
	private String modelId = null;
	private int seqId = -1;

	public WordedModel(Resource r, WordExtractor extractor) {
		this.words = extractor.toWords(r);	
	}
	
	public WordedModel(IndexedModel m, WordExtractor extractor, ILoader loader) throws IOException {
		this.seqId = m.getSeqId();
		this.modelId = m.getModelId();
		doSplit(m, extractor, loader);
	}
	
	private void doSplit(IndexedModel m, WordExtractor extractor, ILoader loader) throws IOException {
		System.out.println(m.getModelId());

		Resource loaded = null;
		try {
			loaded = loader.toEMF(m.getFile());
			this.words = extractor.toWords(loaded);			
		} finally {
			if (loaded != null)
				loaded.unload();
		}
	}
	
	public String getModelId() {
		Preconditions.checkState(modelId != null);
		return modelId;
	}
	
	@Override
	public List<? extends String> getWords() {
		return words.all().stream().filter(w -> ! isStopWord(w)).collect(Collectors.toList());
	}
	
	public boolean isStopWord(String w) {
		return false; //"name".equalsIgnoreCase(w);
	}

	//public List<? extends String> getAllWords() {
	//	return words.all();
	//}
	
	public List<? extends String> getWordsFromCategory(String category) {
		return words.fromCategory(category);
	}

	@Override
	public int getSeqId() {
		Preconditions.checkState(seqId >= 0);
		return seqId;
	}

}
