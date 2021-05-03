package mar.restservice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

public class CountScoreCalculator {
	
	private final Object2DoubleMap<String> scores = new Object2DoubleOpenHashMap<String>(1024);
	private final HashMap<String,List<Double>> divided_scores = new HashMap<String,List<Double>>();
	private final HashMap<String,Integer> association = new HashMap<String, Integer>();
	
	public CountScoreCalculator() {
		association.put("EClass-EClass",0);
		association.put("EReference-EClass",1);
		association.put("EAttribute-EClass",2);
		association.put("EPackage-EClass",3);
		association.put("EDataType-EClass",4);
		
		association.put("EReference-EReference",5);
		association.put("EAttribute-EReference",6);
		association.put("EPackage-EReference",7);
		association.put("EDataType-EReference",8);
		
		association.put("EAttribute-EAttribute",9);
		association.put("EPackage-EAttribute",10);
		association.put("EDataType-EAttribute",11);
		
		association.put("EPackage-EPackage",12);
		association.put("EDataType-EPackage",13);
		
		association.put("EDataType-EDataType",14);
		
		//one class
		
		association.put("EClass",15);
		association.put("EReference",16);
		association.put("EAttribute",17);
		association.put("EPackage",18);
		association.put("EDataType",19);
		
		
		
	}
	
	public void addPath(String path, String docName) {
		double old_score = scores.computeDoubleIfAbsent(docName, (k) -> 0.0d);
		double new_score = old_score + 1;
		scores.put(docName, new_score);
		
		int occurrencesCommas = path.length() - path.replace(",", "").length();
		int len = occurrencesCommas / 2;
		
		List<Double> list = null;
		if (divided_scores.containsKey(docName)) {
			list = divided_scores.get(docName);
		} else {
			list = new LinkedList<Double>();
			list.add(0.);
			for (int i = 0; i < (association.size()); ++i) {
				list.add(0.);
			}
			divided_scores.put(docName, list);
		}
		
		Double allPaths = list.get(0);
		list.set(0, allPaths + 1);
		int pos = 1;
		if (len == 1) {
			for (String ass : association.keySet()) {
				if (path.contains(ass +")")) {
					list.set(pos + association.get(ass), list.get(pos + association.get(ass))+ 1);
					return;
				}
			}
		} else {
			for (String ass : association.keySet()) {
				
				if (ass.contains("-")) {
					String[] vs = ass.split("-");
					if (vs[0].equals(vs[1]) && (org.apache.commons.lang3.StringUtils.countMatches(path, vs[0])==2)) {
						pos = pos +  association.get(ass);
						list.set(pos , list.get(pos)+ 1);
						return;
					} else if (path.contains(vs[0]) && path.contains(vs[1])) {
						pos = pos +  association.get(ass);
						list.set(pos , list.get(pos)+ 1);
						return;
					}
				}
				
				
//				for (String ass2 : association.keySet()) {
//					if (ass.equals(ass2) && (org.apache.commons.lang3.StringUtils.countMatches(path, ass)==2)) {
//						pos = pos + association.size()*association.get(ass) + association.get(ass2);
//						list.set(pos , list.get(pos)+ 1);
//						return;
//					} else if (path.contains(ass) && path.contains(ass2)) {
//						pos = pos + association.size()*association.get(ass) + association.get(ass2);
//						list.set(pos , list.get(pos)+ 1);
//						return;
//					}
//				}
			}
		}
	}
	
	@NonNull
	public Map<String, Double> getScores() {
		return scores;
	}
	
	@NonNull
	public Map<String,List<Double>> getPartitionedScores(){
		return divided_scores;
	}

}
