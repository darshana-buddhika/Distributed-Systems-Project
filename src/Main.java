import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			UDPClient client_one = new UDPClient(10001, "darshana");
			client_one.registerNetwork();
			
//			client_one.unregisterNetwork();
			client_one.closeSocket();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
