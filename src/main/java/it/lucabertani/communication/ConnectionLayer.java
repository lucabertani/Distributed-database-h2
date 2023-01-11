package it.lucabertani.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.lucabertani.communication.server.ServerManager;

public class ConnectionLayer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLayer.class);
	
	private ConnectionLayer() {
		
	}
	
	public void listen() {
		ServerManager.getInstance().start();
	}
	
	public void startPingBroadcast() {
		
	}
	
	public void stopPingBroadcast() {
		
	}
	
	

	private static class SingletonHolder {
        private static final ConnectionLayer INSTANCE = new ConnectionLayer();
    }

    public static ConnectionLayer getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
