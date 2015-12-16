package skj.raf.rp;

import java.io.IOException;
import java.net.InetAddress;

public interface IWaitingSocket extends Cloneable {
	public ITransactionalSocket listen() throws IOException;
	public int getPort();
	public InetAddress getAddress();
	public void close() throws IOException;
}
