import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

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
				System.out.println("Enter the following numbers to do the corrosponding actions....");
				System.out.println("1. To register network");
				System.out.println("2. View neighbour nodes");
				System.out.println("3. View set files in the node");
				System.out.println("4. Search a file in the network");

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
					System.out.println("Enter file name you wanna download...");
					String file = input.next();
					System.out.println("Enter number of hops u wanna go...");
					int hops = input.nextInt();
					node.searchFiles(file, hops);
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

		// String[] usernames = { "supeepa", "ranula", "hasini", "darshana", "sandaru",
		// "chamath", "rangika", "nadeesha",
		// "prabod", "vikum" };
		// ArrayList<UDPClient> nodesInTheNetwork = new ArrayList<>();
		//
		// Scanner scanner = new Scanner(System.in);
		// try {
		// // Creating 10 nodes for the network
		// for (int i = 0; i < 2; i++) {
		//
		// nodesInTheNetwork.add(new UDPClient(15010 + i * 2, usernames[i]));
		//
		// }
		//
		// for (UDPClient node : nodesInTheNetwork) {
		// node.registerNetwork();
		// }
		//
		// while (true) {
		//
		// int input = scanner.nextInt();
		//
		// nodesInTheNetwork.get(input).getNeighbours();
		// nodesInTheNetwork.get(input).getFiles();
		// nodesInTheNetwork.get(input).searchFiles("windows", 2);
		//
		// }
		// } catch (UnknownHostException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SocketException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// scanner.close();

	}

}
