package it.lucabertani.communication.server.worker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSocketWorker implements Runnable {
	
	private static final byte BYTE_END = '\n';
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerSocketWorker.class);
	
	private Socket clientSocket = null;
	private int serverSocketReadTimeout;
	
	public static ServerSocketWorker createNewWorker(Socket clientSocket, int serverSocketReadTimeout/*, String serverText*/) {
		return new ServerSocketWorker(clientSocket, serverSocketReadTimeout/*, serverText*/);
	}

	public ServerSocketWorker(Socket clientSocket, int serverSocketReadTimeout/*, String serverText*/) {
		this.clientSocket = clientSocket;
		this.serverSocketReadTimeout = serverSocketReadTimeout;
	}
	
	public void run() {
		
		//InputStream inputStream = null;
		//OutputStream outputStream = null;
		
		try (InputStream inputStream = clientSocket.getInputStream(); OutputStream outputStream = clientSocket.getOutputStream();) {
			
			clientSocket.setSoTimeout(serverSocketReadTimeout);
			
			while (true) {
				byte[] messages = readStream(inputStream);
				
				String s = new String(messages);
				System.out.println("Written: " + s);
				
				if ( StringUtils.isEmpty(s) ) {
					break;
				}
			}
			
			
		} catch (IOException e) {
			LOGGER.error("Errore IO", e);
		}
		
		/*try {
			inputStream = clientSocket.getInputStream();
			outputStream = clientSocket.getOutputStream();
			
			this.clientSocket.setSoTimeout(this.serverSocketReadTimeout);
			
			logger.info("New connection, starting reading...");
			
			Packet packet = PacketManager.readUntilParityOk(this, null);
			
			packet = Flusso1Autenticazione.execute(packet, this);
			packet = Flusso2ChiaveCifratura.execute(packet, this);
			packet = Flusso3ChiaveFirma.execute(packet, this);
			packet = Flusso4ChiaviDSRC.execute(packet, this);
			
		} catch (BadActionException e) {
			logger.severe("BadActionException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_BAD_ACTION, outputStream);
		} catch (BadPacketException e) {
			logger.severe("BadPacketException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_BAD_PACKET, outputStream);
		} catch (BadSamIdException e) {
			logger.severe("BadSamIdException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_BAD_SAMID, outputStream);
		} catch (BadKeyException e) {
			logger.severe("BadKeyException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_BAD_KEY, outputStream);
		} catch (BadVersionKey e) {
			logger.severe("BadVersionKey: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_BAD_VERSIONKEY, outputStream);
		} catch (BadParityException e) {
			logger.severe("BadParityException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_BAD_PARITY, outputStream);
			// al momento non dovrebbe mai arrivare qui perché, come da protocollo, ci si aspetta un reinvio del pacchetto
		} catch (ActionUnsupportedException e) {
			logger.severe("ActionUnsupportedException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_ACTION_UNSUPPORTED, outputStream);
		} catch (ParamErrorException e) {
			logger.severe("ParamErrorException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_PARAM_ERROR, outputStream);
		} catch (EmptyFrameException e) {
			// mi è stato inviato un frame vuoto oppure si è chiusa la connessione, non faccio nulla...
			logger.warning("Empty frame or connection closed");
		} catch (StopProcessingException e) {
			// questa eccezione serve solo per terminare il workflow in quanto c'è stato un errore riportato dai servizi SOAP
			
		} catch (GenericErrorException e) {
			logger.severe("GenericErrorException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_GENERIC_ERROR, outputStream);
		} catch (SocketTimeoutException e) {
			logger.severe("SocketTimeoutException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_TIMEOUT, outputStream);
		} catch (IOException e) {
			logger.severe("IOException: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_GENERIC_ERROR, outputStream);
		} catch (Exception e) {
			logger.severe("Exception: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_GENERIC_ERROR, outputStream);
			
		} catch (Throwable e) {
			logger.severe("Throwable: " + e.getMessage(), e);
			PacketManager.sendErrorPacket(this.model.getLastNmesg(), Constants.RESPONSE_GENERIC_ERROR, outputStream);
			
		} finally {
			// chiusura di tutte le connessioni
			logger.info("Closing streams and socket...");
			NetworkManager.silentCloseStream(inputStream);
			NetworkManager.silentCloseStream(outputStream);
			NetworkManager.silentCloseSocket(this.clientSocket);
			logger.info("... done !");
		}*/
	}
	
	
	
	private static byte[] readStream(InputStream input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] bytes = new byte[1024];
		int count = 0;
		boolean checkEnd = false;

		while ( !checkEnd && (count = input.read(bytes)) > 0 ) {
			for ( int i = 0; i < count; i++ ) {
				byte b = bytes[i];
				if ( b == BYTE_END ) {
					
					count = i - 1;
					
					checkEnd = true;
					break;
				}
			}
			
			baos.write(bytes, 0, count);
		}

		return baos.toByteArray();
	}
	
	private static byte[] readBytes(InputStream input, int size) throws IOException {
		byte[] bytes = new byte[size];

		int check = input.read(bytes);
		if (check == size) {
			return bytes;
		}

		throw new RuntimeException("Unable to read " + size + " bytes, readed only " + check);
	}

	private static byte readByte(InputStream input) throws IOException {
		return readBytes(input, 1)[0];
	}

	private static void writeBytes(OutputStream output, byte[] bytes) throws IOException {
		output.write(bytes);
	}
}
