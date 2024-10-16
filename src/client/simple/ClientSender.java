package client.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClientSender implements Runnable {
	
	private PrintWriter toServer;
	private boolean send;
	
	private BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));
	
	public ClientSender(PrintWriter outputStream) {
		toServer = outputStream;
		send = true;
	}
	
	public void run() {
		while (send) {
			try {
				toServer.println(clientInput.readLine());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void stopSending() {
		send = false;
		toServer.close();
	}
}
