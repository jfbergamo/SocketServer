package server.threaded;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import utils.jlibs.MutexSemaphore;

public class ServerProtocol implements Runnable {

	private static final String NOME = "Jacopo";
	private static final String COGNOME = "Bergamasco";

	private static final String PASSWORD = "loris";

	private BufferedReader in;
	private PrintWriter out;

	private Socket socket;
	private String nome;
	private String clientAddr;
	private boolean isAdmin;
	
	private ProtocolList protocols;
	private MutexSemaphore sem;

	public ServerProtocol(Socket sock, ProtocolList connections, MutexSemaphore sem) {
		socket = sock;
		protocols = connections;
		this.sem = sem;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		sem.P();
		protocols.add(this);
		sem.V();
		
		long beginConnection = System.currentTimeMillis();

		getClientAddr();
		System.out.println("INFO: Server accepted connection from " + clientAddr);

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

		// Potrei cambiarlo successivamente
		doAuth();

		try { // Socket Timeout
			if (ServerThreaded.DO_TIMEOUT) try {
				socket.setSoTimeout(60000);
			} catch (SocketException ex) {
				System.err.println("ERROR: Could not set client environment for " + clientAddr + ": " + ex.getMessage());
				return;
			}

			protocols.broadcast("[SERVER] " + nome + " si sta connettendo.", this);
			boolean acceptCommands = true;
			while (acceptCommands) {
				String cmd = getCmd();
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
						out.println("[SERVER] Client connessi: " + protocols.size());
						for (ServerProtocol p : protocols) {
							out.println("]\t" + p.nome);
						}
						break;
					case "stop":
					case "shutdown":
						if (isAdmin) {
							out.println("[SERVER] Chiusura del server in corso...");
							ServerThreaded.stop();
						} else {
							out.println("[SERVER] ATTENZIONE: Non disponi dei permessi di amministratore per eseguire tale azione.");
						}
						break;
					case "kick":
						if (isAdmin) {
							out.println("[SERVER] Non implementato, dovrai aspettare ancora un po'");
						} else {
							out.println("[SERVER] ATTENZIONE: Non disponi dei permessi di amministratore per eseguire tale azione.");
						}
						break;
					case "bye":
					case "quit":
					case "exit":
					case "logout":
						protocols.broadcast("[SERVER] " + nome + " si sta disconnettendo.", this);
						acceptCommands = false;
					case "":
						break;
					default:
						//out.println("[" + nome + "] " + cmd);
						protocols.broadcast("[" + nome + "] " + cmd, this);
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

		exit();

		return;
	}

	public void globalMessage(String message) {
		out.println("\r" + message);
		out.print("> ");
		out.flush();
	}
	
	private void exit() {
		sem.P();
		protocols.remove(this);

		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException ex) {
			System.err.println("ERROR: Couldn't close file descriptor for client " + clientAddr + ", garbage collector should close them anyway.");
			return;
		}
		sem.V();
	}

	private void doAuth() {
		out.println("Benvenuto nel server di " + COGNOME + " " + NOME + "!");
		
		out.println("Per favore, inserisci il tuo nome: ");
		nome = getCmd();
		isAdmin = nome.equals("admin@" + PASSWORD);
		if (isAdmin) nome = "admin";
		out.println("Sei loggato al server come " + nome + "[" + clientAddr + "]");
	}

	private void getClientAddr() {
		InetAddress addr = socket.getInetAddress();
		clientAddr = (!addr.equals(socket.getLocalAddress()) ? addr.toString() : "localhost") 
				+ ":" + Integer.toString(socket.getPort());
	}
	
	private String getCmd() {
		
		out.print("\r> ");
		out.flush();
		try {
			return in.readLine().trim();
		} catch (Exception ex) {
			return "exit";
		}
	}
	
}
