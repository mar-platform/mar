package mar.rest.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import org.apache.commons.cli.ParseException;
import org.junit.rules.ExternalResource;

import mar.indexer.common.configuration.EnvironmentVariables;
import mar.indexer.common.configuration.EnvironmentVariables.MAR;
import mar.restservice.Main;
import spark.Spark;

// Based on the ideas of: https://dzone.com/articles/overview-of-spark-and-http-testing-with-junit
public class MarHttpServer extends ExternalResource {

	public static int defaultPort = 4567;

	@Override
	protected void before() throws Throwable {
		ensureHBaseRunning();
		
		String config = Paths.get(EnvironmentVariables.getVariable(MAR.REPO_MAR), "configuration/repo-test/config.json").toString();
		String[] args = { "-p", String.valueOf(defaultPort), "-c", config };
		try {
			Main.main(args);
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
		
		// Wait a bit to make sure it is running
		// TODO: change for a busy waiting to a /heartbeat
        Spark.awaitInitialization();
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void after() {
		Spark.stop();
	}

	private void ensureHBaseRunning() {
		File running = new File(
				EnvironmentVariables.getVariable(MAR.REPO_MAR) + File.separator + ".test_docker_running");
		if (!running.exists())
			throw new IllegalStateException("Docker not running. Check: " + running.getAbsolutePath());
	}

	public String getURL(@Nonnull String params) {
		return "http://localhost:4567/" + params;
	}
}
