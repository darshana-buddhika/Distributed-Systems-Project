import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Scanner scanner = new Scanner(System.in);
		try {
			UDPClient client_one = new UDPClient(15000, "darshana");
			client_one.registerNetwork();

			
			UDPClient client_two = new UDPClient(16000, "ranula");
			client_two.registerNetwork();
			
			UDPClient client_three = new UDPClient(17000, "sudeepa");
			client_three.registerNetwork();
			
			while(true) {
				System.out.println("Press 1 for send message form client ONE");
				System.out.println("Press 2 for send message from client TWO");
				System.out.println("Press 3 for send message from client THREE");
				
				int input = scanner.nextInt();
				
				switch (input) {
				case 1:
					System.out.println("You have pressed 1... Message will be sent from client ONE");
					client_one.connect();
					break;
				case 2:
					
					System.out.println("You have pressed 1... Message will be sent from client ONE");
					client_two.connect();
					break;
					
				case 3:
					
					System.out.println("You have pressed 1... Message will be sent from client ONE");
					client_three.connect();

				default:
					System.out.println("PLEASE PRESS A VALID VALUE...");
					break;
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
