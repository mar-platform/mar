package mar.restservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import edu.umd.cs.findbugs.annotations.NonNull;

public class HBaseStats extends HBaseModelAccessor {

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
