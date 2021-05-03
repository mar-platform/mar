package mar.paths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.text.DefaultEditorKit.BeepAction;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.paths.stemming.IStemmer;

public class ListOfPathsWeightsEcore extends ListofPaths{
	
	public ListOfPathsWeightsEcore(@NonNull List<? extends Path> paths, @Nonnull PathFactory factory) {
		super(paths, factory);
	}
	
	@Override
	public PartitionedPathMap toMapParticionedPaths() {
		
		PartitionedPathMap result = new PartitionedPathMap();
		IStemmer ps = factory.getStemmer();
		
		for (Path path : listofpaths) {
			boolean betweenStrings = path.betweenStrings();
			
			int points = 1;
			
			if (betweenStrings)
				points = points + 10;
			
			if (isNameClassPath(path))
				points = points + 20;
			
			String toadd = "(";
			for (PathNode node : path.getPath()) {
				if (node.getNodetype() == NodeType.ATTRIBUTE_VALUE_STRING) 
					toadd = toadd + ps.stem(node.getValue()) + ",";
				else if (node.getNodetype() == NodeType.ATTRIBUTE_VALUE_OTHER)
					toadd = toadd + node.getValue() + ",";
				else
					toadd = toadd + node.getValue() + ",";
			}
			
			toadd = toadd.substring(0, toadd.length() - 1) + ")";

			String commom = "";
			
			if (path.getPath().get(0).getNodetype() == NodeType.ATTRIBUTE_VALUE_STRING) {
				commom = "(" + ps.stem(path.getPath().get(0).getValue()) +"," + path.getPath().get(1).getValue() +
						"," + path.getPath().get(2).getValue();
			}else if ((path.getPath().get(0).getNodetype() == NodeType.ATTRIBUTE_VALUE_OTHER)) {
				commom = "(" + path.getPath().get(0).getValue() +"," + path.getPath().get(1).getValue() +
						"," + path.getPath().get(2).getValue();
			}
			
			else {
				commom = "(" + path.getPath().get(0).getValue();
			}

			
			toadd = toadd.substring(commom.length(), toadd.length());
			
			if (result.containsKey(commom)) {
				HashMap<String, Integer> hm = (HashMap<String, Integer>) result.get(commom);
				if (hm.containsKey(toadd)) {
					int value = points + hm.get(toadd);
					hm.put(toadd, value);
				} else {	
					hm.put(toadd, points);	
				}
			} else {
				HashMap<String, Integer> hm = new HashMap<String, Integer>();
				hm.put(toadd, points);
				result.put(commom, hm);
			}
			
		}
		return result;
	}
	
	public boolean isNameClassPath(Path p) {
		int len = p.numberOfNodes();
		if (len!= 3)
			return false;
		
		List<PathNode> lpn = p.getPath();
		
		if (!lpn.get(1).getValue().equals("name"))
			return false;
		
		if (!lpn.get(2).getValue().equals("EClass"))
			return false;
		
		return true;
		
		
	}
}
