package mar.validation.server;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import mar.analysis.thrift.InvalidOperation;
import mar.analysis.thrift.Result;
import mar.analysis.thrift.ValidateService;
import mar.analysis.thrift.ValidationJob;
import mar.validation.AnalysisDB.Status;
import mar.validation.AnalysisMetadataDocument;
import mar.validation.AnalysisResult;
import mar.validation.IFileInfo;
import mar.validation.ISingleFileAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;

public class AnalysisClient implements ISingleFileAnalyser.Remote {

	private static final int TIMEOUT_MS = 45 * 1000;

	// Counters for new ports
	private final AtomicInteger availablePorts = new AtomicInteger(9081);
	// Each analysing thread holds its own ServerProcess.
	private final ThreadLocal<ServerProcess> serverProcess = ThreadLocal.withInitial(this::newProcess);
	// Aggregate all processes here so that we can stop them
	private final Set<ServerProcess> runningServers = Collections.newSetFromMap(new ConcurrentHashMap<ServerProcess, Boolean>());
	
	@Nonnull
	private final String type;
	@Nonnull
	private final OptionMap options;
	
	public AnalysisClient(@Nonnull String type, @CheckForNull OptionMap options) {
		this.type = type;
		this.options = options;
	}

	public AnalysisClient(@Nonnull String type) {
		this(type, null);
	}

	@Override
	public AnalysisResult analyse(IFileInfo f) {
		ServerProcess process = checkOrRestartServer();
		
		try {
			TSocket transport = new TSocket("localhost", process.getPort());
			transport.setSocketTimeout(TIMEOUT_MS);
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			ValidateService.Client client = new ValidateService.Client(protocol);
			
			ValidationJob job = new ValidationJob(f.getModelId(), f.getRelativePath(), f.getAbsolutePath(), type, options);
			Result jobResult = client.validate(job);
			transport.close();		
			
			AnalysisResult r = new AnalysisResult(f.getModelId(), Status.valueOf(jobResult.getStatus()));
			if (jobResult.isSetStats())
				jobResult.stats.forEach((k, v) -> r.withStats(k, v));
			if (jobResult.isSetMetadata())
				r.withMetadata(jobResult.metadata);			
			if (jobResult.isSetMetadata_json()) {
				AnalysisMetadataDocument document = AnalysisMetadataDocument.loadFromJSON(jobResult.metadata_json);
				addIngestedMetadata(f, document);
				try {
					r.withMetadataJSON(document);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
			return r;
		} catch (TTransportException e) {
			if (e.getCause() instanceof SocketTimeoutException) {
				e.printStackTrace();
				System.out.println("TTransportException");
				return new AnalysisResult(f.getModelId(), Status.TIMEOUT);
			}
			throw new RuntimeException(e);
		} catch (InvalidOperation e) {
			throw new RuntimeException(e);
		} catch (TException e) {
			if (e.getCause() instanceof SocketTimeoutException) {
				e.printStackTrace();
				System.out.println("TException");
				return new AnalysisResult(f.getModelId(), Status.TIMEOUT);
			}
			throw new RuntimeException(e);
		}	
	}
	
	@Override
	public void close() {
		runningServers.forEach(r -> {
			r.kill();
		});
		runningServers.clear();
	}

	private ServerProcess newProcess() {
		ServerProcess process = new ServerProcess(availablePorts.getAndIncrement());
		process.start();
		runningServers.add(process);
		while (process.status != ServerStatus.STARTED) {
			Thread.yield();
		}
		
		try {
			// Sleep a bit, to make sure the server is running started
			Thread.sleep(500);
		} catch (InterruptedException e) { }
		
		return process;
	}
	
	@Nonnull
	private ServerProcess checkOrRestartServer() {
		ServerProcess process = serverProcess.get();
		// All the initialization process is indirectly done by newProcess when
		// the get method is called the first time
		return process;
	}
	
	private static class ServerProcess extends Thread {

		private volatile ServerStatus status = ServerStatus.STOPPED;
		private Process process;
		private boolean killed = false;
		private final int port;

		public ServerProcess(int port) {
			this.port = port;
		}
		
		public int getPort() {
			return port;
		}

		@Override
		public void run() {
		    while (true) {
		    	status = ServerStatus.STARTING;
		    	try {
		    		if (killed)
		    			return;
		    		
		    		process = spawnProcess();
		    		status = ServerStatus.STARTED;

		    		Thread closeChildThread = new Thread() {
			            public void run() {
			                process.destroyForcibly();
			            }
			        };

			        Runtime.getRuntime().addShutdownHook(closeChildThread);			        
					process.waitFor();
					// process.exitValue();
			        Runtime.getRuntime().removeShutdownHook(closeChildThread);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
		    }
	    }			
		
		public void kill() {
			if (process != null && process.isAlive()) {
				killed = true;
				process.destroyForcibly();				
			}
		}

		@Override
		protected void finalize() throws Throwable {
			
		}
		
		public ServerStatus getStatus() {
			return status;
		}

		private Process spawnProcess() throws IOException {
			Class<?> klass = AnalysisServer.class;
			
			String javaHome = System.getProperty("java.home");
	        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
	        String classpath = System.getProperty("java.class.path");
	        String className = klass.getName();

	        // TODO: Consider: -XX:OnOutOfMemoryError=kill -9 %p
	        
	        List<String> command = new ArrayList<String>();
	        command.add(javaBin);
	        command.add("-cp");
	        command.add(classpath);
	        command.add(className);
	        command.add(String.valueOf(port));

	        ProcessBuilder builder = new ProcessBuilder(command);
	        Process process = builder.inheritIO().start();	        
			return process;
		}

	}

	private static enum ServerStatus {
		STARTING,
		STARTED,
		STOPPED
	}
	
}
