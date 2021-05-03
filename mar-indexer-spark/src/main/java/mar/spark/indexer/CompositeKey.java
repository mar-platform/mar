package mar.spark.indexer;

import java.io.Serializable;

public class CompositeKey implements Comparable<CompositeKey>, Serializable {
	
	private String part1;
	
	private String part2;
	
	public CompositeKey() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((part1 == null) ? 0 : part1.hashCode());
		result = prime * result + ((part2 == null) ? 0 : part2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositeKey other = (CompositeKey) obj;
		if (part1 == null) {
			if (other.part1 != null)
				return false;
		} else if (!part1.equals(other.part1))
			return false;
		if (part2 == null) {
			if (other.part2 != null)
				return false;
		} else if (!part2.equals(other.part2))
			return false;
		return true;
	}
	
	public String getPart1() {
		return part1;
	}

	public void setPart1(String part1) {
		this.part1 = part1;
	}

	public String getPart2() {
		return part2;
	}

	public void setPart2(String part2) {
		this.part2 = part2;
	}

	@Override
	public int compareTo(CompositeKey arg0) {
		String p1 = arg0.part1;
		String p2 = arg0.part2;
		
		if (this.part1.compareTo(p1) == 0) {
			return this.part2.compareTo(p2);
		} else {
			return this.part1.compareTo(p1);
		}
	}

}
