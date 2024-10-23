package server.threaded;

import java.util.ArrayList;

import utils.jlibs.MutexSemaphore;

public class ProtocolList extends ArrayList<ServerProtocol> {
	private MutexSemaphore sem;
	
	public ProtocolList(MutexSemaphore s) {
		super();
		sem = s;
	}
	
	public void broadcast(String message) {
		sem.P();
		for (ServerProtocol s : this) {
			s.globalMessage(message);
		}
		sem.V();
	}
}
