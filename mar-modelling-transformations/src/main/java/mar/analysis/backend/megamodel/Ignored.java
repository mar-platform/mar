package mar.analysis.backend.megamodel;

import java.nio.file.Path;

public class Ignored {

	private final Path path;
	private final String cause;
	private String id;

	public Ignored(Path path, String cause) {
		this.path = path;
		this.cause = cause;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public Path getPath() {
		return path;
	}
	
	public String getCause() {
		return cause;
	}
}
