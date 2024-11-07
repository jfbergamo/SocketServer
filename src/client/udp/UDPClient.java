package client.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class UDPClient {

	public static final InetAddress SERVER = getAddrByName("192.168.80.52");
	public static final int PORT = 7979;
	
	public static final int PACKET_LEN = 1024;
	
	public static void main(String[] args) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		
		String msg = "Jacopo Bergamasco";
		byte[] data = msg.getBytes();
		
		DatagramPacket pkt = new DatagramPacket(data, data.length, SERVER, PORT);
		
		socket.send(pkt);
		
		pkt = new DatagramPacket(new byte[PACKET_LEN], PACKET_LEN);
		socket.receive(pkt);
		
		System.out.println(new String(pkt.getData()));
		
		socket.close();
	}
	
	public static final InetAddress getAddrByName(String name) {
		try {
			return InetAddress.getByName(name);
		} catch (UnknownHostException ex) {
			return null;
		}
	}
	
}