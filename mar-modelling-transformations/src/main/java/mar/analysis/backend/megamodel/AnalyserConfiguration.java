package mar.analysis.backend.megamodel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Represents a configuration, normally stored in a YAML file,
 * 
 * @author jesus
 *
 */
public class AnalyserConfiguration {
	
	@JsonProperty(required = false)
	private List<IgnoredFile> ignore = new ArrayList<>();
	
	public static AnalyserConfiguration read(File f) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.reader().readValue(f, AnalyserConfiguration.class);
	}
	
	public static class IgnoredFile {
		@JsonProperty(required = false)
		private String type = "unknown";
		
		@JsonProperty
		private String pattern;		
	}

	public boolean isIgnored(Path filePath) {
		for (IgnoredFile ignoredFile : ignore) {
			if (filePath.startsWith(ignoredFile.pattern))
				return true;
		}
		return false;
	}
	
}
