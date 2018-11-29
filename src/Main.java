import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static int UDP_PORT_OFFSET = 10;
	public static String UPLOAD_FILE_PATH = "C:/Users/darshana/Desktop";
	public static String DOWNALOAD_FILE_PATH = "C:/Users/darshana/Desktop/download";
	public static void main(String[] args) {

		Scanner input = new Scanner(System.in);
		System.out.println("Enter username for the client node...");
		String username = input.next();

		System.out.println("Enter Udp port for the node...");
		int port = input.nextInt();

		System.out.println("Enter bootstrap server ip...");
		String serverip = input.next();

		try {
			UDPClient node = new UDPClient(port, username, serverip);

			while (true) {
				System.out.println("");
				System.out.println("Enter the following numbers to do the corrosponding actions....");
				System.out.println("1. To register network");
				System.out.println("2. View neighbour nodes");
				System.out.println("3. View set files in the node");
				System.out.println("4. Search a file in the network");
				System.out.println("5. View File route table");
				System.out.println("6. Leave network");
				System.out.println("");

				int option = input.nextInt();

				switch (option) {
				case 1:
					node.registerNetwork();
					break;
				case 2:
					node.getNeighbours();
					break;

				case 3:
					node.getFiles();
					break;

				case 4:
					System.out.println("Enter the file name you want to download...");
					String file = input.next();
//					System.out.println("Enter the number of hops you want to go...");
//					int hops = input.nextInt();
					node.searchFiles(file, 0);
					break;

				case 5:
					node.getFileRoute();
					break;
				
				case 6:
					node.leaveNetwork();
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
