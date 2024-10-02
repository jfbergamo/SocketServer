package server.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSimple {

	public static final String NOME = "Jacopo";
	public static final String COGNOME = "Bergamasco";

	public static void main(String[] args) throws IOException {

		ServerSocket ws = new ServerSocket(7979); // Welcoming Socket
		System.out.println("[SERVER] Listening on port " + ws.getLocalPort());	

		boolean go = true;
		while (go) {
			Socket sock = ws.accept();
			
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

			while (true) {
				String cmd = in.readLine();
				if (cmd == null) cmd = "exit";
				System.out.println("[CLIENT] " + cmd);

				if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit") || cmd.equalsIgnoreCase("stop")) {
					if (cmd.equalsIgnoreCase("stop")) {
						go = false;
					}
					break;
				}
				out.println("[SERVER] " + cmd.toUpperCase());
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

}
