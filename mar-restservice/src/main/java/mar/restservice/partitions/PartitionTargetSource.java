package mar.restservice.partitions;

import java.util.HashMap;

import mar.restservice.IPartition;

public class PartitionTargetSource implements IPartition{
	
	private HashMap<String, Integer> mapClass = new HashMap<String, Integer>();
	
	public PartitionTargetSource() {
		mapClass.put("EClass",0);
		mapClass.put("EAttribute",1);
		mapClass.put("EPackage",2);
		mapClass.put("EReference",3);
		mapClass.put("EDataType",4);
		mapClass.put("EEnum",5);
		mapClass.put("EEnumLiteral",6);
	}
	
	@Override
	public int getNumPartitions() {
		return (mapClass.size() * mapClass.size()) + mapClass.size() + 1;
	}

	@Override
	public int getPartition(String path) {
		int occurrencesCommas = path.length() - path.replace(",", "").length();
		int len = occurrencesCommas / 2;
		
		if (len == 1) {
			String[] parts = path.split(",");
			String v1 = parts[2].substring(0, parts[2].length() - 1);
			if (mapClass.containsKey(v1))
				return mapClass.get(v1);
		} else {
			int pos = mapClass.size();
			String[] parts = path.split(",");
			String sourceP = parts[2];
			String targetP = parts[parts.length - 3];
			for (String source : mapClass.keySet())
				for (String target : mapClass.keySet()) {
					if (sourceP.equals(source) && target.equals(targetP)) {
						pos = pos + mapClass.get(sourceP) * mapClass.size() + mapClass.get(target);
						return pos;
					}
						
				}
		}
		
		return (mapClass.size() * mapClass.size()) + mapClass.size();
	}

}
