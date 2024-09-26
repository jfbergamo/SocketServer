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
			System.out.println(InetAddress.getLocalHost());

			String addr = sock.getInetAddress().toString();
			String clientAddr = (!addr.equals("/0:0:0:0:0:0:0:1") ? addr : "localhost") 
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
				out.println("[SERVER] " + cmd);

				if (cmd.equalsIgnoreCase("quit") || cmd.equals("exit")) {
					if (cmd.equalsIgnoreCase("quit")) {
						go = false;
					}
					break;
				}
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
