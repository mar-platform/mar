package mar.restservice.partitions;

import java.util.HashMap;

import mar.restservice.IPartition;

public class PartitionBySource implements IPartition{
	
	private HashMap<String,Integer> mapSources = new HashMap<String,Integer>();
	
	public PartitionBySource() {
		mapSources.put("EClass",0);
		mapSources.put("EAttribute",1);
		mapSources.put("EPackage",2);
		mapSources.put("EReference",3);
		mapSources.put("EDataType",4);
		mapSources.put("EEnum",5);
		mapSources.put("EEnumLiteral",6);
//		mapSources.put("EOperation",7);
//		mapSources.put("EParameter",8);

	}

	@Override
	public int getNumPartitions() {
		return mapSources.size() + 1;
	}

	@Override
	public int getPartition(String path) {
		//check len this is incorrect
		String[] parts = path.split(",");
		String v1 = parts[2];
		String v2 = parts[0].substring(1);
		if (mapSources.containsKey(v2))
			return mapSources.get(v2);
		if (mapSources.containsKey(v1))
			return mapSources.get(v1);
		return mapSources.size();
	}
	
	
	
}
