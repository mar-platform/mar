package mar.paths.stemming;

import javax.annotation.Nonnull;

import opennlp.tools.stemmer.PorterStemmer;

public class DefaultStemmer implements IStemmer {
	private PorterStemmer ps = new PorterStemmer();
	
	@Override
	public String stem(@Nonnull String word) {
		return ps.stem(word);
	}

}
