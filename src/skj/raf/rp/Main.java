package skj.raf.rp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
	
	public static void main(String[] args) {
		if(args.length < 1) return;
		
		if(args.length == 1) {
			try {
				int remotePort = 9999;
				InetAddress remoteAddress = InetAddress.getByName(args[0]);
				
				RPWaitingSocket server = RPWaitingSocket.createIWaitingSocket(remotePort, remoteAddress);
				server.start();
			} catch (IOException e) {
				
			}
		} else {
			try {
				RPTransactionalSocket client = RPTransactionalSocket.createTransactionalSocket(9999, InetAddress.getByName(args[0]), 5000, InetAddress.getByName(args[1]));
				client.openTransaction();
				client.write("Test data to check for consistency".getBytes());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
