package mar.paths.stemming;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface ITokenizer {

	@NonNull
	String[] tokenize(@NonNull String word); 
	
}
