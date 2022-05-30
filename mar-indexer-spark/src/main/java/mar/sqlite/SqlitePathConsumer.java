package mar.sqlite;

@FunctionalInterface
public interface SqlitePathConsumer {
	public void consume(String path, String docId, int numDocsWithPath, int nTokens, int nOccurences);
}