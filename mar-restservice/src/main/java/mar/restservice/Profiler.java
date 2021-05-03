package mar.restservice;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A poors-man profiler, for easy debugging.
 * 
 * @author jesus
 *
 */
public class Profiler {

	private Stopwatch watch;
	/** Times in seconds */
	private Map<String, Double> times = new HashMap<>();
	
	public Profiler() {
		this.watch = Stopwatch.createUnstarted();
	}
	
	public void start() {
		Preconditions.checkState(! watch.isRunning());
		watch.start();
	}

	public <T> T execute(@NonNull String name, @NonNull Supplier<T> r) {
		start();
		T result = r.get();
		stop(name);
		return result;
	}
	
	public void stop(@NonNull String name) {
		Preconditions.checkState(watch.isRunning());
		watch.stop();
		times.put(name, watch.elapsed(TimeUnit.MILLISECONDS) / 1_000.0);
		watch.reset();
	}

	public void stop(@NonNull String name, @NonNull PrintStream stream) {
		stop(name);
		Double v = times.get(name);
		stream.println(name + " - " + String.format("%.2f secs.", v));
	}
	
	@NonNull
	public Map<? extends String, Double> getTimes() {
		return times;
	}
	
	public void toOutput(@NonNull PrintStream stream) {
		times.forEach((k, v) -> stream.println(k + " - " + String.format("%.2f secs.", v)));
	}
}
