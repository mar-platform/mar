package ml2.mar.webserver.configuration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnalysisConfiguration(@JsonProperty("filter-projects") List<Project> projects) {
	
	public static record Project(String name, String cause) {
		
	}
}
