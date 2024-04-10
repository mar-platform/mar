package mar.indexer.embeddings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import com.google.common.base.Preconditions;

import mar.paths.stemming.CamelCaseTokenizer;
import opennlp.tools.util.wordvector.Glove;
import opennlp.tools.util.wordvector.WordVector;
import opennlp.tools.util.wordvector.WordVectorTable;

public interface EmbeddingStrategy {

	static final int WORDE_DIMENSIONS = 300;
	
	public int size();
	
	public float[] toVector(WordedModel r);
	
	public static class GloveWordE implements EmbeddingStrategy {
		protected WordVectorTable gloveVectors;

		public GloveWordE(File vectorsFile) throws IOException {
			WordVectorTable table = Glove.parse(new FileInputStream(vectorsFile));		
			this.gloveVectors = table;
			Preconditions.checkState(WORDE_DIMENSIONS == gloveVectors.dimension());
			
			/*
			Word2Vec w2vModel = WordVectorSerializer.readWord2VecModel(vectorsFile);
			System.out.println(w2vModel);
			*/			
		}
		
		@Override
		public int size() {			
			return WORDE_DIMENSIONS;
		}
		
		public float[] toVector(WordedModel r) {
			List<? extends String> words = r.getWords();
			return getVectorsFromWords(words);
		}
		
		protected float[] getVectorsFromWords(List<? extends String> words) {			
			
			int totalVectors = 0;
			float[] result = new float[WORDE_DIMENSIONS];
			for (String original : words) {
				String w = original.toLowerCase();
				WordVector v = gloveVectors.get(w);
				
				List<WordVector> moreVectors;
				if (v == null) {
					String[] moreWords = CamelCaseTokenizer.INSTANCE.tokenize(original);
					// String[] moreWords = extractor.split(original);
					
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
	
	public static class GloveConcatEmbeddings extends GloveWordE {

		public GloveConcatEmbeddings(File vectorsFile) throws IOException {
			super(vectorsFile);
		}
		
		@Override
		public int size() {			
			return super.size() * 2;
		}		
		
		@Override
		public float[] toVector(WordedModel r) {
			List<? extends String> classes  = r.getWordsFromCategory(WordExtractor.CLASS_CATEGORY);
			List<? extends String> features = r.getWordsFromCategory(WordExtractor.FEATURE_CATEGORY);
			float[] v1  = getVectorsFromWords(classes);
			float[] v2  = getVectorsFromWords(features);
			float[] res = new float[v1.length + v2.length];
			int i = 0;
			for(; i < v1.length; i++) res[i] = v1[i];
			for(int j = 0; j < v2.length; j++) res[i++] = v2[j];
			
			return res;
		}
	}
	
	public static class FastTextWordE implements EmbeddingStrategy {

		private final FastText ft;

		public FastTextWordE(File folder) {
			this.ft = new FastText();
			this.ft.loadPretrainedVectors(folder);
		}
		
		@Override
		public int size() {			
			return WORDE_DIMENSIONS;
		}
		
		@Override
		public float[] toVector(WordedModel r) {
			// TODO Auto-generated method stub
			return null;
		} 
		
	}
	
}
