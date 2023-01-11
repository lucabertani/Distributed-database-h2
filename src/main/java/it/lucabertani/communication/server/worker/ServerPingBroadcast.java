package it.lucabertani.communication.server.worker;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class ServerPingBroadcast {

	private ServerPingBroadcastThread serverPingBroadcastThread;
	private Thread worker;

	private ServerPingBroadcast() {
	}

	public void startPing() {
		serverPingBroadcastThread = new ServerPingBroadcastThread();
		worker = new Thread(serverPingBroadcastThread);
		worker.run();
	}

	public void stopPing() {
		serverPingBroadcastThread.stop();
		worker = null;
	}

	private class ServerPingBroadcastThread implements Runnable {

		private volatile boolean exit = false;

		@Override
		public void run() {
			while ( !exit ) {
				pingBroadcast();
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void pingBroadcast() {
			String broadcastMessage = "hello world!";

			try {
				List<InetAddress> addresses = listAllBroadcastAddresses();
				for (InetAddress address : addresses) {
					DatagramSocket socket = new DatagramSocket();
					socket.setBroadcast(true);

					byte[] buffer = broadcastMessage.getBytes();
					
					System.out.println("Send ping to " + address);

					DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4445);
					socket.send(packet);
					socket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private List<InetAddress> listAllBroadcastAddresses() throws SocketException {
			List<InetAddress> broadcastList = new ArrayList<>();
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue;
				}

				networkInterface.getInterfaceAddresses().stream()
					.map(a -> a.getBroadcast())
					.filter(Objects::nonNull)
					.forEach(broadcastList::add);
			}
			return broadcastList;
		}

		public void stop() {
			exit = true;
		}

	}

	private static class SingletonHolder {
		private static final ServerPingBroadcast INSTANCE = new ServerPingBroadcast();
	}

	public static ServerPingBroadcast getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
