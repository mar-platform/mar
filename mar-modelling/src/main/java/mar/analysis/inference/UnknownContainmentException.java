package mar.analysis.inference;

import org.eclipse.emf.ecore.EClass;

@SuppressWarnings("serial")
public class UnknownContainmentException extends RuntimeException {

	private String name;
	private EClass parent;
	private int line;
	private int column;

	public UnknownContainmentException(String name, EClass parent, int line, int column) {
		super("Unknown containment feature: " + name);
		this.name = name;
		this.parent = parent;
		this.line = line;
		this.column = column;
	}

	public int getLine() {
		return line;
	}
	
	public int getColumn() {
		return column;
	}
	
	public String getName() {
		return name;
	}
	
	public EClass getParent() {
		return parent;
	}
}
