package mar.utils;

import javax.annotation.CheckForNull;

public class Utils {

	public static boolean isNullOrEmpty(@CheckForNull String s) {
		return s == null || s.isEmpty();
	}
	
}
