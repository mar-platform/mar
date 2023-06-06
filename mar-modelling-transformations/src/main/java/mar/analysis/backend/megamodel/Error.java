package mar.analysis.backend.megamodel;

public class Error {

	private final String id;
	private final String type;
	private final String cause;
	
	public Error(String id, String type, String cause) {
		this.id = id;
		this.type = type;
		this.cause = cause;
	}
	
	public String getId() {
		return id;
	}
	
	public String getCause() {
		return cause;
	}
	
	public String getType() {
		return type;
	}
	
}