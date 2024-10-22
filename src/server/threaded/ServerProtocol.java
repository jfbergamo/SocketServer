package server.threaded;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import utils.jlibs.MutexSemaphore;

public class ServerProtocol extends Thread {

	private static final String NOME = "Jacopo";
	private static final String COGNOME = "Bergamasco";

	private static final String PASSWORD = "loris";

	private Client client;
	private BufferedReader in;
	private PrintWriter out;
	private String nome;
	private String clientAddr;
	private boolean isAdmin;
	private List<Client> clients;
	private MutexSemaphore sem;

	public ServerProtocol(Client sock, List<Client> connections, MutexSemaphore sem) {
		client = sock;
		clients = connections;
		this.sem = sem;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		long beginConnection = System.currentTimeMillis();

		getClientAddr();
		System.out.println("INFO: Server accepted connection from " + clientAddr);

		try {
			in = new BufferedReader(
					new InputStreamReader(client.socket().getInputStream())
					);
			out = new PrintWriter(
					client.socket().getOutputStream(), 
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
				client.socket().setSoTimeout(10000);
			} catch (SocketException ex) {
				System.err.println("ERROR: Could not set client environment for " + clientAddr + ": " + ex.getMessage());
				return;
			}

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
						out.println("[SERVER] Client connessi: " + clients.size());
						for (Client s : clients) {
							out.println("]\t" + s.nome());
						}
						break;
					case "stop":
					case "shutdown":
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
						sem.P();
						out.println("[" + client.nome() + "] " + cmd);
						sem.V();
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

	private void exit() {
		sem.P();
		clients.remove(client);
		sem.V();

		try {
			in.close();
			out.close();
			client.socket().close();
		} catch (IOException ex) {
			System.err.println("ERROR: Couldn't close file descriptor for client " + clientAddr + ", garbage collector should close them anyway.");
			return;
		}
	}

	private void doAuth() {
		out.println("Benvenuto nel server di " + COGNOME + " " + NOME + "!");
		out.println("Per favore, inserisci il tuo nome: ");
		String[] auth = getCmd().split("@");
		nome = auth[0];
		isAdmin = auth.length > 1 && auth[1].equals(PASSWORD);
		out.println("Sei loggato al server come " + nome + "[" + clientAddr + "]");
		client.setNome(nome);
	}

	private void getClientAddr() {
		InetAddress addr = client.socket().getInetAddress();
		clientAddr = (!addr.equals(client.socket().getLocalAddress()) ? addr.toString() : "localhost") 
				+ ":" + Integer.toString(client.socket().getPort());
	}

	private String getCmd() {
		out.print("> ");
		out.flush();
		try {
			return in.readLine().trim();
		} catch (Exception ex) {
			return "exit";
		}
	}

}
