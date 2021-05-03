package mar.paths;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.paths.stemming.DefaultStemmer;
import mar.paths.stemming.DefaultStopWords;
import mar.paths.stemming.IStemmer;
import mar.paths.stemming.IStopWords;
import mar.paths.stemming.ITokenizer;
import mar.paths.stemming.WhiteSpaceANDCamelCaseTokenizer;
import mar.paths.stemming.WhitespaceTokenizer;

/**
 * 
 * Allows us to configure the creation of the paths, along with 
 * tokenizing and stemming process.
 * 
 * @author jesus
 *
 */
public interface PathFactory {

	@NonNull
	IStemmer getStemmer();
	
	//tokenizer should return toLowerCase tokens
	@NonNull
	ITokenizer getTokenizer();

	@NonNull
	IStopWords getStopWords();

	@NonNull
	ListofPaths newPathSet(List<? extends Path> paths);
	
	public static class DefaultPathFactory implements PathFactory {

		public static final DefaultPathFactory INSTANCE = new DefaultPathFactory();
		
		@Override
		public IStemmer getStemmer() {
			return new DefaultStemmer();
		}
		
		@Override
		public ITokenizer getTokenizer() {
			return new WhitespaceTokenizer();
		}
		
		@Override
		public IStopWords getStopWords() {
			return DefaultStopWords.INSTANCE;
		}

		@Override
		public ListofPaths newPathSet(List<? extends Path> paths) {
			return new ListofPathsComplexStopWords(paths, this);
		}
		
	}
	
	public static class justToLowerCasePathFactory implements PathFactory {

		@Override
		public IStemmer getStemmer() {
			
			return new IStemmer() {
				
				@Override
				public String stem(String lowerCase) {
					return lowerCase.toLowerCase();
				}
			};
		}

		@Override
		public ITokenizer getTokenizer() {
			return new ITokenizer() {
				
				@Override
				public String[] tokenize(String word) {
					String[] result = {word.toLowerCase()};
					return result;
				}
			};
		}

		@Override
		public IStopWords getStopWords() {
			return new IStopWords() {
				
				@Override
				public boolean isStopWord(String word) {
					return false;
				}
			};
		}
		//if simple, steeming has to perform toLowerCase
		@Override
		public ListofPaths newPathSet(List<? extends Path> paths) {
			return new ListofPathsSimple(paths, this);
		}
		
	}
	
	public static class DefaultPathFactoryWithWeigths implements PathFactory {
		
		@Override
		public IStemmer getStemmer() {
			return new DefaultStemmer();
		}

		@Override
		public ITokenizer getTokenizer() {
			return new WhitespaceTokenizer();
		}
		
		@Override
		public IStopWords getStopWords() {
			return DefaultStopWords.INSTANCE;
		}

		@Override
		public ListofPaths newPathSet(List<? extends Path> paths) {
			return new ListOfPathsComplexStopWordsWeighs(paths, this);
		}
		
	}
	
	public static class DefaultPathFactoryWithWeigthsEcore implements PathFactory {
			
			@Override
			public IStemmer getStemmer() {
				return new DefaultStemmer();
			}
	
			@Override
			public ITokenizer getTokenizer() {
				return new WhitespaceTokenizer();
			}
			
			@Override
			public IStopWords getStopWords() {
				return DefaultStopWords.INSTANCE;
			}
	
			@Override
			public ListofPaths newPathSet(List<? extends Path> paths) {
				return new ListOfPathsComplexStopWordsWeightsEcore(paths, this);
			}
			
		}
	
	public static class justToLowerCaseWithWeigthsPathFactory implements PathFactory {

		@Override
		public IStemmer getStemmer() {
			
			return new IStemmer() {
				
				@Override
				public String stem(String lowerCase) {
					return lowerCase.toLowerCase();
				}
			};
		}

		@Override
		public ITokenizer getTokenizer() {
			return new ITokenizer() {
				
				@Override
				public String[] tokenize(String word) {
					String[] result = {word};
					return result;
				}
			};
		}

		@Override
		public IStopWords getStopWords() {
			return new IStopWords() {
				
				@Override
				public boolean isStopWord(String word) {
					return false;
				}
			};
		}

		@Override
		public ListofPaths newPathSet(List<? extends Path> paths) {
			return new ListOfPathsWeigths(paths, this);
		}
		
		
	}
	
	public static class WSandCCTokenizerSWStemming implements PathFactory {

		@Override
		public IStemmer getStemmer() {
			return new DefaultStemmer();
		}

		@Override
		public ITokenizer getTokenizer() {
			return new WhiteSpaceANDCamelCaseTokenizer();
		}

		@Override
		public IStopWords getStopWords() {
			return DefaultStopWords.INSTANCE;
		}

		@Override
		public ListofPaths newPathSet(List<? extends Path> paths) {
			return new ListofPathsComplexStopWords(paths, this);
		}
		
	}
	
	public static class EcoreTokenizer implements PathFactory {
		
		@Override
		public IStemmer getStemmer() {
			
			return new IStemmer() {
				
				@Override
				public String stem(String word) {
					return word;
				}
			};
		}
		
		@Override
		public ITokenizer getTokenizer() {
			return new WhiteSpaceANDCamelCaseTokenizer();
		}
		
		
		@Override
		public IStopWords getStopWords() {
			return new IStopWords() {
				
				@Override
				public boolean isStopWord(String word) {
					return false;
				}
			};
		}

		@Override
		public ListofPaths newPathSet(List<? extends Path> paths) {
			return new ListofPathsComplexStopWords(paths, this);
		}
		
	}
	
	
	
	
}
