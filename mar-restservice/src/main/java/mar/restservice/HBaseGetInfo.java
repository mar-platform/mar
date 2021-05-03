package mar.restservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.restservice.model.IModelResult;
import mar.validation.AnalysisMetadataDocument;

/**
 * Obtains information about a set of models (given its ids)
 */
public class HBaseGetInfo extends HBaseModelAccessor {
	private final static byte[] DOC = "information".getBytes();
	private final static byte[] METADATA = "metadataDocument".getBytes();

	public void updateInformation(@Nonnull List<? extends IModelResult> models) throws IOException {
		Table docs = getDocsInfo();
		
		Map<String, IModelResult> byId = new HashMap<String, IModelResult>();
		
	    List<Get> gets = new ArrayList<Get>(models.size());
		for (IModelResult model: models) {
	    	Get get = new Get(model.getId().getBytes());
	    	get.addFamily(DOC);
	    	gets.add(get);
    	
	    	byId.put(model.getId(), model);
		}

    	Result[] results = docs.get(gets);
		for (Result result : results) {
			byte[] v = result.getValue(DOC, METADATA);
			if (v != null) {
				String id = Bytes.toString(result.getRow());
				String metadata = Bytes.toString(v);
				IModelResult r = byId.get(id);
				r.setMetadata(AnalysisMetadataDocument.loadFromJSON(metadata));
			}
		}
	}

	/**
	 * Given a model return, return the associate metada. 
	 * @param id
	 * @return null if the model does not exist.
	 * @throws IOException 
	 */
	@CheckForNull
	public String getMetadata(@Nonnull String id) throws IOException {
		Get get = new Get(id.getBytes());
		Table docs = getDocsInfo();
		Result result = docs.get(get);		
		if (! result.isEmpty()) {
			byte[] v = result.getValue(DOC, METADATA);
			if (v == null) 
				return "{}"; // an empty document
			return Bytes.toString(v);
		}
		return null;
	}

	public static class Stats {
		@NonNull
		private final Map<String, Integer> counters;

		private Stats(@NonNull Map<String, Integer> counters) {
			this.counters = counters;
		}
		
		@NonNull
		public Map<? extends String, Integer> getCounters() {
			return counters;
		}

	}

}
