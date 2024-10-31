package server.threaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import utils.jlibs.MutexSemaphore;

// TODO: Chat globale

public class ServerThreaded {
	protected static final boolean DO_TIMEOUT = false;
	public static final int PORT = 7979;
	public static final int MAX_CLIENTS = 20;
	
	private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	
	private static ServerSocket server;
	private static boolean accept;
	private static ProtocolList protocols;
	
	public static void main(String[] args) {
		// Inizializzazione semaforo e chat globale
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
		protocols = new ProtocolList(s);
		accept = true;
		new Thread(() -> {
			while (accept) try {
				protocols.broadcast("[SERVER] " + stdin.readLine(), null);
			} catch (IOException ex) {}
		});//.start();
		ExecutorService pool = Executors.newFixedThreadPool(MAX_CLIENTS);
		while (accept) {
			Socket sock;
			try {
				sock = server.accept();
			} catch (IOException ex) {
				System.err.println("ERROR: Connection failed with client: " + ex.getMessage());
				continue;
			}
			pool.execute(new ServerProtocol(sock, protocols, s));
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
