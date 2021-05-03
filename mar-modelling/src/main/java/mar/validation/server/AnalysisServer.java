package mar.validation.server;

import java.util.concurrent.TimeUnit;

import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import mar.analysis.thrift.ValidateService;

public class AnalysisServer {

	public static final int PORT = 9081;
	
	private TThreadPoolServer server;
	private int port;

	public static void main(String[] args) throws TTransportException {
		int port;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		} else {
			port = PORT;
		}
		new AnalysisServer(port).start();
	}

	public AnalysisServer(int port) {
		this.port = port;
	}
	
	public void start() throws TTransportException {		
        TServerTransport serverTransport = new TServerSocket(port);
        // TSimpleServer
        server = new TThreadPoolServer(
        		new TThreadPoolServer.Args(serverTransport).			
        			requestTimeout(30).requestTimeoutUnit(TimeUnit.SECONDS).
        			processor(new ValidateService.Processor<>(new ValidateServiceImpl())));
        
        System.out.print("Starting the server... ");

        server.serve();

        System.out.println("done.");
    }

    public void stop() {
        if (server != null && server.isServing()) {
            System.out.print("Stopping the server... ");

            server.stop();

            System.out.println("done.");
        }
    }
}