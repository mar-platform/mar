package mar.paths.stemming;

import javax.annotation.Nonnull;

public interface IStopWords {

	boolean isStopWord(@Nonnull String word);
	
}
