package mar.paths;

public class PathNode {
	private String value;
	private NodeType nodetype;

	public PathNode(String value, NodeType nodetype) {
		super();
		this.value = value;
		this.nodetype = nodetype;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public NodeType getNodetype() {
		return nodetype;
	}

	public void setNodetype(NodeType nodetype) {
		this.nodetype = nodetype;
	}
	
	
}
