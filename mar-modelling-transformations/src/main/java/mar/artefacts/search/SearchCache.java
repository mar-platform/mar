package mar.artefacts.search;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;

public class SearchCache {

	private Map<String, Set<String>> footprintNamesByFile = new HashMap<>();
	private Map<String, List<Path>> filesByExtensionAndProjectName = new HashMap<>();
 	
	@CheckForNull
	public Set<String> getClassNamesOf(File f) {
		return footprintNamesByFile.get(f.getAbsolutePath());
	}

	public void putClassNames(File f, Set<String> names) {
		this.footprintNamesByFile.put(f.getAbsolutePath(), names);
	}

	public void putFilesByExtension(Path projectRoot, String extension, List<Path> result) {
		this.filesByExtensionAndProjectName.put(projectRoot.toString() + "--" + extension, result);
	}

	@CheckForNull
	public List<Path> getFilesByExtension(Path projectRoot, String extension) {
		return this.filesByExtensionAndProjectName.get(projectRoot.toString() + "--" + extension);
	}
}
