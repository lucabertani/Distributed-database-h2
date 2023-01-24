package it.lucabertani.communication.server.socket;

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

import it.lucabertani.communication.server.worker.ServerSocketTCPWorker;
import it.lucabertani.utils.Constants;

public class ServerListenerTCP {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerListenerTCP.class);
	
	private final ServerListenerThread serverListenerThread;
	
	
	
	
	
	private ServerListenerTCP() {
		this.serverListenerThread = new ServerListenerThread();
	}
	
	public void start() {
		serverListenerThread.start();
		Thread t = new Thread(serverListenerThread);
		t.start();
	}
	
	/*private synchronized boolean isStarted() {
		return started;
	}*/

	public void stop() {
		serverListenerThread.stop();
	}


	private static class SingletonHolder {
        private static final ServerListenerTCP INSTANCE = new ServerListenerTCP();
    }
    public static ServerListenerTCP getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    
    private class ServerListenerThread implements Runnable {
		
		private ServerListenerThread() {
			LOGGER.info("Reading TCP socket properties...");
			
			this.serverAddress = "127.0.0.1"; //PropertiesManager.getInstance().readExternalProperty(Constants.PROP_SOCKET_ADDRESS);
			//this.serverAddress = "192.168.1.170";
			// this.serverPort = 6666; //PropertiesManager.getInstance().readExternalPropertyInt(Constants.PROP_SOCKET_PORT);
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
		
		private String serverAddress = null;
		private int serverPort = Constants.SERVER_PORT;
		private int serverBacklog = -1;
		private InetAddress serverInetAddress = null;
		private int serverSocketReadTimeout = -1;
		
		private ServerSocket serverSocket = null;
		private boolean started = false;
		private ThreadPoolExecutor threadPool;
		
		private Thread shutdownHook = null;

		public void start() {
			if ( started ) {
				return;
			}
			
			LOGGER.info("Starting listening TCP at " + serverAddress + ", port " + serverPort + " with backlog " + serverBacklog);
			
			try {
				serverSocket = new ServerSocket(serverPort, serverBacklog, serverInetAddress);
			} catch (IOException e) {
				String msg = "Cannot open socket TCP at " + serverAddress + " for port " + serverPort + " with backlog " + serverBacklog;
				LOGGER.error(msg, e);
				throw new RuntimeException(msg, e);
			}
			
			LOGGER.info("Done! Waiting for connections...");
			
			if ( shutdownHook == null ) {
				shutdownHook = new Thread() {
		            @Override
		            public void run() {
		                ServerListenerTCP.getInstance().stop();
		            }
		        };
		        
				Runtime.getRuntime().addShutdownHook(shutdownHook);
			}
			
			started = true;
			
			
		}
		
		private synchronized boolean isStopped() {
			return !started;
		}
		
		public synchronized void stop() {
			if ( !started ) {
				return;
			}
			
			try {
				started = false;
				serverSocket.close();
				
				if ( shutdownHook != null ) {
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
					shutdownHook = null;
				}
				
			} catch (IOException e) {
				LOGGER.error("Error closing server", e);
				throw new RuntimeException("Error closing server", e);
			}
		}
		
		@Override
		public void run() {
			while (!isStopped()) {
				Socket clientSocket = null;
				try {
					clientSocket = serverSocket.accept();
				} catch (IOException e) {
					if (isStopped()) {
						LOGGER.info("Server TCP stopped");
						return;
					}
					throw new RuntimeException("Error accepting client connection", e);
				}

				threadPool.submit(ServerSocketTCPWorker.createNewWorker(clientSocket, serverSocketReadTimeout));
			}
			LOGGER.info("Server TCP stopped");
		}
		
	}
}
