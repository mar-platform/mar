package mar.indexer.embeddings;

import java.util.List;

import javax.annotation.CheckForNull;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class VectorizedPath implements Embeddable {

	private int seqId;
	private float[] vector;
	private IntArrayList pathIds = new IntArrayList();
	private String pathText;
	
	public VectorizedPath(int seqId, float[] vector) {
		this.seqId = seqId;
		this.vector = vector;
	}
	
	public VectorizedPath(int seqId, float[] vector, String pathText) {
		this(seqId, vector);
		this.pathText = pathText;
	}
	
	@Override
	public int getSeqId() {
		return seqId;
	}
	
	public float[] getVector() {
		return vector;
	}
	
	@Override
	public List<? extends String> getWords() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isAlreadyEmbedded() {
		return true;
	}

	public void addPathId(int pathId) {
		pathIds.add(pathId);
	}
	
	public int[] getPathIds() {
		return pathIds.toIntArray();
	}
	
	public String toVectorString() {
		StringBuilder builder = new StringBuilder(pathIds.size() * 2);
		String separator = "";
		for(int i = 0, len = vector.length; i < len; i++) {
			float f = vector[i];
			builder.append(separator);
			builder.append(f);
			separator = " ";
		}
		return builder.toString();		
	}
	
	public String toPathIdsString() {
		StringBuilder builder = new StringBuilder(pathIds.size() * 2);
		String separator = "";
		for(int i = 0, len = pathIds.size(); i < len; i++) {
			int pathId = pathIds.getInt(i);
			builder.append(separator);
			builder.append(pathId);
			separator = " ";
		}
		return builder.toString();
	}

	public static int[] fromPathIdsStrings(String paths) {
		String[] parts = paths.split(" ");
		int nums[] = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			nums[i] = Integer.parseInt(parts[i]);
		}
		return nums;
	}

	@CheckForNull
	public String getPathText() {
		return pathText;
	}
}

