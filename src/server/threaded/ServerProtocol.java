package server.threaded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerProtocol implements Runnable {
	
	private static final String NOME = "Jacopo";
	private static final String COGNOME = "Bergamasco";
	
	private Socket socket;
	
	public ServerProtocol(Socket s) {
		socket = s;
	}
	
	@SuppressWarnings("unused")
	@Override
	public void run() {
		
		long beginConnection = System.currentTimeMillis();

		String clientAddr = getClientAddr(socket);
		System.out.println("INFO: Server accepted connection from " + clientAddr);

		BufferedReader in;
		PrintWriter out;
		try {
			in = new BufferedReader(
					new InputStreamReader(socket.getInputStream())
					);
			out = new PrintWriter(
					socket.getOutputStream(), 
					true
					);
		} catch (IOException ex) {
			System.err.println("ERROR: Unable to interface with client " + clientAddr + ": " + ex.getMessage());
			return;
		}
		
		out.println("Benvenuto nel server di " + COGNOME + " " + NOME + "!");
		out.println("Per favore, inserisci il tuo nome: ");
		String nome = getCmd(in);
		out.println("Sei loggato al server come " + nome + "[" + clientAddr + "]");
		try { // Socket Timeout
			if (ServerThreaded.DO_TIMEOUT) try {
				socket.setSoTimeout(10000);
			} catch (SocketException ex) {
				System.err.println("ERROR: Could not set client environment for " + clientAddr + ": " + ex.getMessage());
				return;
			}

			boolean acceptCommands = true;
			while (acceptCommands) {
				String cmd = getCmd(in);
				System.out.println("[" + nome + "] " + cmd);

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
					ServerThreaded.stop();
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

			if (false) throw new SocketTimeoutException(); // Trucchetto non troppo bello da vedere
		} catch (SocketTimeoutException ex) {
			System.out.println("SERVER: Connection with client timeout.");
			out.println("[SERVER] Tempo di inattività scaduto.");
		}

		long elapsedTime = System.currentTimeMillis() - beginConnection;
		out.println("Disconnessione. Tempo trascorso: " + elapsedTime + " ms.");

		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException ex) {
			System.err.println("ERROR: Couldn't close file descriptor for client " + clientAddr + ", garbage collector should close them anyway.");
			return;
		}
		
		return;
	}

	private String getClientAddr(Socket sock) {
		InetAddress addr = sock.getInetAddress();
		return (!addr.equals(sock.getLocalAddress()) ? addr.toString() : "localhost") 
				+ ":" + Integer.toString(sock.getPort());
	}

	private String getCmd(BufferedReader in) {
		try {
			return in.readLine().toLowerCase();
		} catch (Exception ex) {
			return "exit";
		}
	}
	
}
