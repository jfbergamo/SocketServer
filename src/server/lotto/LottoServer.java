package server.lotto;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LottoServer {
	
	public static final int PORT = 7979;
	
	public static void main(String[] args) {
		ServerSocket ws;
		try {
			ws = new ServerSocket(PORT);
		} catch (IOException ex) {
			handle(ex, "Impossibile creare welcoming socket");
			return;
		}
	
		System.out.println("Server avviato su porta " + PORT);
		
		boolean accept = true;
		while (accept) {
			Socket socket;
			try {
				socket = ws.accept();
			} catch (IOException ex) {
				continue;
			}
			new LottoServerProtocol(socket).start();
		} 
	}
	
	protected static void handle(Exception ex, String msg) {
		System.err.println("ERROR: " + msg + ": " + ex.getMessage());
	}
}
