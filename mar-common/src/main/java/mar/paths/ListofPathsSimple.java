package mar.paths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.paths.stemming.IStemmer;
import opennlp.tools.stemmer.PorterStemmer;

public class ListofPathsSimple extends ListofPaths{

	public ListofPathsSimple(@NonNull List<? extends Path> paths, @Nonnull PathFactory factory) {
		super(paths, factory);
	}

	@Override
	public PartitionedPathMap  toMapParticionedPaths() {
		
		PartitionedPathMap result = new PartitionedPathMap();
		IStemmer ps = factory.getStemmer();
		
		for (Path path : listofpaths) {
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
					int value = 1 + hm.get(toadd);
					hm.put(toadd, value);
				} else {
					hm.put(toadd, 1);
				}
			} else {
				HashMap<String, Integer> hm = new HashMap<String, Integer>();
				hm.put(toadd, 1);
				result.put(commom, hm);
			}
			
		}
		return result;
	}

}
