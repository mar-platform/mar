package mar.restservice.partitions;

import org.apache.commons.lang3.StringUtils;

import mar.restservice.IPartition;

public class PartitionByNames implements IPartition{

	@Override
	public int getNumPartitions() {
		return 9;
	}

	@Override
	public int getPartition(String path) {
		int occurrencesCommas = path.length() - path.replace(",", "").length();
		int len = occurrencesCommas / 2;
		
		switch (len) {
		case 1:
			String[] parts = path.split(",");
			if (parts[1].equals("name"))
				return 0;
			return 1;

		case 2:
			if (StringUtils.countMatches(path,",name,")==2)
				 return 2;
			 return 3;
		case 3:
			if (StringUtils.countMatches(path,",name,")==2)
				 return 4;
			 return 5;
		case 4:
			if (StringUtils.countMatches(path,",name,")==2)
				 return 6;
			 return 7;
		default:
			return 8;
		}
		
//		if (len == 1) {
//			String[] parts = path.split(",");
//			if (parts[1].equals("name"))
//				return 0;
//			return 1;
//		} else {
//			 if (StringUtils.countMatches(path,",name,")==2)
//				 return 2;
//			 return 3;
//		}
	}

}
