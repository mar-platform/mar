package mar.paths.stemming;

import edu.umd.cs.findbugs.annotations.NonNull;

@FunctionalInterface
public interface ITokenizer {
	
	@NonNull
	String[] tokenize(@NonNull String word); 

	public static final ITokenizer IDENTITY = (s) -> new String[] { s }; 
}
