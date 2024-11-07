package client.udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPClient {

	private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	
	public static final InetAddress SERVER = getAddrByName("localhost");
	public static final int PORT = 7979;
	
	public static final int PACKET_LEN = 1024;
	
	public static void main(String[] args) {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			return;
		}
		
		String msg;
		while (true) {
			msg = input();
			byte[] data = msg.getBytes();

			DatagramPacket pkt = new DatagramPacket(data, data.length, SERVER, PORT);

			try {
				socket.send(pkt);
			} catch (IOException ex) {
				System.err.println(ex.getMessage());
				socket.close();
				return;
			}

			pkt = new DatagramPacket(new byte[PACKET_LEN], PACKET_LEN);
			try {
				socket.receive(pkt);
			} catch (IOException ex) {
				System.err.println(ex.getMessage());
				socket.close();
				return;
			}
			String res = new String(pkt.getData()).trim();
			if (res.equals("@quit")) break;
			System.out.println(res);
		}
		socket.close();
	}
	
	public static final InetAddress getAddrByName(String name) {
		try {
			return InetAddress.getByName(name);
		} catch (UnknownHostException ex) {
			return null;
		}
	}
	
	public static String input() {
		System.out.print("> ");
		try {
			return stdin.readLine();
		} catch (IOException ex) {
			return "";
		}
	}
	
}