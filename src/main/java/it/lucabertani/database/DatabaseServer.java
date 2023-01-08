package it.lucabertani.database;

import java.sql.Connection;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseServer {

	private static final String DB_NAME = "db";
	private static final String USERNAME = "db";
	private static final String PASSWORD = "xab45e-xym(Aq-D#xdzn";
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServer.class);
	
	private final String url;
	private boolean connect;
    private Connection dbConnection;
    
    
    private DatabaseServer() {
    	this.url = "jdbc:h2:./db/" + DB_NAME;
    }
	
	public void connect() {
		if ( connect ) {
			return;
		}
		
		try {
			Class.forName("org.h2.Driver");
	        dbConnection = DriverManager.getConnection(url, USERNAME, PASSWORD);
	        connect = true;
		} catch (Exception e) {
			LOGGER.error("Errore nella connessione al database", e);
		}
	}
	
	public void disconnect() {
		if ( !connect ) {
			return;
		}
		
		try {
			dbConnection.close();
		} catch (Exception e) {
			LOGGER.error("Errore nella disconnessione al database", e);
		}
	}
	
	
	
	
    
    private static class SingletonHolder {
        private static final DatabaseServer INSTANCE = new DatabaseServer();
    }

    public static DatabaseServer getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
