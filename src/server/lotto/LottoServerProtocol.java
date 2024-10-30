package server.lotto;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Set;

import utils.jlibs.MutexSemaphore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class LottoServerProtocol extends Thread {

	Socket client;

	private BufferedReader in;
	private PrintWriter out;

	private MutexSemaphore mutex;

	private String clientAddr;
	private boolean accept;

	private Set<Integer> rngSet;
	private long time;
	
	public LottoServerProtocol(Socket socket) {
		client = socket;
		mutex = new MutexSemaphore();
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
		
		new Thread(() -> {
			final long delay = 180;
			rngSet = new HashSet<Integer>();
			time = 0;
			Random rng = new Random(time);
			while (accept) {
				if (System.currentTimeMillis() - time >= delay*1000) {
					mutex.P();
					rngSet.clear();
					while (rngSet.size() < 5) {
						rngSet.add(rng.nextInt(30) + 1);
					}
					mutex.V();
					time = System.currentTimeMillis();
				}
				try {
					sleep(1000);
				} catch (InterruptedException ex) { 
					accept = false;
					return;
				}
			}
		}).start();
		
		while (accept) {
			String cmd = getCmd();
			String[] args = getArgs(cmd);
			if (args.length <= 0) continue;
			switch (args[0]) {
				case "lotto":
					// lotto 24, 12, 11, 23, 9
					String regex = "^\\s*lotto\\s+(([1-9]|[1-2][0-9]|30)(\\s*,\\s*|\\s+)){4}([1-9]|[1-2][0-9]|30)\\s*$";
					if (cmd.matches(regex)) {
						Set<Integer> userSet = new HashSet<Integer>();
						for (int i = 0; i < args.length; i++) {
							int n = Integer.parseInt(args[i + 1]);
							if (0 < n && n > 30) {
								
							} else {
								userSet.add(n);
							}
						}
						if (userSet.size() < 5) {
							out.println("Non puoi inserire lo stesso numero piu' volte");
						} else {
							lotto(userSet);
						}
					} else {
						out.println("Sintassi del comando invalida!");
					}
					break;
				case "draw":
					draw();
				case "show":
					out.println("I numeri estratti sono: " + Arrays.toString(rngSet.toArray()));
					out.println("Mancano " + (180 - (System.currentTimeMillis() - time)/1000) + "s alla prossima estrazione.");
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
			LottoServer.handle(ex, "Impossibile chiudere i buffer, affidiamoci al gc");
		}
	}

	private void lotto(Set<Integer> userSet) {
		int conta = 0;
		Iterator<Integer> user = userSet.iterator();
		Set<Integer> guessed = new HashSet<Integer>();
		mutex.P();
		Iterator<Integer> rng  =  rngSet.iterator();
		while (user.hasNext() && rng.hasNext()) {
			if (user.next().equals(rng.next())) {
				conta++;
			}
		}
		mutex.V();
		out.println("I numeri estratti sono: " + Arrays.toString(rngSet.toArray()));
		switch (conta) {
		case 0:
			out.println("HAI PERSO!");
			break;
		case 1:
			out.println("Ne hai azzeccato uno: " + Arrays.toString(guessed.toArray()));
			break;
		case 2:
			out.println("AMBO! " + Arrays.toString(guessed.toArray()));
			break;
		case 3:
			out.println("TERNO! " + Arrays.toString(guessed.toArray()));
			break;
		case 4:
			out.println("QUATERNA! " + Arrays.toString(guessed.toArray()));
			break;
		case 5:
			out.println("HAI VINTO! " + Arrays.toString(guessed.toArray()));
			break;
		}
		draw();
	}
	
	private void draw() {
		time = 0;
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
