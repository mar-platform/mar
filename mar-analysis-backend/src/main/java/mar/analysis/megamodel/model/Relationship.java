package mar.analysis.megamodel.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Relationship {
	
	TYPED_BY("typed-by"), 
	IMPORT("import"), 
	DUPLICATE("duplicate");

	private static Map<String, Relationship> byKind = Arrays.stream(Relationship.values()).collect(Collectors.toMap(k -> k.getKind(), k -> k));

	private String kind;

	Relationship(String kind) {
		this.kind = kind;
	}
	
	public String getKind() {
		return kind;
	}
	
	public static Relationship getByKind(String kind) {
		return byKind.get(kind);
	}
	
}
