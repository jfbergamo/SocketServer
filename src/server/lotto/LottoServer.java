package server.lotto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class LottoServer {
	
	public static final int PORT = 7979;
	
	private static boolean accept;
	private static ServerSocket ws;
	
	public static void main(String[] args) {		
		try {
			ws = new ServerSocket(PORT);
		} catch (IOException ex) {
			handle(ex, "Impossibile creare welcoming socket");
			return;
		}
	
		System.out.println("Server avviato su porta " + PORT);
		
		accept = true;
		new Thread(() -> {
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			String con;
			while (accept) {
				try {
					con = stdin.readLine();
				} catch (IOException ex) { return; }
				System.out.println(con);
				if (con.equals("stop")) {
					stop();
				}
			}
		}).start();
		while (accept) {
			Socket socket;
			try {
				socket = ws.accept();
			} catch (IOException ex) {
				continue;
			}
			new LottoServerProtocol(socket).start();
		}
		stop();
	}
	
	protected static void stop() {
		accept = false;
		try {
			if (!ws.isClosed()) {
				ws.close();
			}
		} catch (IOException ex) {
			handle(ex, "Errore nella chiusura del server, ma tanto il programma e' finito quindi fara' tutto il gc");
		}
	}
	
	protected static void handle(Exception ex, String msg) {
		System.err.println("ERROR: " + msg + ": " + ex.getMessage());
	}
}
