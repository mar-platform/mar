package mar.indexer.embeddings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.base.Preconditions;

import opennlp.tools.util.wordvector.Glove;
import opennlp.tools.util.wordvector.WordVector;
import opennlp.tools.util.wordvector.WordVectorTable;

public interface EmbeddingStrategy {

	static final int WORDE_DIMENSIONS = 300;
	
	
	public float[] toVector(Resource r);
	
	public static class GloveWordE implements EmbeddingStrategy {
		private final WordExtractor extractor = WordExtractor.NAME_EXTRACTOR;
		private WordVectorTable gloveVectors;

		public GloveWordE(File vectorsFile) throws IOException {
			WordVectorTable table = Glove.parse(new FileInputStream(vectorsFile));		
			this.gloveVectors = table;
			/*
			Word2Vec w2vModel = WordVectorSerializer.readWord2VecModel(vectorsFile);
			System.out.println(w2vModel);
			*/
			
		}
		
		public float[] toVector(Resource r) {
			List<String> words = extractor.toWords(r);
			
			Preconditions.checkState(WORDE_DIMENSIONS == gloveVectors.dimension());
			
			int totalVectors = 0;
			float[] result = new float[WORDE_DIMENSIONS];
			for (String original : words) {
				String w = original.toLowerCase();
				WordVector v = gloveVectors.get(w);
				
				List<WordVector> moreVectors;
				if (v == null) {
					String[] moreWords = extractor.split(original);
					moreVectors = new ArrayList<>(moreWords.length);
					for (String string : moreWords) {
						WordVector v2 = gloveVectors.get(string);
						if (v2 != null) {
							moreVectors.add(v2);
						}
					}
				} else {
					moreVectors = Collections.singletonList(v);
				}
				
				for(int j = 0; j < moreVectors.size(); j++) {
					v = moreVectors.get(j);
					for(int i = 0, len = WORDE_DIMENSIONS; i < len; i++) {
						result[i] += v.getAsDouble(i);
					}
					totalVectors++;
				}
			}
			
			if (totalVectors == 0) {
				return result;
			}
			
			for(int i = 0, len = WORDE_DIMENSIONS; i < len; i++) {
				result[i] = result[i] / totalVectors;
			}
			
			return result;			
		}
	}
}
