package server.threaded;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import utils.jlibs.MutexSemaphore;

// TODO: lista socket connessi
// TODO: comando list
// TODO: Chat globale

public class ServerThreaded {
	protected static final boolean DO_TIMEOUT = false;
	public static final int PORT = 7979;
	
	private static ServerSocket server;
	private static boolean accept;
	private static ArrayList<Client> clients;
	
	public static void main(String[] args) {
		// Inizializzazione semaforo
		MutexSemaphore s = new MutexSemaphore();
		
		// Creazione Welcoming Socket
		try {
			server = new ServerSocket(PORT);
		} catch (IOException ex) {
			System.err.println("ERROR: Unable to start server: " + ex.getMessage());
			return;
		}
		
		System.out.println("INFO: Server avviato su porta " + PORT);
		
		// Accettazione connessioni
		clients = new ArrayList<Client>();
		accept = true;
		while (accept) {
			Socket sock;
			try {
				sock = server.accept();
			} catch (IOException ex) {
				System.err.println("ERROR: Connection failed with client: " + ex.getMessage());
				continue;
			}
			Client client = new Client(sock);
			s.P();
			clients.add(client);
			s.V();
			new ServerProtocol(client, clients, s).start();
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
