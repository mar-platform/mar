package mar.paths.stemming;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface IStopWords {

	boolean isStopWord(@Nonnull String word);
	
}
