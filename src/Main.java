import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		String[] usernames = { "supeepa", "ranula", "hasini", "darshana", "sandaru", "chamath", "rangika", "nadeesha",
				"prabod", "vikum" };
		ArrayList<UDPClient> nodesInTheNetwork = new ArrayList<>();

		Scanner scanner = new Scanner(System.in);
		try {
			// Creating 10 nodes for the network
			for (int i = 0; i < 10; i++) {

				nodesInTheNetwork.add(new UDPClient(15000 + i * 2, usernames[i]));

			}

			for (UDPClient node : nodesInTheNetwork) {
				node.registerNetwork();
			}

			while (true) {

				int input = scanner.nextInt();

				nodesInTheNetwork.get(input).getNeighbours();
				nodesInTheNetwork.get(input).getFiles();
				nodesInTheNetwork.get(input).searchFiles("Adventures", 2);

			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scanner.close();

	}

}
