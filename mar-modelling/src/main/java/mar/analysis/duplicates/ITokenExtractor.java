package mar.analysis.duplicates;

import java.util.List;

import javax.annotation.Nonnull;

public interface ITokenExtractor<T> {

	@Nonnull
	List<String> extract(@Nonnull T resource);

	default void addToken(List<String> tokens, String string) {
		if (string != null)
			tokens.add(string.toLowerCase());
	}
}
