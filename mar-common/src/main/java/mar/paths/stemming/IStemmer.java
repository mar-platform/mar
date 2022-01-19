package mar.paths.stemming;

@FunctionalInterface
public interface IStemmer {

	String stem(String lowerCase);

	public static final IStemmer IDENTITY = (s) -> s;
}
