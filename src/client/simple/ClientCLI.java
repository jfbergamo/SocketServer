package client.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientCLI {
	public static void main(String[] args) throws IOException {
		Socket sock = new Socket("192.168.80.52", 7979);
		
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		PrintWriter outputStream = new PrintWriter(sock.getOutputStream(), true);
		
		ServerReceptionsHanlder srh = new ServerReceptionsHanlder(inputStream);
		Thread t = new Thread(srh);
		t.start();
		
		ClientSender cs = new ClientSender(outputStream);
		new Thread(cs).start();
		
		try {
			t.join();
		} catch (InterruptedException ex) {

		}
		cs.stopSending();
		
		inputStream.close();
		outputStream.close();
		sock.close();
		
		System.out.println("Fine main");
	}
}
