package mar.restservice.partitions;

import mar.restservice.IPartition;

public class PartitionByLen implements IPartition{
	
	private int maxLen;

	public PartitionByLen(int maxLen) {
		super();
		this.maxLen = maxLen;
	}

	@Override
	public int getNumPartitions() {
		return maxLen;
	}

	@Override
	public int getPartition(String path) {
		int occurrencesCommas = path.length() - path.replace(",", "").length();
		int len = occurrencesCommas / 2;
		return len - 1;
	}

}
