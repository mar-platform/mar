package mar.paths.stemming;


import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class WhiteSpaceANDCamelCaseTokenizer implements ITokenizer{

	public static final WhiteSpaceANDCamelCaseTokenizer INSTANCE = new WhiteSpaceANDCamelCaseTokenizer();
	
	@Override
	public String[] tokenize(String word) {
		
		String word1 = word.replaceAll("[^a-zA-Z0-9]", " ");
		
		String[] tokens = opennlp.tools.tokenize.WhitespaceTokenizer.INSTANCE.tokenize(word1);
		
		List<String> tokenList = new LinkedList<String>();
		
		for (String string : tokens) {
			String[] ts = string.replaceAll(
				      String.format("%s|%s|%s",
						         "(?<=[A-Z])(?=[A-Z][a-z])",
						         "(?<=[^A-Z])(?=[A-Z])",
						         "(?<=[A-Za-z])(?=[^A-Za-z])"
						      ),
						      " "
						   ).split("\\s+");
			
			tokenList.addAll(Arrays.asList(ts));
			
		}
		
		List<String> rl = tokenList.stream().map(s -> s.toLowerCase()).collect(Collectors.toList());
		
		return rl.toArray((new String[rl.size()]));
	}

}
