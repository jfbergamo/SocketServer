package server.threaded;

import java.net.SocketOption;
import java.util.HashMap;

public class SocketAuth implements SocketOption<HashMap<String, String>> {

	@Override
	public String name() {
		return "SocketAuth";
	}

	@Override
	public Class<HashMap<String, String>> type() {
		return null;
	}

	
	
}
