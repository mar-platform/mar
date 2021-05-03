package mar.paths.stemming;

import javax.annotation.Nonnull;

/**
 * Tokenizes based on camel case, it is copied from:
 *  
 * 		https://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java 
 * 
 * See test case CamelCaseTokenizerTest.
 *  
 * It also handles spaces, but it is probably not as good as {@link WhitespaceTokenizer} for that.
 *  
 * @author jesus
 *
 */
public class CamelCaseTokenizer implements ITokenizer {

	public static final CamelCaseTokenizer INSTANCE = new CamelCaseTokenizer();

	@Override
	public String[] tokenize(@Nonnull String word) {	
	   String[] result = (word.replaceAll("[^a-zA-Z0-9]", " ")).replaceAll(
			      String.format("%s|%s|%s",
					         "(?<=[A-Z])(?=[A-Z][a-z])",
					         "(?<=[^A-Z])(?=[A-Z])",
					         "(?<=[A-Za-z])(?=[^A-Za-z])"
					      ),
					      " "
					   ).split("\\s+");
	   
	   String[] newArray = new String[result.length];
	   int i = 0;
	   for (String string : result) {
		   newArray[i] = string.toLowerCase();
		   i++;
	}
	   return newArray;
	}

}
