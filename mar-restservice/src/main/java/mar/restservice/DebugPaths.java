package mar.restservice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DebugPaths {
	
	private HashMap<String,List<String>> docs_paths = new HashMap<String,List<String>>();
	
	public DebugPaths() {
		
	}
	
	public void addDocPath(String doc, String path) {
		if (docs_paths.containsKey(doc)) {
			docs_paths.get(doc).add(path);
		}else {
			LinkedList<String> ls = new LinkedList<String>();
			ls.add(path);
			docs_paths.put(doc, ls);
		}
	}
	
	public void print() {
		Set<Entry<String,List<String>>> se = docs_paths.entrySet();
		String doc ="";
		int best_size = -1;
		for (Entry<String, List<String>> entry : se) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
			if (entry.getValue().size() > best_size) {
				best_size = entry.getValue().size();
				doc = entry.getKey();
			}
			
		}
		System.out.println(doc);
		System.out.println(docs_paths.get(doc));
	}

}
