package mar.paths;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class PathParser {

	public static final PathParser INSTANCE = new PathParser();
	
	private final Map<Integer, Integer> pathLengths = new HashMap<>();
	
	public PathParser() {
		for(int pathLen = 1; pathLen < 10; pathLen++) {
			pathLengths.put(3 * pathLen + (pathLen - 1), pathLen);
		}
	}
	
	public String[] toAttributeValues(String path) {
		String[] parts = path.substring(1, path.length() - 1).split(",");
		int numElements = parts.length;
		
		// Case 1:
		// (unary,name,EEnum)
		if (numElements == 3) {
			return new String[] { parts[0] };
		}
		
		// Case 2:
		// (expression,name,EClass,ePackage,EPackage,name,expressions)
		// Note that after ePackage the property value is in the third position of the triple: "expressions"
		
		// Formula to calculate the number of elements from the path length: 3 * pathLen + (pathLen - 1)
		int pathLength = pathLengths.get(numElements);	
		String[] values = new String[pathLength];
		
		values[0] = parts[0];
		for(int i = 1; i < pathLength; i++) {
			String value = parts[i * 3 + i + 2];
			values[i] = value;
		}
		
		return values;
	}

	
	public String[] toAttributeNameValuePath(String path) {
		String[] parts = path.substring(1, path.length() - 1).split(",");
		int numElements = parts.length;
		
		// Case 1:
		// (unary,name,EEnum)
		if (numElements == 3) {
			return new String[] { parts[0], parts[1] };
		}
		
		// Case 2:
		// (expression,name,EClass,ePackage,EPackage,name,expressions)
		// Note that after ePackage the property value is in the third position of the triple: "expressions"
		
		// Formula to calculate the number of elements from the path length: 3 * pathLen + (pathLen - 1)
		int pathLength = pathLengths.get(numElements);
		int numWords = pathLength * 2;
		String[] values = new String[numWords];
		
		values[0] = parts[0];
		values[1] = parts[1];
		
		int j = 2;
		for(int i = 1; i < pathLength; i++) {
			String value1 = parts[i * 3 + i + 1];
			String value2 = parts[i * 3 + i + 1 + 1];
			
			values[j] = value2;
			values[j + 1] = value1;
			
			j = j + 2;
		}
		
		return values;
	}

	public String[] toFullPath(String path) {
		String[] parts = path.substring(1, path.length() - 1).split(",");
		return parts;		
	}
	
	public int getPathSize(String path) {
		int parts = StringUtils.countMatches(path, ",") + 1;
		return pathLengths.get(parts);
	}

	
}
