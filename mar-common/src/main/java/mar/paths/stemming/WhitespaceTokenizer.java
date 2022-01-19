package mar.paths.stemming;

public class WhitespaceTokenizer implements ITokenizer {

	@Override
	public String[] tokenize(String word) {
		return opennlp.tools.tokenize.WhitespaceTokenizer.INSTANCE.tokenize(word.replaceAll("[^a-zA-Z0-9]", " "));
	}

	
	
}
