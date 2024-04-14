package mar.sqlite;

@FunctionalInterface
public interface SqliteSimplePathConsumer {
	public void consume(long pathId, String path);
}