package server.lotto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class LottoServerProtocol extends Thread {

	Socket client;

	private BufferedReader in;
	private PrintWriter out;

	private String clientAddr;
	
	private boolean accept;

	public LottoServerProtocol(Socket socket) {
		client = socket;
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream());
		} catch (IOException ex) {
			LottoServer.handle(ex, "Impossibile creare buffer di lettura e scrittura per client");
			return;
		}

		getClientAddr();
		System.out.println("Client " + clientAddr + " connesso.");
		
		accept = true;
		while (accept) {
			String cmd = getCmd();
			String[] args = getArgs(cmd);
			if (args.length <= 0) continue;
			switch (args[0]) {
				case "lotto":
					// lotto 24, 12, 11, 23, 9
					String regex = "^\\s*lotto\\s+(([1-9]|[1-2][0-9]|30)(\\s*,\\s*|\\s+)){4}([1-9]|[1-2][0-9]|30)\\s*$";
					if (cmd.matches(regex)) {
						int[] userN = new int[5];
						for (int i = 0; i < userN.length; i++) {
							userN[i] = Integer.parseInt(args[i + 1]);
						}
					} else {
						out.println("Sintassi del comando invalida!");
					}
					break;
				case "bye":
				case "quit":
				case "exit":
					accept = false;
				default:
					out.println(cmd);
					break;
			}
		}
		
		System.out.println("Client " + clientAddr + " disconnesso.");
		
		try {
			in.close();
			out.close();
			client.close();
		} catch (IOException ex) {
			LottoServer.handle(ex, "Impossibile chiudere i descrittori, penso che Java possa farcela comunque.");
		}
	}

	private String getCmd() {
		out.print("\r> ");
		out.flush();
		try {
			return in.readLine().trim();
		} catch (Exception ex) {
			return "exit";
		}
	}
	
	private String[] getArgs(String cmd) {
		return cmd.split("(\\s*,\\s*|\\s+)");
	}
	
	private void getClientAddr() {
		InetAddress addr = client.getInetAddress();
		clientAddr = (!addr.equals(client.getLocalAddress()) ? addr.toString() : "localhost") 
				+ ":" + Integer.toString(client.getPort());
	}
	
}
