package client.simple;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerReceptionsHanlder implements Runnable {
	
	private BufferedReader inputReader;
	
	public ServerReceptionsHanlder(BufferedReader inputReader) {
		this.inputReader = inputReader;
	}
	
	public void run() {
		while (true) {
			String serverOutput;
			try {
				serverOutput = inputReader.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
				break;
			}
			if (serverOutput != null) {
				System.out.println(serverOutput);
			} else {
				break;
			}
		}
		System.out.println("Fine receiver");
	}
}
