package server.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
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

		String header = "";
		
		try {
			// TODO: leggere l'intero header
			header += in.readLine();
		} catch (IOException ex) {
			return;
		}
		
		System.out.println(header);
		
		try {
			switch (parseUrl(header)) {
			case "/":
				char[] content = loadFromFile("index.html", "text/html");
				out.write(content);
				out.flush();
				break;
			default:
				throw new FileNotFoundException();
			}
			
		} catch (FileNotFoundException ex) {
			out.println("HTTP/1.1 404 Not Found");
			out.println("Content-Type: text/html");
			out.println("");
			out.println("<h1>404</h1>");
		}

		try {
			out.close();
			in.close();
			sock.close();
		} catch (IOException ex) {}
		
		return;
	}
	
	// TODO: Gestire metodi diversi da GET
	private String parseUrl(String header) {
		String[] req = header.split("\\r\\n")[0].split(" ");
//		String method = req[0];
		String path = req[1];
		return path;
	}
	
	private char[] loadFromFile(String path, String mime) throws FileNotFoundException {
		File index = new File("./src/server/http/" + path);
		Scanner read = new Scanner(index);
		
		List<Character> content = new ArrayList<Character>();
		
		for (char c : ("HTTP/1.1 200 OK\r\n" + "Content-Type: " + mime + "\r\n\r\n").toCharArray()) {
			content.add(c);
		}
		
		while (read.hasNext()) {
			for (char c : (read.next() + " ").toCharArray()) {
				content.add(c);
			}
		}
		
		read.close();
		return CharacterListToCharArray(content);
	}
	
	private char[] CharacterListToCharArray(List<Character> list) {
		char[] array = new char[list.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = list.get(i);
		}
		return array;
	}
}
