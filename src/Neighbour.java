import java.net.InetAddress;
import java.net.UnknownHostException;

public class Neighbour {

	private InetAddress ipAddress;
	private int port;

	public Neighbour(String ipAddress, String port) {
		try {
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			System.out.println(ipAddress+" ip address cannot be convert to InetAddress ");
			e.printStackTrace();
		}
		this.port = Integer.parseInt(port);
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

}
