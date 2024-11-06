package client.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientCLI {
	public static void main(String[] args) throws IOException {
		Socket sock = new Socket("localhost", 7979);
		
		BufferedReader socketInputStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		PrintWriter socketOutputStream = new PrintWriter(sock.getOutputStream(), true);
		
		ClientSender cs = new ClientSender(socketOutputStream);
		new Thread(cs).start();
		
		try {
			String msg;
			while ((msg = socketInputStream.readLine()) != null) {
				System.out.println(msg);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		cs.stopSending();
		
		socketInputStream.close();
		socketOutputStream.close();
		sock.close();
		
		System.exit(0);
	}
}
