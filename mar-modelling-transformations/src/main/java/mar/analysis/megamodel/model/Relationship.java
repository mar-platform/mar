package mar.analysis.megamodel.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Relationship {
	
	TYPED_BY("typed-by"), 
	IMPORT("import"), 
	DUPLICATE("duplicate"),
	GENERATE("duplicate"),	
	
	BUILD_DUPLICATE("build_duplicate"), // Specialization of duplicate for projects
	
	PROJECT_RELATED_TO("project-to-project"),
	INPUT_TYPE("input-type"),   /* From meta-model to transformation */
	OUTPUT_TYPE("output-type"), /* From transformation to meta-model */
	HEURISTIC("heuristic");
	
	private static Map<String, Relationship> byKind = Arrays.stream(Relationship.values()).collect(Collectors.toMap(k -> k.getKind(), k -> k));

	private String kind;

	Relationship(String kind) {
		this.kind = kind;
	}
	
	@JsonValue
	public String getKind() {
		return kind;
	}
	
	@JsonCreator
	public static Relationship getByKind(String kind) {
		return byKind.get(kind);
	}
	
}
