package it.lucabertani.main;

import it.lucabertani.communication.ConnectionLayer;
import it.lucabertani.database.DatabaseServer;

public class Main {

	public static void main(String[] args) {
		System.out.println("db connection...");
		DatabaseServer.getInstance().connect();
		System.out.println("db connected!");
		
		System.out.println("db disconnection...");
		DatabaseServer.getInstance().disconnect();
		System.out.println("db disconnected!");
		
		ConnectionLayer.getInstance().listen();
		
		try {
			Thread.sleep(6000 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// nc -l localhost 8888.
	}
}
