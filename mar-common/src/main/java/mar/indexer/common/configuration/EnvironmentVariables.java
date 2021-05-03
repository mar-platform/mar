package mar.indexer.common.configuration;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class EnvironmentVariables {

	public static enum MAR {
		REPO_MAR ("REPO_MAR"),
		REPO_ROOT ("REPO_ROOT"),
		INDEX_TARGET ("INDEX_TARGET");
		
		private final String name;

		MAR (String name) {
			this.name = name;
		}
	}
	
	@Nonnull
	public static String getVariable(MAR variable) {
		String value = getVariableOrNull(variable);
		if (value == null)
			throw new IllegalStateException("No variable " + variable);
		return value;
	}
	
	@CheckForNull
	public static String getVariableOrNull(MAR variable) {
		return System.getenv(variable.name);
	}
	
	
	
}
