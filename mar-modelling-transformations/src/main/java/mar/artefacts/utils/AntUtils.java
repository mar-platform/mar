package mar.artefacts.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import mar.artefacts.RecoveredPath;

public class AntUtils {

	public static RecoveredPath.Ant parseAntPath(Path buildFileFolder, String file) {
		String result = "";
		boolean loosyFilePath = false;
		
		// Try to remove variables
		String[] parts = getFileParts(file);
		String separator = "";
		for (String p : parts) {
			if (p.equals("${basedir}"))
				p = buildFileFolder.toString();
			if (p.contains("{")) {
				// The goal here is to generate only a partial path -> perhaps mark this somehow?
				loosyFilePath = true;
				result = "";
				continue;
			}
			result = result + separator + p;
			separator = File.separator;
		}	
		
		return new RecoveredPath.Ant(Paths.get(result), loosyFilePath);
	}

	private static String[] getFileParts(String file) {
		String unixLikePath = file.replaceAll("\\\\", "/");
		String[] parts = unixLikePath.split("/");
		return parts;
	}

	public static String stripUnknownElements(String file) {
		String result = "";
		// Try to remove variables
		String[] parts = getFileParts(file);
		String separator = "";
		for (String p : parts) {
			if (p.contains("{")) {
				result = "";
				continue;
			}
			result = result + separator + p;
			separator = File.separator;
		}	
		return result;
	}
	
}
