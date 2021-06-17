package mar.validation;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import mar.validation.AnalysisDB.Status;

public class ResilientAnalyser implements ISingleFileAnalyser {

	private static AtomicInteger instances = new AtomicInteger(0);
	private final ThreadLocal<String> limiterId = ThreadLocal.withInitial(this::newId);	

	@Nonnull
	private ISingleFileAnalyser delegate;

	private TimeLimiterRegistry timeLimiter;
	
	public ResilientAnalyser(@Nonnull ISingleFileAnalyser delegate	) {
		this(delegate, 30);
	}
	
	public ResilientAnalyser(@Nonnull ISingleFileAnalyser delegate, int timeoutSeconds) {
		this.delegate = delegate;
		TimeLimiterConfig config = TimeLimiterConfig.custom()
				   .cancelRunningFuture(true)
				   .timeoutDuration(Duration.ofSeconds(timeoutSeconds))
				   .build();
	
		this.timeLimiter = TimeLimiterRegistry.of(config);
	}
	
	public String newId() {		
		return "Resilient" + instances.incrementAndGet();
	}

	@Override
	public AnalysisResult analyse(IFileInfo f) {
		String id = limiterId.get();
		
		TimeLimiter limiter = timeLimiter.timeLimiter(id);
		AnalysisResult result;
		try {
			result = limiter.executeFutureSupplier(() -> CompletableFuture.supplyAsync(() -> delegate.analyse(f)));
		} catch (java.util.concurrent.TimeoutException e) {
			return new AnalysisResult(f.getModelId(), Status.TIMEOUT);
		} catch (Exception e) {
			e.printStackTrace();
			return new AnalysisResult(f.getModelId(), Status.CRASHED);
		}
		
		return result;
	}

}
