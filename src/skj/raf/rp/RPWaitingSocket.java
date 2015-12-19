package skj.raf.rp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.TreeSet;

public class RPWaitingSocket implements IWaitingSocket {

	private static final String _pre = "IWaitingSocket: ";
	private static final int SEGMENT_MAX_SIZE = 4096;
	
	private DatagramSocket _socket;
	private ArrayList<RPTransactionalSocket> _connections;
	private DatagramPacket _packet;
	private byte[] _buffer;
	private boolean _closed = false;
	
	
	private RPWaitingSocket(int port, InetAddress address) throws IOException{
		_socket = new DatagramSocket(port, address);
		_socket.setBroadcast(true);
		_connections = new ArrayList<>();
		_buffer = new byte[SEGMENT_MAX_SIZE];
		_packet = new DatagramPacket(_buffer, SEGMENT_MAX_SIZE);
	}
	
	public static RPWaitingSocket createIWaitingSocket(int port, InetAddress address) throws IOException{
		return new RPWaitingSocket(port, address);
	}
	
	@Override
	public ITransactionalSocket listen() throws IOException {
		System.out.println("Waiting for client...");
		_socket.receive(_packet);
		System.out.println("Client connected from:" + _packet.getAddress().getHostAddress() + ":" + _packet.getPort() );
		System.out.println("Msg: " + new String(_packet.getData(), Charset.defaultCharset()));
		RPTransactionalSocket connection = new RPTransactionalSocket();
		System.out.println("Creating response:");
		connection.connect(_packet.getSocketAddress());
		_connections.add(connection);
		return connection;
	}

	@Override
	public int getPort() {
		return _socket.getLocalPort();
	}

	@Override
	public InetAddress getAddress() {
		return _socket.getLocalAddress();
	}

	@Override
	public void close() throws IOException {
		for(RPTransactionalSocket connection : _connections) {
			connection.close();
			_closed = true;
		}
	}
	
	public void start() throws IOException{
		System.out.println("Creating server at: " + _socket.getLocalAddress() + ":" + _socket.getLocalPort());
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!_closed) {
					try {
						ITransactionalSocket client = listen();
					} catch (IOException e) {
						System.out.println(_pre + "Error with client connection");
					}
				}
			}
		}).start();
	}

}
