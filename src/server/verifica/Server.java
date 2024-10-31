package server.verifica;

// Bergamasco Jacopo, 5AIA, A.S. 2024-2025

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import utils.jlibs.MutexSemaphore;

public class Server {
	
	public static final int PORT = 7979;

	private static MutexSemaphore mutex;
	protected static int connections = 0;
	
	public static void main(String[] args) {
		// Inizializzazioni
		mutex = new MutexSemaphore();
		connections = 0;
		ServerSocket ws;
		try {
			ws = new ServerSocket(PORT);
		} catch (IOException ex) {
			handle(ex, "Impossibile creare il server.");
			return;
		}
		
		// Notifica di avvio
		System.out.println("Server avviato su porta " + PORT);
		
		// Loop di ricezione principale
		while (true) {
			Socket s;
			try {
				s = ws.accept();
				mutex.P();
				// Se un client si connette, il contatore viene incrementato
				// Quando un client si disconnette, il contatore viene decrementato
				//     dall'oggetto Protocol, sempre in mutua esclusione
				connections++;
				mutex.V();
			} catch (IOException ex) {
				handle(ex, "Connessione con client fallita");
				continue;
			}
			new Thread(new Protocol(s, mutex)).start();
		
			// Il Welcoming Socket non viene mai chiuso, ma tanto ci pensa Windows
		}
	}
	
	protected static void handle(Exception ex, String msg) {
		// Mostra gli errori, almeno non bisogna scrivere una stringa di 10 milioni di
		//     caratteri ogni volta che si gestisce un'eccezione
		System.err.println("ERRORE: " + msg + ": " + ex.getMessage());
	}
}
