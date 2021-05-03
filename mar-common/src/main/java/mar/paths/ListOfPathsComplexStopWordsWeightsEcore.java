package mar.paths;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.paths.stemming.IStopWords;
import mar.paths.stemming.ITokenizer;

public class ListOfPathsComplexStopWordsWeightsEcore extends ListofPaths{
	public ListOfPathsComplexStopWordsWeightsEcore(@NonNull List<? extends Path> lps, @NonNull PathFactory factory) {
		super(lps, factory);

	}
	
	@Override
	public PartitionedPathMap toMapParticionedPaths() {
		ITokenizer tokenizer = factory.getTokenizer(); 
		IStopWords sw = factory.getStopWords();
		
		List<Path> newpaths = new LinkedList<Path>();
		for (Path path : listofpaths) {
			PathNode f = path.getFirst();
			PathNode l = path.getLast();
			LinkedList<PathNode> lpath = new LinkedList<PathNode>(path.getPath());
			if (f.getNodetype() == NodeType.ATTRIBUTE_VALUE_STRING && l.getNodetype() == NodeType.ATTRIBUTE_VALUE_STRING) {
				String fstr = f.getValue();
				String lstr = l.getValue();
				//we have to remove stop words !!! TO DO
				String ftokens[] = tokenizer.tokenize(fstr); 
				String ltokens[] = tokenizer.tokenize(lstr); 
				lpath.removeFirst();
				lpath.removeLast();
				
				for (String sf : ftokens) {
					if (sw.isStopWord(sf))
						continue;
					for (String sl : ltokens) {
						if (sw.isStopWord(sl))
							continue;
						PathNode nf = new PathNode(sf, NodeType.ATTRIBUTE_VALUE_STRING);
						PathNode nl = new PathNode(sl, NodeType.ATTRIBUTE_VALUE_STRING);
						LinkedList<PathNode> newl = new LinkedList<PathNode>(lpath);
						newl.addFirst(nf);
						newl.addLast(nl);
						Path p = new Path(newl);
						newpaths.add(p);
					}
				}
				
				
			} else if (f.getNodetype() == NodeType.ATTRIBUTE_VALUE_STRING) {
				lpath.removeFirst();
				String fstr = f.getValue();
				String ftokens[] = tokenizer.tokenize(fstr); 
				for (String sf : ftokens) {
					if (sw.isStopWord(sf))
						continue;
					PathNode nf = new PathNode(sf, NodeType.ATTRIBUTE_VALUE_STRING);
					LinkedList<PathNode> newl = new LinkedList<PathNode>(lpath);
					newl.addFirst(nf);
					Path p = new Path(newl);
					newpaths.add(p);
				}
				
				
				
			} else if (l.getNodetype() == NodeType.ATTRIBUTE_VALUE_STRING) {
				lpath.removeLast();
				String lstr = l.getValue();
				String ltokens[] = tokenizer.tokenize(lstr); 
				for (String sl : ltokens) {
					if (sw.isStopWord(sl.toLowerCase()))
						continue;
					PathNode nl = new PathNode(sl, NodeType.ATTRIBUTE_VALUE_STRING);
					LinkedList<PathNode> newl = new LinkedList<PathNode>(lpath);
					newl.addLast(nl);
					Path p = new Path(newl);
					newpaths.add(p);
				}
				
			} else {
				LinkedList<PathNode> newl = new LinkedList<PathNode>(lpath);
				Path p = new Path(newl);
				newpaths.add(p);
				
			}	
			
		}
		//convert to a simple path
		ListOfPathsWeightsEcore lps = new ListOfPathsWeightsEcore(newpaths, factory);
		return lps.toMapParticionedPaths();
	}
}
