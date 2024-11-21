package server.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPServer {
	
	public static final int PORT = 8080;
	
	private int port;
	private boolean go;
	
	public HTTPServer(int port) {
		this.port = port;
	}
	
	public void start() {
		ServerSocket ws;
		try {
			ws = new ServerSocket(port);
		} catch (IOException ex) {
			return;
		}
		
		System.out.println("Server HTTP avviato su http://localhost:" + port);
		
		go = true;
		ExecutorService pool = Executors.newCachedThreadPool();
		while (go) {
			try {
				pool.execute(new HTTPProtocol(ws.accept()));
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
	
	public static void main(String[] args) {
		HTTPServer server = new HTTPServer(PORT);
		server.start();
	}
}
