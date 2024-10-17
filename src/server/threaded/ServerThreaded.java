package server.threaded;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// TODO: lista socket connessi
// TODO: comando list
// TODO: Chat globale

public class ServerThreaded {
	protected static final boolean DO_TIMEOUT = false;
	public static final int PORT = 7979;
	
	private static ServerSocket server;
	private static boolean accept;
	private static ArrayList<Socket> connections;
	
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
		connections = new ArrayList<Socket>();
		accept = true;
		while (accept) {
			Socket connection;
			try {
				connection = server.accept();
			} catch (IOException ex) {
				System.err.println("ERROR: Connection failed with client: " + ex.getMessage());
				continue;
			}
			connections.add(connection);
			new Thread(new ServerProtocol(connection, connections)).start();
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
