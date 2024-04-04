package mar.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class Utils {

	public static boolean isNullOrEmpty(@CheckForNull String s) {
		return s == null || s.isEmpty();
	}

	public static String replaceEnv(@Nonnull String path, @Nonnull Map<String, String> env) {
		Pattern pattern = Pattern.compile("\\$\\((.+)\\)");
		Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
        	String var = matcher.group(1);
        	String sub = env.get(var);
        	if (sub == null)
        		throw new RuntimeException("No variable: " + var);
        	path = matcher.replaceFirst(sub);
        	matcher.reset(path);
        }
		return path;
	}
	
}
