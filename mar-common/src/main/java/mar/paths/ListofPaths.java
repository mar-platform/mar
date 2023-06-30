package mar.paths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import opennlp.tools.stemmer.PorterStemmer;

public abstract class ListofPaths {
	
	@NonNull
	protected final List<? extends Path> listofpaths;
	protected final PathFactory factory;
		
	public ListofPaths(@NonNull List<? extends Path> paths, @Nonnull PathFactory factory) {
		this.listofpaths = paths;
		this.factory = factory;
	}	
	
    public abstract PartitionedPathMap  toMapParticionedPaths();
	
	public Map<String, Integer> toMapPaths() {
		HashMap<String, Integer> result = new HashMap<>();
		PorterStemmer ps = new PorterStemmer();
		
		for (Path path : listofpaths) {
			String str = "(";
			String separator = "";
			for (PathNode node : path.getPath()) {
				String toadd;
				if (node.getNodetype() == NodeType.ATTRIBUTE_VALUE_STRING) 
					toadd = ps.stem(node.getValue().toLowerCase()) + ",";
				else if (node.getNodetype() == NodeType.ATTRIBUTE_VALUE_OTHER)
					toadd = node.getValue();
				else
					toadd = node.getValue();						
						
				str += separator + toadd;
				separator = ",";
			}
			str += ")";
			
			int v = result.computeIfAbsent(str, (k) -> 0);
			result.put(str, v + 1);
		}		
		
		return result;
	}

}
