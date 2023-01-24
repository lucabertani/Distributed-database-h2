package it.lucabertani.main;

import java.util.Optional;

import it.lucabertani.communication.model.Package;
import it.lucabertani.communication.model.PackageFactory;
import it.lucabertani.communication.model.PackagePing;
import it.lucabertani.communication.server.ServerManager;
import it.lucabertani.communication.server.worker.ServerPingMulticast;

public class Main {

	public static void main(String[] args) {
		/*System.out.println("db connection...");
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
		*/
		// nc -l localhost 8888
		// nc -l localhost 6666
		// ServerPingBroadcast.getInstance().startPing();
		
		ServerManager.getInstance().start();
		ServerPingMulticast.getInstance().startPing();
		
		/*PackagePing packagePing = new PackagePing("test123");
		String json = packagePing.toJson();
		System.out.println("json: " + json);
		
		Optional<Package> convertToPackage = PackageFactory.convertToPackage(json);
		if ( convertToPackage.isPresent() ) {
			Package p = convertToPackage.get();
			System.out.println(p.getServerId() + " " + p.getType());
		}*/
		
		try {
			Thread.sleep(6000 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
