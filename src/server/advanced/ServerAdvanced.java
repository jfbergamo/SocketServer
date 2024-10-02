package server.advanced;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class ServerAdvanced {

	public static final String NOME = "Jacopo";
	public static final String COGNOME = "Bergamasco";

	public static void main(String[] args) throws IOException {
		ServerSocket ws = new ServerSocket(7979); // Welcoming Socket
		System.out.println("[SERVER] Listening on port " + ws.getLocalPort());	

		boolean acceptConnections = true;
		while (acceptConnections) {
			Socket sock = ws.accept();
			sock.setSoTimeout(10000);
			long beginConnection = System.currentTimeMillis();

			InetAddress addr = sock.getInetAddress();
			String clientAddr = (!addr.equals(sock.getLocalAddress()) ? addr.toString() : "localhost") 
					+ ":" + Integer.toString(sock.getPort());
			System.out.println("[SERVER] Accepted connection from " + clientAddr);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(sock.getInputStream())
					);
			PrintWriter out = new PrintWriter(
					sock.getOutputStream(), 
					true
					);

			out.println("Benvenuto nel server di " + COGNOME + " " + NOME + "!");
			out.println("Sei loggato al server come " + clientAddr);

			boolean acceptCommands = true;
			try {
				while (acceptCommands) {
					String inputCmd = in.readLine();
					if (inputCmd == null) inputCmd = "exit";
					System.out.println("[CLIENT] " + inputCmd);

					Command cmd = StringToCommand(inputCmd);
					switch (cmd) {
						case ECHO:
							out.println("[SERVER] " + inputCmd);
							break;
						case HELP:
							out.println("Comandi disponibili:");
							out.println("- help: Mostra questo menu");
							out.println("- date: Mostra la data e l'ora attuali");
							out.println("- exit, quit, bye: Chiudi la connessione");
							out.println("- stop: Spegni il server");
							out.println("-----------");
							break;
						case DATE:
							out.println("[SERVER] Ora esatta: " 
									+ LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
							break;
						case EXIT:
							acceptCommands = false;
							break;
						case STOP:
							out.println("[SERVER] Chiusura del server in corso...");
							acceptCommands = false;
							acceptConnections = false;
							break;
						default:
							acceptCommands = false;
							acceptConnections = false;
							System.err.println("Unreachable");
							break;
					}

				}
			} catch (SocketTimeoutException ex) {
				System.out.println("[SERVER] Connection with client timeout.");
				out.println("[SERVER] Tempo di attesa scaduto.");
			}

			long elapsedTime = System.currentTimeMillis() - beginConnection;
			out.println("Disconnessione. Tempo trascorso: " + elapsedTime + " ms.");

			in.close();
			out.close();
			sock.close();

			System.out.println("[SERVER] Connection with client has been closed.");
			System.out.println("[SERVER] Elapsed time: " + elapsedTime + " ms");
		}
		ws.close();
		System.out.println("Server has stopped.");
	}

	public static Command StringToCommand(String cmd) {
		@SuppressWarnings("serial")
		HashMap<String, Command> map = new HashMap<>() {{
			put("help", Command.HELP);
			put("date", Command.DATE);
			put("quit", Command.EXIT);
			put("exit", Command.EXIT);
			put("bye",  Command.EXIT);
			put("stop", Command.STOP);
		}};
		Command command = map.get(cmd);
		return command != null ? command : Command.ECHO;
	}

}
