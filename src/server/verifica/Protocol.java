package server.verifica;

//Bergamasco Jacopo, 5AIA, A.S. 2024-2025

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import utils.jlibs.MutexSemaphore;

public class Protocol implements Runnable {

	private MutexSemaphore mutex;

	private Socket sock;
	private BufferedReader in;
	private PrintWriter out;

	private boolean accept;

	private boolean echoMode;
	private int ans;
	
	public Protocol(Socket s, MutexSemaphore mut) {
		sock = s;
		mutex = mut;
		echoMode = true;
	}

	public void run() {
		// Inizializzazione buffer
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch (IOException ex) {
			Server.handle(ex, "Impossibile inizializzare buffer rw");
		}

		// Notifiche di connessione
		out.println("Bergamasco Jacopo");
		System.out.println(sock.getInetAddress() + ":" + sock.getPort() + " si e' connesso.");

		// Ricezione comandi
		accept = true;
		while (accept) {
			if (echoMode) {
				EchoMode();
			} else {
				// CalcMode restituisce false se il calcolo e' stato abortito o resettato
				if (!CalcMode()) ans = 0;
			}
		}

		// Disconnessione, chiusure buffer e rimozione dalla lista di client connessi
		System.out.println("Disconnessione di " + sock.getInetAddress() + ":" + sock.getPort());
		mutex.P();
		// Sezione critica che gestisce il contatore di client connessi
		Server.connections--;
		mutex.V();
		try {
			in.close();
			out.close();
			sock.close();
		} catch (IOException ex) {
			Server.handle(ex, "Errore nella chiusura dei buffer, il gc se la cavera'");
		}
	}

	private void EchoMode() {
		String cmd = getCmd();
		if (needsToExit(cmd)) return;
		switch (cmd) {
		case "client":
			// Controllo client connessi effettuata in mutua esclusione
			mutex.P();
			out.println("Ci sono " + Server.connections + " client connessi al server.");
			mutex.V();
			break;
		case "calc":
			echoMode = false;
			ans = 0; // Inizializzazione variabile ans, per modalita' calcolatrice
			out.println("CALC READY");
			break;
		default:
			out.println(cmd.toUpperCase()); // ECHO UPPERCASE
			break;
		}
	}

	private boolean CalcMode() {
		String cmd;
		int first, second;
		char op;

		// Primo operando
		cmd = getCmd();
		if (needsToExit(cmd)) return false;
		try {
			first = parseOperand(cmd);
		} catch (NumberFormatException ex) {
			out.println("ERR");
			return false;
		}

		// Operatore
		cmd = getCmd();
		if (needsToExit(cmd)) return false;
		try {
			op = parseOperator(cmd);
		} catch (Exception ex) {
			out.println("ERR");
			return false;
		}

		// Secondo operando
		cmd = getCmd();
		if (needsToExit(cmd)) return false;
		try {
			second = parseOperand(cmd);
		} catch (NumberFormatException ex) {
			out.println("ERR");
			return false;
		}
		
		// Calcolo del risultato
		switch (op) {
		case '+':
			ans = first + second;
			break;
		case '-':
			ans = first - second;
			break;
		case '*':
			ans = first * second;
			break;
		case '/':
			ans = first / second;
			break;
		case '%':
			ans = first % second;
			break;
		default:
			System.err.println("Irraggiungibile");
		}
		out.println(ans);
		return true;
	}

	private int parseOperand(String cmd) throws NumberFormatException {
		// Converte il comando in numero
		// Se il comando e' ans rimanda l'ultimo risultato
		// Se il comando non e' un numero tira una NumberFormatException
		if (cmd.equals("ans")) return ans;
		return Integer.parseInt(cmd);
	}
	
	private char parseOperator(String cmd) throws Exception {
		// Fa il parsing dell'operatore
		// In caso di operatore invalido tira una Exception generica
		if (cmd.length() == 1) {
			char op = cmd.charAt(0);
			switch (op) {
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				return op;
			}
		}
		throw new Exception("Valore operatore non valido");
	}
	
	private boolean needsToExit(String cmd) {
		// Controlla se il socket deve chiudere la sua connessione
		// Fa aggiuntivi controlli per la CalcMode
		if (!echoMode && (cmd.equals("abort") || cmd.equals("reset"))) {
			echoMode = cmd.equals("abort");
			out.println(cmd.equals("reset") ? "RESET OK" : "QUIT CALC");
			return true;
		}
		
		switch (cmd) {
		case "quit":
		case "exit":
			out.println("Bye");
			accept = false;
			return true;
		default:
			return false;
		}
	}

	private String getCmd() {
		// Ottiene un comando dal buffer di input
		// In caso di errore ritorna una stringa vuota
		out.print("> ");
		out.flush();
		try {
			return in.readLine().toLowerCase().trim();
		} catch (Exception ex) {
			return "";
		}
	}
}
