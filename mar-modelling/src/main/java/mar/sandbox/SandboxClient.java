package mar.sandbox;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import mar.analysis.thrift.InvalidOperation;
import mar.analysis.thrift.ValidateService;
import mar.validation.server.AnalysisServer;

public abstract class SandboxClient implements AutoCloseable {

	private static final int TIMEOUT_MS = 45 * 1000;

	// Counters for new ports
	private final AtomicInteger availablePorts;
	// Each analysing thread holds its own ServerProcess.
	private final ThreadLocal<ServerProcess> serverProcess = ThreadLocal.withInitial(this::newProcess);
	// Aggregate all processes here so that we can stop them
	private final Set<ServerProcess> runningServers = Collections.newSetFromMap(new ConcurrentHashMap<ServerProcess, Boolean>());

	@FunctionalInterface
	protected static interface Invoker<T> {
		public T invoke(TProtocol protocol) throws TException;
	}
	
	public SandboxClient(int initalPort) {
		this.availablePorts = new AtomicInteger(initalPort);
	}
	
	protected <T> T invokeService(Invoker<T> invoker, Supplier<T> onTimeOut) {
		ServerProcess process = checkOrRestartServer();
		
		try {
			TSocket transport = newSocket(process);
			transport.setSocketTimeout(TIMEOUT_MS);
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
	
			try {
				return invoker.invoke(protocol);
			} finally {
				transport.close();
			}
		} catch (TTransportException e) {
			if (e.getCause() instanceof SocketTimeoutException) {
				e.printStackTrace();
				System.out.println("TTransportException");
				return onTimeOut.get();
			}
			throw new RuntimeException(e);
		} catch (InvalidOperation e) {
			throw new RuntimeException(e);
		} catch (TException e) {
			if (e.getCause() instanceof SocketTimeoutException) {
				e.printStackTrace();
				System.out.println("TException");
				return onTimeOut.get();
			}
			throw new RuntimeException(e);
		}
	}

	private TSocket newSocket(@Nonnull ServerProcess process) {
		TSocket transport = new TSocket("localhost", process.getPort());
		return transport;
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
		
		try (TSocket transport = newSocket(process)){
			while (true) {
				try {									
					transport.open();
					break;
				} catch (TTransportException e) {
					Thread.yield();
					continue;
				}
			}
		}
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

		    		Thread closeChildThread = new Thread() {
			            public void run() {
			                process.destroyForcibly();
			            }
			        };

			        Runtime.getRuntime().addShutdownHook(closeChildThread);			        
			        status = ServerStatus.STARTED;
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
