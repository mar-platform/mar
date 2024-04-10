package mar.paths;

import java.util.ArrayList;
import java.util.List;

public class Path {
	
	private List<PathNode> path;
	
	public Path() {
		path = new ArrayList<PathNode>();
	}
	
	public Path(List<PathNode> path) {
		super();
		this.path = path;
	}

	public List<PathNode> getPath() {
		return path;
	}

	public void setPath(List<PathNode> path) {
		this.path = path;
	}
	
	public void addNode(PathNode pn) {
		path.add(pn);
	}
	
	public PathNode getFirst() {
		return path.get(0);
	}
	
	public PathNode getLast() {
		return path.get(path.size()-1);
	}
	
	public boolean betweenStrings() {
		return getFirst().getNodetype().equals(NodeType.ATTRIBUTE_VALUE_STRING) 
				&& getLast().getNodetype().equals(NodeType.ATTRIBUTE_VALUE_STRING);
	}
	
	public int numberOfNodes() {
		return path.size();
	}

	
	
	
}
