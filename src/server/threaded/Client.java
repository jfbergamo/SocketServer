package server.threaded;

import java.net.Socket;

public class Client {
	private Socket s;
	private String n;
	
	public Client(Socket sock) {
		s = sock;
		n = "unknown";
	}
	
	public Socket socket() {
		return s;
	}
	
	public void setNome(String nome) {
		n = nome;
	}
	
	public String nome() {
		return n;
	}
}
