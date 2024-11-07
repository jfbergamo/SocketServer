package server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {

	public static final int PORT = 7979;

	public static final int PACKET_LEN = 1024;

	public static void main(String[] args) {
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(PORT);
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			return;
		}

		DatagramPacket pkt;

		boolean go = true;
		while (go) {
			pkt = new DatagramPacket(new byte[PACKET_LEN], PACKET_LEN);
			try {
				socket.receive(pkt);
			} catch (IOException ex) {
				System.err.println(ex.getMessage());
				socket.close();
				return;
			}

			String msg = new String(pkt.getData()).trim();
			if (msg.equals("quit")) {
				msg = "@quit";
				go = false;
			}
			byte[] data = msg.getBytes();
			
			try {
				socket.send(new DatagramPacket(data, data.length, pkt.getAddress(), pkt.getPort()));
			} catch (IOException ex) {
				System.err.println(ex.getMessage());
			}
		}

		socket.close();
	}

}