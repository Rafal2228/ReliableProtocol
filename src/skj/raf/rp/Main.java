package skj.raf.rp;

import java.io.IOException;
import java.net.InetAddress;

public class Main {
	
	public static void main(String[] args) {
		try {
			int remotePort = 9999;
			InetAddress remoteAddress = InetAddress.getByName("localhost");
			int localPort = 5000;
			
			IWaitingSocket server = RPWaitingSocket.createIWaitingSocket(remotePort, remoteAddress);
			ITransactionalSocket client = RPTransactionalSocket.createTransactionalSocket(server.getPort(), server.getAddress(), localPort, remoteAddress);
		} catch (IOException e) {
			
		}
	}
}
