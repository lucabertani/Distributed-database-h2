package it.lucabertani.communication.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);
	
	private String serverAddress = null;
	private int serverPort = -1;
	private int serverBacklog = -1;
	private InetAddress serverInetAddress = null;
	private int serverSocketReadTimeout = -1;
	
	private ServerSocket serverSocket = null;
	private boolean started = false;
	private ThreadPoolExecutor threadPool;
	
	private Thread shutdownHook = null;
	
	private ServerManager() {
		LOGGER.info("Reading socket properties...");
		
		this.serverAddress = "127.0.0.1"; //PropertiesManager.getInstance().readExternalProperty(Constants.PROP_SOCKET_ADDRESS);
		this.serverPort = 6666; //PropertiesManager.getInstance().readExternalPropertyInt(Constants.PROP_SOCKET_PORT);
		this.serverBacklog = 100; // PropertiesManager.getInstance().readExternalPropertyInt(Constants.PROP_SOCKET_BACKLOG);
		
		try {			
			this.serverInetAddress = Inet4Address.getByName(this.serverAddress);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.serverSocketReadTimeout = 60 * 1000; //PropertiesManager.getInstance().readExternalPropertyInt(Constants.PROP_SOCKET_READ_TIMEOUT) * 1000;

		LOGGER.info("Done!");
		
		int sizeInit = 1;// PropertiesManager.getInstance().readExternalPropertyInt(Constants.PROP_THREADPOOL_SIZE_INIT);
		int sizeMax = 10; //PropertiesManager.getInstance().readExternalPropertyInt(Constants.PROP_THREADPOOL_SIZE_MAX);
		int idleTimeout = 1000; //PropertiesManager.getInstance().readExternalPropertyInt(Constants.PROP_THREADPOOL_IDLE_TIMEOUT);
		
		LOGGER.info("Creating thread pool with initial size " + sizeInit + ", max size " + sizeMax + ", idle timeout " + idleTimeout + "...");
		
		threadPool = new ThreadPoolExecutor(
				sizeInit,    // core size
				sizeMax,     // max size
				idleTimeout, // idle timeout
			    TimeUnit.SECONDS,
			    new LinkedBlockingQueue<Runnable>()
			);
		
		threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
		    @Override
		    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		    	LOGGER.error("Thread reject !!");
		    }
		});
		
		LOGGER.info("Done!");
	}
	
	public void start() {
		if ( started ) {
			return;
		}
		
		LOGGER.info("Starting listening at " + serverAddress + ", port " + serverPort + " with backlog " + serverBacklog);
		
		try {
			serverSocket = new ServerSocket(serverPort, serverBacklog, serverInetAddress);
		} catch (IOException e) {
			String msg = "Cannot open socket at " + serverAddress + " for port " + serverPort + " with backlog " + serverBacklog;
			LOGGER.error(msg, e);
			throw new RuntimeException(msg, e);
		}
		
		LOGGER.info("Done! Waiting for connections...");
		
		if ( shutdownHook == null ) {
			shutdownHook = new Thread() {
	            @Override
	            public void run() {
	                ServerManager.getInstance().stop();
	            }
	        };
	        
			Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
		
		started = true;
		
		while (!isStopped()) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				if (isStopped()) {
					LOGGER.info("Server stopped");
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}

			threadPool.submit(ServerSocketWorker.createNewWorker(clientSocket, serverSocketReadTimeout));
		}
		LOGGER.info("Server stopped");
	}
	
	/*private synchronized boolean isStarted() {
		return started;
	}*/

	private synchronized boolean isStopped() {
		return !started;
	}
	
	public synchronized void stop() {
		if ( !started ) {
			return;
		}
		
		try {
			serverSocket.close();
			started = false;
			
			if ( shutdownHook != null ) {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
				shutdownHook = null;
			}
			
		} catch (IOException e) {
			LOGGER.error("Error closing server", e);
			throw new RuntimeException("Error closing server", e);
		}
	}

	
	

	private static class SingletonHolder {
        private static final ServerManager INSTANCE = new ServerManager();
    }

    public static ServerManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
