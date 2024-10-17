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
import java.util.List;

public class ServerProtocol implements Runnable {
	
	private static final String NOME = "Jacopo";
	private static final String COGNOME = "Bergamasco";
	
	private static final String PASSWORD = "loris";
	
	private Socket socket;
	private List<Socket> sockets;
	
	public ServerProtocol(Socket s, List<Socket> connections) {
		socket = s;
		sockets = connections;
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
		String[] auth = getCmd(in).split("@");
		String nome = auth[0];
		boolean isAdmin = auth.length > 1 && auth[1].equals(PASSWORD);
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
					out.println("- list: Mostra la lista dei client");
					out.println("- stop: Spegni il server (solo admin)");
					out.println("-----------");
					break;
				case "date":
					out.println("[SERVER] Ora esatta: " 
							+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
					break;
				case "list":
					out.println("[SERVER] Client connessi: " + sockets.size());
					for (Socket s : sockets) {
						out.println(s.getInetAddress());
					}
					break;
				case "stop":
					if (isAdmin) {
						out.println("[SERVER] Chiusura del server in corso...");
						ServerThreaded.stop();
					} else {
						out.println("[SERVER] ATTENZIONE: Non disponi dei permessi di amministratore per eseguire tale azione.");
						break;
					}
				case "bye":
				case "quit":
				case "exit":
				case "logout":
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
		
		exit(in, out, socket, clientAddr);
		
		return;
	}
	
	private void exit(BufferedReader in, PrintWriter out, Socket socket, String clientAddr) {
		sockets.remove(socket);
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException ex) {
			System.err.println("ERROR: Couldn't close file descriptor for client " + clientAddr + ", garbage collector should close them anyway.");
			return;
		}
	}
	
	private String getClientAddr(Socket sock) {
		InetAddress addr = sock.getInetAddress();
		return (!addr.equals(sock.getLocalAddress()) ? addr.toString() : "localhost") 
				+ ":" + Integer.toString(sock.getPort());
	}

	private String getCmd(BufferedReader in) {
		try {
			return in.readLine().trim();
		} catch (Exception ex) {
			return "exit";
		}
	}
	
}
