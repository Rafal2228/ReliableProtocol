package skj.raf.rp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

public class RPTransactionalSocket implements ITransactionalSocket {

	private static final String _pre = "TransactionalSocket: ";
	private static final int SEGMENT_MAX_SIZE = 4096;
	
	private DatagramSocket _local;
	private byte[] _buffer;
	private DatagramPacket _packet;
	private boolean _transaction;
	private int _timeout = 10000;
	
	public RPTransactionalSocket() throws IOException{
		_local = new DatagramSocket();
		_local.setSoTimeout(_timeout);
		_buffer = new byte[SEGMENT_MAX_SIZE];
		_packet = new DatagramPacket(_buffer, SEGMENT_MAX_SIZE);
		_transaction = false;
	}
	
	public RPTransactionalSocket(int localPort, InetAddress localAddress) throws IOException {
		_local = new DatagramSocket(localPort, localAddress);
		_local.setSoTimeout(_timeout);
		_buffer = new byte[SEGMENT_MAX_SIZE];
		_packet = new DatagramPacket(_buffer, SEGMENT_MAX_SIZE);
		_transaction = false;
	}
	
	public static RPTransactionalSocket createTransactionalSocket(int remotePort, InetAddress remoteAddress, int localPort, InetAddress localAddress) {
		RPTransactionalSocket result = null;
		try {
			System.out.println("Creating client at: " + localAddress.getHostAddress() + ":" + localPort);
			result = new RPTransactionalSocket(localPort, localAddress);
			System.out.println("Sending packet to: " + remoteAddress + ":" + remotePort);
			result._packet.setData("Hello".getBytes());
			result._packet.setAddress(remoteAddress);
			result._packet.setPort(remotePort);
			result._local.send(result._packet);
			
			System.out.println("Waiting for respond: " + remoteAddress + ":" + remotePort);
			result._local.receive(result._packet);
			System.out.println("Recived: " + result._packet.getData().toString());
			System.out.println("Connecting to: " + result._packet.getAddress().getHostAddress() + ":" + result._packet.getPort());
			result._local.connect(result._packet.getSocketAddress());
		} catch (IOException e) {
			System.out.println(_pre + "Error while creating transactional socket");
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public byte[] read() throws IOException {
		if(_transaction) {
			_local.receive(_packet);
		}
		else {
			System.out.println(_pre + "Transaction is not opened");
			throw new IOException();
		}
		return _packet.getData();
	}

	@Override
	public void write(byte[] buff) {
		if(_transaction) {
			boolean sent = false;
			while(!sent) {
				_packet.setData(buff);
				try {
					_local.send(_packet);
					sent = true;
				} catch (IOException e) {
					System.out.println(_pre + "Timeout, retrying...");
					write(buff);
				}
			}
		}
	}

	@Override
	public void openTransaction() throws IOException {
		if(_local.isConnected()) {
			_transaction = true;
		} else {
			System.out.println(_pre + "Transactional Socket is not connected");
		}
	}

	@Override
	public void closeTransaction() throws IOException {
		if(_transaction){
			_transaction = false;
		} else {
			throw new IOException("Transaction not opened");
		}
	}

	@Override
	public void abortTransaction() throws IOException {
		if(_transaction){
			_transaction = false;
			_local.disconnect();
			_local.close();
		} else {
			throw new IOException("Transaction not opened");
		}
	}

	@Override
	public void close() throws IOException {
		if(_transaction) abortTransaction();
		else _local.disconnect();
	}

	@Override
	public void setTimeout(int readTimeout) {
		try {
			_local.setSoTimeout(readTimeout);
		} catch (IOException e) {
			System.out.println(_pre + "Error while setting new timeout");
		}
	}

	@Override
	public int getTimeout() {
		return _timeout;
	}

	@Override
	public void setSegmentSize(int size) {
		System.out.println("Implemented as constant");
	}

	@Override
	public int getSegmentSize() {
		return SEGMENT_MAX_SIZE;
	}

	@Override
	public int getLocalPort() {
		return _local.getLocalPort();
	}

	@Override
	public InetAddress getLocalAddress() {
		return _local.getLocalAddress();
	}

	@Override
	public int getRemotePort() {
		return _local.getPort();
	}

	@Override
	public InetAddress getRemoteAddress() {
		return _local.getInetAddress();
	}
	
	public void connect(SocketAddress addr) throws IOException {
		if(!_local.isConnected()) {
			_packet.setSocketAddress(addr);
			_packet.setData("Ack".getBytes());
			_local.send(_packet);
			_local.connect(addr);
		}
	}

}
