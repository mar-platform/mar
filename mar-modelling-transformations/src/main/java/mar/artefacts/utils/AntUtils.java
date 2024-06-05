package mar.artefacts.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import mar.artefacts.RecoveredPath;

public class AntUtils {

	public static RecoveredPath.Ant parseAntPath(Path buildFileFolder, Path basedir, String file) {
		String result = "";
		boolean loosyFilePath = false;
		
		// Try to remove variables
		String[] parts = getFileParts(file);
		
		if (parts.length == 1 && !parts[0].contains("{")) {
			// TODO: Check if the file exists??
			Path p = buildFileFolder.resolve(file);
			return new RecoveredPath.Ant(p, true);
		}
		
		String separator = "";
		for (String p : parts) {
			if (p.equals("${basedir}"))
				p = basedir.toString();
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

	public static String parseAntPath2(Path buildFileFolder, Path basedir, String file) {
		String result = "";
		
		// Try to remove variables
		String[] parts = getFileParts(file);
		
		if (parts.length == 1 && !parts[0].contains("{")) {
			// TODO: Check if the file exists??
			Path p = buildFileFolder.resolve(file);
			return p.toString();
		}
		
		String separator = "";
		for (String p : parts) {
			if (p.equals("${basedir}"))
				p = basedir.toString();
			if (p.contains("{")) {
				result = "";
				continue;
			}
			result = result + separator + p;
			separator = File.separator;
		}	
		
		return result;
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
