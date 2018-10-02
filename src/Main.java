import java.net.SocketException;
import java.net.UnknownHostException;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			UDPClient client_one = new UDPClient(15000, "darshana");
			client_one.registerNetwork();
			
//			client_one.unregisterNetwork();
//			client_one.closeSocket();
			
			UDPClient client_two = new UDPClient(16000, "fyp");
			client_two.registerNetwork();
			
			UDPClient client_3 = new UDPClient(17000, "three");
			client_3.registerNetwork();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
