package mar.indexer.embeddings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;

import org.deeplearning4j.models.fasttext.FastText;

import com.google.common.base.Preconditions;

import io.github.jbellis.jvector.vector.VectorUtil;
import mar.paths.stemming.CamelCaseTokenizer;
import opennlp.tools.util.wordvector.Glove;
import opennlp.tools.util.wordvector.WordVector;
import opennlp.tools.util.wordvector.WordVectorTable;
import opennlp.tools.util.wordvector.WordVectorType;

public interface EmbeddingStrategy {

	static final int WORDE_DIMENSIONS = 300;
	
	public int size();
	
	public float[] toVectorOrNull(Embeddable r);
	
	public default float[] toVector(Embeddable r) {
		float[] e = toVectorOrNull(r);
		if (e == null)
			return new float[size()];
		return e;
	}
	
	public default float[] toNormalizedVector(Embeddable r) {
		float[] e = toVectorOrNull(r);
		if (e == null)
			return new float[size()];
		VectorUtil.l2normalize(e);
		return e;
	}
	
	public float[] getWordVectorFromWord(String word);
	
	public static class GloveWordE extends AbstractWordE4MDEEmbedding {
		private WordVectorTable gloveVectors;

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
		public WordVector getWordVector(String w) {
			WordVector wv = gloveVectors.get(w);
			return wv;
		}
	
	}
	
	public static abstract class AbstractWordE4MDEEmbedding implements EmbeddingStrategy {	
		protected boolean bagOfWords = true;
		protected boolean splitWords = true;
		
		@Override
		public int size() {			
			return WORDE_DIMENSIONS;
		}
		
		@CheckForNull
		protected abstract WordVector getWordVector(String w);
		
		public float[] getWordVectorFromWord(String word) {
			WordVector wv = getWordVector(word);
			if (wv == null)
				return null;
			float[] v = new float[wv.dimension()];
			for(int i = 0, len = wv.dimension(); i < len; i++) {
				v[i] += wv.getAsFloat(i);
			}
			return v;
		}
		
		public float[] toVectorOrNull(Embeddable r) {
			List<? extends String> words = r.getWords();
			return getVectorsFromWords(words);
		}
		
		protected float[] getVectorsFromWords(Collection<? extends String> words) {			
			Set<String> bag = new HashSet<>();
			
			int totalVectors = 0;
			float[] result = new float[WORDE_DIMENSIONS];
			for (String original : words) {				
				//List<WordVector> moreVectors = getVectorFromString(original, v);
				List<WordVector> moreVectors = getVectorFromStringAggresive(original, bag);
				
				for(int j = 0; j < moreVectors.size(); j++) {
					WordVector v = moreVectors.get(j);
					for(int i = 0, len = WORDE_DIMENSIONS; i < len; i++) {
						result[i] += v.getAsDouble(i);
					}
					totalVectors++;
				}
			}
			
			if (totalVectors == 0) {
				return null;
			}
			
			for(int i = 0, len = WORDE_DIMENSIONS; i < len; i++) {
				result[i] = result[i] / totalVectors;
			}
			
			return result;			
		}

		private List<WordVector> getVectorFromStringAggresive(String original, Set<String> bag) {
			List<WordVector> moreVectors = null;

			// Tokenize first
			if (splitWords) {
				String[] moreWords = CamelCaseTokenizer.INSTANCE.tokenize(original);
				if (moreWords.length > 0) {			
					moreVectors = new ArrayList<>(moreWords.length);
					for (String string : moreWords) {
						if (bagOfWords && bag.contains(string)) {
							continue;
						}
						bag.add(string);
						
						WordVector v2 = getWordVector(string);
						if (v2 != null) {
							moreVectors.add(v2);
						}
					}
				}
			}

			// The single word
			String w = original.toLowerCase();
			if (bagOfWords && bag.contains(w)) {
				return moreVectors == null ? Collections.emptyList() : moreVectors;
			}
			
			bag.add(w);
			
			WordVector v = getWordVector(w);			
			if (v != null) {
				if (moreVectors == null)
					moreVectors = Collections.singletonList(v);
				else 
					moreVectors.add(v);
			}
			return moreVectors;
		}
		
		private List<WordVector> getVectorFromString(String original) {
			String w = original.toLowerCase();				
			WordVector v = getWordVector(w);
			
			List<WordVector> moreVectors;
			if (v == null) {
				if (! splitWords) {
					return Collections.emptyList();
				}
				
				String[] moreWords = CamelCaseTokenizer.INSTANCE.tokenize(original);
				// String[] moreWords = extractor.split(original);
				
				moreVectors = new ArrayList<>(moreWords.length);
				for (String string : moreWords) {
					WordVector v2 = getWordVector(string);
					if (v2 != null) {
						System.out.println("Word2: " + string);
						moreVectors.add(v2);
					}
				}
			} else {
				moreVectors = Collections.singletonList(v);
			}
			return moreVectors;
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
		public float[] toVector(Embeddable e) {
			WordedModel r = (WordedModel) e;
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
	
	public static class FastTextWordE extends AbstractWordE4MDEEmbedding {

		private final FastText ft;

		public FastTextWordE(File binaryFile) {
			this.ft = new FastText();
			this.ft.loadBinaryModel(binaryFile.getAbsolutePath());
			
			this.splitWords = false;
			this.bagOfWords = false;
		}
		
		@Override
		public int size() {			
			return WORDE_DIMENSIONS;
		}
		
		@Override
		public WordVector getWordVector(String w) {
			double[] d = ft.getWordVector(w);
			return new SimpleWordVector(d);
		}
			
		
	}
		
	static class SimpleWordVector implements WordVector {

		private double[] values;

		public SimpleWordVector(double[] values) {
			this.values = values;
		}
		
		@Override
		public WordVectorType getDataType() {
			return WordVectorType.DOUBLE;
		}

		@Override
		public float getAsFloat(int index) {
			return (float) values[index];
		}

		@Override
		public double getAsDouble(int index) {
			return values[index];
		}

		@Override
		public FloatBuffer toFloatBuffer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public DoubleBuffer toDoubleBuffer() {
			return DoubleBuffer.wrap(values).asReadOnlyBuffer();
		}

		@Override
		public int dimension() {
			return values.length;
		}
		
	}
	
}
