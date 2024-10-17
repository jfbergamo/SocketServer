package server.threaded;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// TODO: Utente admin che chiude il server
// TODO: Chat globale

public class ServerThreaded {
	private static ServerSocket server;
	private static boolean accept;
	
	protected static final boolean DO_TIMEOUT = false;
	public static final int PORT = 7979;
	
	public static void main(String[] args) {
		// Creazione Welcoming Socket
		try {
			server = new ServerSocket(PORT);
		} catch (IOException ex) {
			System.err.println("ERROR: Unable to start server: " + ex.getMessage());
			return;
		}
		
		System.out.println("INFO: Server avviato su porta " + PORT);
		
		// Accettazione connessioni
		accept = true;
		while (accept) {
			Socket connection;
			try {
				connection = server.accept();
			} catch (IOException ex) {
				System.err.println("ERROR: Connection failed with client: " + ex.getMessage());
				continue;
			}
			new Thread(new ServerProtocol(connection)).start();
		}
		
	}
	
	protected static void stop() {
		accept = false;
		// Chiusura Server
		try {
			server.close();
		} catch (IOException ex) {
			System.exit(0);
		}
	}
}
