package mar.restservice.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;



public class TreeResults {
	private Node root;
	
	public TreeResults(Map<String,List<String>> pathsModels, int depth) {
		this.root = recursiveConstruction(null, pathsModels, depth);
		
	}
	
	
	
	@Override
	public String toString() {
		return "TreeResults [root=" + root + "]";
	}



	private static Node recursiveConstruction(Node father, Map<String,List<String>> pathsModels, int depth) {
		
		int total_models = pathsModels.values().stream().flatMap(l -> l.stream()).collect(Collectors.toSet()).size();
		if ((depth <= 0)) {
			NodeStats ns = new NodeStats(total_models, null);
			Node l = new Node(ns, father, null, pathsModels, true);
			return l;
		} else {
			
			Map<String, Integer> number_models_per_path = pathsModels.entrySet()
					.stream().collect(Collectors.toMap(e -> e.getKey(), e->e.getValue().size()));
			//select split
			Map<String, Double> entropy_per_path = number_models_per_path.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), 
					e ->entropy(e.getValue(),total_models)));
			Entry<String, Double> entry = entropy_per_path.entrySet().stream().max((e1,e2) -> e1.getValue().compareTo(e2.getValue())).get();
			//System.out.println(entry);
			//create split maps
			Map<String,List<String>> new_map_left =  getLeftChild(entry.getKey(), pathsModels);
			Map<String,List<String>> new_map_right =  getRightChild(entry.getKey(), pathsModels);
			
			
			NodeStats ns = new NodeStats(total_models, entry.getKey());
			Node l = new Node(ns, father, new ArrayList<>(), pathsModels, false);
			
			int total_models_left = new_map_left.values().stream().flatMap(ll -> ll.stream()).collect(Collectors.toSet()).size();
			int total_models_right = new_map_right.values().stream().flatMap(ll -> ll.stream()).collect(Collectors.toSet()).size();
			
			
			if (total_models_left!=0)
				l.addChild(recursiveConstruction(l,new_map_left,depth-1));
			if (total_models_right!=0)
				l.addChild(recursiveConstruction(l,new_map_right,depth-1));
			
			if (total_models_left == 0 && total_models_right == 0) {
				l.isLeaf = true;
				l.children = null;
				l.data.path = null;
			}
				
			
			return l;
		}
	}
	
	
	private static Map<String,List<String>> getLeftChild(String path, Map<String,List<String>> total){
		
		Map<String,List<String>> result = cloneMap(total);
		List<String> models = result.get(path);
		result.remove(path);
		for (Entry<String,List<String>> entry: result.entrySet()) {
			List<String> toDel = new ArrayList<String>();
			for (String s : entry.getValue()) {
				if (!models.contains(s)) {
					toDel.add(s);
				}
			}
			for (String s : toDel) {
				entry.getValue().remove(s);
			}
			
		}
		
		return result;
	}
	
	
	private static Map<String,List<String>> getRightChild(String path, Map<String,List<String>> total){
		Map<String,List<String>> result = cloneMap(total);
		List<String> models = result.get(path);
		result.remove(path);
		for (Entry<String,List<String>> entry: result.entrySet()) {
			for (String s : models) {
				entry.getValue().remove(s);
			}
		}
		
		return result;
	}
	
	private static Map<String,List<String>> cloneMap(Map<String,List<String>> original){
		Map<String,List<String>> clone = new HashMap<String, List<String>>();
		for (Entry<String,List<String>> entry: original.entrySet()) {
			clone.put(entry.getKey(), new ArrayList<>());
			for (String s: entry.getValue()) {
				clone.get(entry.getKey()).add(s);
			}
		}
		
		return clone;
		
	}
	
	private static double entropy(int path, int total) {
		double fpath = (double) path;
		double ftotal = (double) total;
		double p1 = fpath/ftotal;
		double p2 = (total - fpath)/ftotal;
		
		if ((p1 == 0) || (p2 == 0))
			return 0.0;
		
		return -(p1 * Math.log(p1)) - (p2 * Math.log(p2)); 
		
	}
	
	
	private static class Node {
		private NodeStats data;
		private Node parent;
		private List<Node> children;
		private Map<String,List<String>> models;
		private boolean isLeaf;

		
		@Override
		public String toString() {
			
			
			
			return "\nNode [data=" + data + ", children=" + children + ", models=" + models
					+ ", isLeaf=" + isLeaf + "]";
		}

		public Node(NodeStats data, Node parent, List<Node> children, 
				Map<String,List<String>> models, boolean isLeaf) {
			super();
			this.data = data;
			this.parent = parent;
			this.children = children;
			this.models = models;
			this.isLeaf = isLeaf;
		}
		
		public void addChild(Node n) {
			children.add(n);
		}
		
		
	}
	
	
	
	private static class NodeStats {
		private int models;
		private String path;

		@Override
		public String toString() {
			return "NodeStats [models=" + models + ", path=" + path + "]";
		}

		public NodeStats(int models, String path) {
			super();
			this.models = models;
			this.path = path;
		}
		
	}
	
	
	

}
