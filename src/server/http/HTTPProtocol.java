package server.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class HTTPProtocol implements Runnable {

	private Socket sock;

	private BufferedReader in;
	private PrintWriter out;

	public HTTPProtocol(Socket s) {
		sock = s;
	}

	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream());
		} catch (IOException ex) {
			return;
		}

//		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		
		try {
			in.readLine();
		} catch (IOException ex) {
			return;
		}

		try {
			File index = new File("./src/server/http/index.html");
			Scanner read = new Scanner(index);
			
			out.println("HTTP/1.1 200 OK");
			out.println("Content-Type: text/html");
			out.println("");
			
			while (read.hasNextLine()) {
				out.println(read.nextLine());
			}
			
			read.close();
		} catch (FileNotFoundException ex) {
			out.println("HTTP/1.1 HTTP/1.1 404 Not Found");
			out.println("Content-Type: text/html");
			out.println("");
			out.println("<h1>404</h1>");
		}

		try {
			out.close();
			in.close();
			sock.close();
		} catch (IOException ex) {
		}
		return;
	}
}
