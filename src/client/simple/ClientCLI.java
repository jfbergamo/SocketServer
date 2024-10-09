package client.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientCLI {
	public static void main(String[] args) throws IOException {
		Socket sock = new Socket("localhost", 7979);
		
		BufferedReader sockIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		PrintWriter sockOut = new PrintWriter(sock.getOutputStream(), true);
		
		System.out.println(sockIn.readLine());
		sockOut.println("Ciao");
		
		sock.close();
	}
}
