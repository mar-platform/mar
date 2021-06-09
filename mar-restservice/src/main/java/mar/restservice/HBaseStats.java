package mar.restservice;

import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import mar.restservice.services.SearchOptions.ModelType;
import mar.spark.indexer.validation.IndexReader;

public class HBaseStats extends HBaseModelAccessor {

	private Stats stats = null;
	
	public Stats getStats() throws Exception {
		return getStats(false);
	}
	
	public synchronized Stats getStats(boolean forceReload) throws Exception {
		if (!forceReload && stats != null) {
			return stats;
		}
		
		Map<String, Integer> counters = new HashMap<>();
		for (ModelType modelType : ModelType.values()) {
			try(IndexReader reader = new IndexReader(modelType.name())) {
				int countDocs = 0;
				int countPaths = 0;
				if (reader.isIndexAvailable()) {
					countDocs = reader.getCountDocs();
					countPaths = reader.getCountPaths();
				}
				// Map<String, String> docs = reader.getDocuments();
				counters.put(modelType.name(), countDocs);
				//System.out.println("Total documents: " + countDocs);
				//System.out.println("Total path-index: " + countPaths);
			}			
		}		
		this.stats = new Stats(counters);
		return this.stats;
	}
	
	/*
	public Stats getStats() throws IOException {
		Table docs = getDocsInfo();
		Scan scan = new Scan();

		Map<String, Integer> counters = new HashMap<>();
		
		ResultScanner scanner = docs.getScanner(scan);
		try {
			for (Result result = scanner.next(); (result != null); result = scanner.next()) {
				List<Cell> cells = result.getColumnCells(Bytes.toBytes("information"), Bytes.toBytes("model_type"));
				for (Cell cell : cells) {
					String type = Bytes.toString(CellUtil.cloneValue(cell));
					int c = counters.getOrDefault(type, 0);
					counters.put(type, c + 1);
				}
				// for(KeyValue keyValue : result.list()) {
				// System.out.println("Qualifier : " + keyValue.getKeyString() + " : Value : " +
				// Bytes.toString(keyValue.getValue()));
				// }
			}
		} finally {
			scanner.close();
		}

		return new Stats(counters);
	}
	*/
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
