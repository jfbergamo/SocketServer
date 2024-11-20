package server.http;

import java.io.IOException;
import java.net.ServerSocket;

public class HTTPServer {
	
	public static final int PORT = 8080;
	
	public static void main(String[] args) {
		ServerSocket ws;
		try {
			ws = new ServerSocket(PORT);
		} catch (IOException ex) {
			return;
		}
		
		boolean go = true;
		while (go) {
			try {
				new Thread(new HTTPProtocol(ws.accept())).start();
			} catch (IOException ex) {
				continue;
			}
		}
		
		try {
			ws.close();
		} catch (IOException ex) {
			return;
		}
	}
}
