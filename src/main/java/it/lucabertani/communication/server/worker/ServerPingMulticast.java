package it.lucabertani.communication.server.worker;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import it.lucabertani.communication.model.PackagePing;
import it.lucabertani.utils.Constants;

public class ServerPingMulticast {

	private ServerPingBroadcastThread serverPingBroadcastThread;
	private Thread worker;

	private ServerPingMulticast() {
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
			//String broadcastMessage = "hello world!";

			try {
				/*List<InetAddress> addresses = listAllBroadcastAddresses();
				for (InetAddress address : addresses) {
					DatagramSocket socket = new DatagramSocket();
					socket.setBroadcast(true);

					byte[] buffer = broadcastMessage.getBytes();
					
					System.out.println("Send ping to " + address);

					DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4445);
					socket.send(packet);
					socket.close();
				}*/
				
				MulticastSocket socket = new MulticastSocket(Constants.SERVER_PORT);
				
				PackagePing packagePing = new PackagePing("server1");
				
				byte[] buffer = packagePing.toJson().getBytes();
				
				System.out.println("Send ping to " + Constants.SERVER_MULTICAST_ADDRESS);
				 
				InetAddress group = InetAddress.getByName(Constants.SERVER_MULTICAST_ADDRESS);
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, Constants.SERVER_PORT);
				socket.send(packet);
				socket.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*private List<InetAddress> listAllBroadcastAddresses() throws SocketException {
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
		}*/

		public void stop() {
			exit = true;
		}

	}

	private static class SingletonHolder {
		private static final ServerPingMulticast INSTANCE = new ServerPingMulticast();
	}

	public static ServerPingMulticast getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
