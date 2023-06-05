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

	@JsonProperty(required = false, value = "content_filters")
	private List<ContentFilter> contentFilters = new ArrayList<>();

	@JsonProperty(required = false)
	private List<IgnoredFile> ignore = new ArrayList<>();

	@JsonProperty(required = false)
	private List<Expectation> expectations = new ArrayList<>();
	
	public static AnalyserConfiguration read(File f) throws IOException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		return mapper.reader().readValue(f, AnalyserConfiguration.class);
	}
	
	public static class ContentFilter {
		@JsonProperty
		private String extension;
		
		@JsonProperty
		private String contains;
		
		public String getContains() {
			return contains;
		}
	}
	
	public static class IgnoredFile {
		@JsonProperty(required = false)
		private String type = "unknown";
		
		@JsonProperty
		private String pattern;		
	}

	public static class Expectation {
		@JsonProperty
		private String check;
		@JsonProperty
		private String file;
	}
	
	public boolean isIgnored(Path filePath) {
		for (IgnoredFile ignoredFile : ignore) {
			if (filePath.startsWith(ignoredFile.pattern))
				return true;
		}
		return false;
	}
	
	public List<ContentFilter> getFilters(String extension) {
		List<ContentFilter> filters = new ArrayList<AnalyserConfiguration.ContentFilter>();
		for (ContentFilter contentFilter : contentFilters) {
			if (contentFilter.extension.equals(extension))
				filters.add(contentFilter);
		}
		return filters;
	}
	
}
