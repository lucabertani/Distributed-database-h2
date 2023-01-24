package it.lucabertani.communication.server.worker;

import java.net.DatagramPacket;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.lucabertani.communication.model.Package;
import it.lucabertani.communication.model.PackageFactory;

public class ServerSocketUDPWorker implements Runnable {

	private static final byte BYTE_END = '\n';
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocketUDPWorker.class);
	
	private DatagramPacket packet;
	
	public static ServerSocketUDPWorker createNewWorker(DatagramPacket packet) {
		return new ServerSocketUDPWorker(packet);
	}
	
	private ServerSocketUDPWorker(DatagramPacket packet) {
		this.packet = packet;
	}

	@Override
	public void run() {
		String messageJson = new String(packet.getData(), 0, packet.getLength());
        Optional<Package> p = PackageFactory.convertToPackage(messageJson);
        if ( p.isPresent() ) {
        	Package pack = p.get();
        	System.out.println("ServerId: " + pack.getServerId() + ", packageType: " + pack.getType());
        }
	}
}
