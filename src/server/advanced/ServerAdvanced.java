package server.advanced;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerAdvanced {

	private static final int MAX_ERRORS = 20;
	
	public static final String NOME = "Jacopo";
	public static final String COGNOME = "Bergamasco";

	public static void main(String[] args) {
		ServerSocket ws;
		try {
			ws = new ServerSocket(7979); // Welcoming Socket
		} catch (IOException ex) {
			System.err.println("[ERROR] " + ex.getMessage());
			return;
		}
		
		System.out.println("[SERVER] Listening on port " + ws.getLocalPort());	

		boolean acceptConnections = true;
		while (acceptConnections) {
			int errors = 0;

			Socket sock;
			try {
				sock = ws.accept();
			} catch (IOException ex) {
				errors++;
				System.err.println("[ERROR] " + ex.getMessage());
				if (errors >= MAX_ERRORS) {
					System.err.println("[ERROR] Too many errors, server shutting down.");
					acceptConnections = false;
				}
				continue;
			}
			errors = 0;
			
			long beginConnection = System.currentTimeMillis();

			String clientAddr = getClientAddr(sock);
			System.out.println("[SERVER] Accepted connection from " + clientAddr);

			BufferedReader in;
			PrintWriter out;
			try {
				in = new BufferedReader(
						new InputStreamReader(sock.getInputStream())
						);
				out = new PrintWriter(
						sock.getOutputStream(), 
						true
						);
			} catch (IOException ex) {
				System.err.println("[ERROR] " + ex.getMessage());
				errors++;
				if (errors >= MAX_ERRORS) {
					System.err.println("[ERROR] Too many errors, server shutting down.");
					acceptConnections = false;
				}
				continue;
			}
			errors = 0;
			
			out.println("Benvenuto nel server di " + COGNOME + " " + NOME + "!");
			out.println("Sei loggato al server come " + clientAddr);
			try {
				try {
					sock.setSoTimeout(10000);
				} catch (SocketException ex) {
					System.err.println("[ERROR] " + ex.getMessage());
					errors++;
					if (errors >= MAX_ERRORS) {
						System.err.println("[ERROR] Too many errors, server shutting down.");
						acceptConnections = false;
					}
					continue;
				}
				errors = 0;

				boolean acceptCommands = true;
				while (acceptCommands) {
					String cmd = getCmd(in);
					System.out.println("[CLIENT] " + cmd);

					switch (cmd.toLowerCase()) {
					case "help":
						out.println("Comandi disponibili:");
						out.println("- help: Mostra questo menu");
						out.println("- date: Mostra la data e l'ora attuali");
						out.println("- exit, quit, bye: Chiudi la connessione");
						out.println("- stop: Spegni il server");
						out.println("-----------");
						break;
					case "date":
						out.println("[SERVER] Ora esatta: " 
								+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
						break;
					case "stop":
						out.println("[SERVER] Chiusura del server in corso...");
						acceptConnections = false;
					case "bye":
					case "quit":
					case "exit":
						acceptCommands = false;
						break;
					default:
						out.println("[SERVER] " + cmd.toUpperCase());
						break;
					}

				} // acceptCommands

			} catch (SocketTimeoutException ex) {
				System.out.println("[SERVER] Connection with client timeout.");
				out.println("[SERVER] Tempo di inattività scaduto.");
			}

			long elapsedTime = System.currentTimeMillis() - beginConnection;
			out.println("Disconnessione. Tempo trascorso: " + elapsedTime + " ms.");

			try {
				in.close();
				out.close();
				sock.close();
			} catch (IOException ex) {
				System.err.println("[ERROR] " + ex.getMessage());
				errors++;
				if (errors >= MAX_ERRORS) {
					System.err.println("[ERROR] Too many errors, server shutting down.");
					acceptConnections = false;
				}
				continue;
			}

			System.out.println("[SERVER] Connection with client has been closed.");
			System.out.println("[SERVER] Elapsed time: " + elapsedTime + " ms");
		}

		try {
			ws.close();
		} catch (IOException ex) {
			System.err.println("[ERROR] " + ex.getMessage());
			return;
		}
		System.out.println("Server has stopped.");
	}


	private static String getClientAddr(Socket sock) {
		InetAddress addr = sock.getInetAddress();
		return (!addr.equals(sock.getLocalAddress()) ? addr.toString() : "localhost") 
				+ ":" + Integer.toString(sock.getPort());
	}

	private static String getCmd(BufferedReader in) throws SocketTimeoutException {
		try {
			return in.readLine().toLowerCase();
		} catch (Exception ex) {
			return "exit";
		}
	}
}
