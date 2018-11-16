import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.*;

public class UDPClient implements Runnable {

	private int myPort;
	private String username;
	private InetAddress serverIP = InetAddress.getByName("127.0.0.1");
	private final int serverPort = 55555;
	private boolean finished = false;

	private byte[] data = new byte[65536];
	private DatagramSocket clientSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket recivePacket;
	private InetAddress myAddress;
	private ArrayList<Neighbour> neighbours = new ArrayList<>(); // contain neighbour nodes
	private ArrayList<String> content = new ArrayList<>(); // contain the list of movies

	public UDPClient(int port, String username) throws UnknownHostException, SocketException {
		this.myPort = port;
		this.username = username;
		this.myAddress = InetAddress.getByName("127.0.0.1");

	}

	// Register with the network for the first time
	public void registerNetwork() {

		String message = " REG " + myAddress + " " + myPort + " " + username;
		message = String.format("%04d", message.length() + 4) + message;
		System.out.println(username + " SENDING REGISTER MESSAGE TO BOOTSTRAP SERVER --> MESSAGE : " + message);

		// Send message
		String ACK = sendMessage(message, serverIP, serverPort);

		if (ACK != null) {
			System.out.println("SERVER RESPONSE TO " + username + " : " + ACK);

			String[] response = ACK.split(" ");

			if (Integer.parseInt(response[2].trim()) == 0) {
				System.out.println("WELCOME TO THE NETWORK... YOU ARE THE FIRST....");
			} else if (Integer.parseInt(response[2].trim()) == 9998) {
				System.out.println(username + " IS ALREADY REGISTERED...");
				if (neighbours.isEmpty()) {
					unregisterNetwork();
					registerNetwork();
				}
			} else if (Integer.parseInt(response[2].trim()) == 9999) {
				System.out.println("COMMAND ERROR ...");
			} else if (Integer.parseInt(response[2].trim()) == 9997) {
				System.out.println("CANNOT REGISTER PLEASE TRY DIFFRENT PORT OR IP...");
				registerNetwork();
			} else if (Integer.parseInt(response[2].trim()) == 9996) {
				System.out.println("BOOTSTRAP IS FULL TRY AGAIN LATER...");
			} else { // if (Integer.parseInt(response[2].trim()) == 1 ||
						// Integer.parseInt(response[2].trim()) == 2)
				System.out.println("Network has more nodes");

				// ONLY TWO OTHER CLIENTS SHOULD GET FROM bootstrap server
				System.out.println(username + " SAVING other nodes GOT FROM THE SERVER...");
				for (int i = 0; i <= Integer.parseInt(response[2].trim()); i += 2) {
					neighbours.add(new Neighbour((response[3 + i]).substring(1), response[4 + i].trim()));
				}

			}

			// Start listening on a new thread for incoming packets
			new Thread(new Runnable() {

				@Override
				public void run() {

					listen();
				}
			}).start();

		} else {
			System.out.println(username + " FAILD TO CONNECT TO SERVER");
		}

		connect();

	}

	// Node unregister from the network(graceful departure)
	public void unregisterNetwork() {

		String message = " UNREG " + myAddress + " " + myPort + " " + username;
		message = String.format("%04d", message.length() + 4) + message;

		System.out.println("SEND UNREGISTER MESSAGE FROM " + username + " : " + message);

		String ACK = sendMessage(message, serverIP, serverPort);

		System.out.print("SERVER RESPONSE TO " + username + ": " + ACK);

	}

	// Node send join other nodes in the neighbor list
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN " + myAddress + " " + myPort;
		message = String.format("%04d", message.length() + 4) + message;
		System.out.println("SEND JOIN MESSAGE FROM " + username + " : " + message);

		// SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, neghbourAddress, neghbourPort);

		System.out.print(username + " GOT A ACKNOLEDGEMENT FROM : " + ACK);
	}

	// Send JOIN message to other nodes in a new thread for each node
	public void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				System.out.println(username + " SENDING JOIN MESSAGE TO " + neighbours.size() + " NEIGHBOURS");
				int numberOfNodes = neighbours.size();
				finished = false;
				for (int i = 0; i < numberOfNodes; i++) {
					try {

						joinNeghbour(neighbours.get(i).getIpAddress(), neighbours.get(i).getPort());

					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}

				System.out.println("Neighbours of " + username);
				finished = true;

			}

		}).start();

	}

	// Listen for incoming packets
	private void listen() {

		ArrayList<Neighbour> tempList = new ArrayList<>();
		if (clientSocket == null) {
			try {
				clientSocket = new DatagramSocket(myPort);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while (true) {
			System.out.println(username + " IS LISTING FOR INCOMMING PACKATES");

			byte[] data_1 = new byte[65536];
			DatagramPacket d = new DatagramPacket(data_1, data_1.length);
			try {
				clientSocket.receive(d);
				String response = new String(d.getData());
				System.out.println(username + " RECEAVE A DATA : " + response);

				String[] a = response.split(" ");

				if (a[1].trim().equals("JOIN")) {
					String message = "JOIN OK";

					sendMessage(message, d.getAddress(), d.getPort());

					// Add new neighbor to the list of neighbors
					Neighbour tempNeighbour = new Neighbour(a[2].trim().substring(1), a[3].trim());

					// Eliminate adding redundant nodes
					if (!tempList.contains(tempNeighbour)) {
						tempList.add(tempNeighbour);
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

//	private void mearge(ArrayList<Neighbour> original, ArrayList<Neighbour> temp) {
//		for (Neighbour tempNode : temp) {
//			for (Neighbour node : original) {
//				if (!tempNode.getIpAddress().getHostAddress().equals(node.getIpAddress().getHostAddress())
//						|| (tempNode.getPort() != node.getPort())) {
//					neighbours.add(tempNode);
//				}
//			}
//		}
//	}

	// UDP message sending protocol
	private String sendMessage(String message, InetAddress neibhourAddress, int neghbourPort) {
		data = message.getBytes();
		int numberOftries = 0;

		try {
			DatagramSocket s = new DatagramSocket();

			sendPacket = new DatagramPacket(data, data.length, neibhourAddress, neghbourPort);

			s.send(sendPacket);

			// RECIVE THE DATA FROM RESPONSE
			data = new byte[65536];
			recivePacket = new DatagramPacket(data, data.length);

			s.setSoTimeout(1000);

			while (true) {
				try {
					s.receive(recivePacket);
					// return new String(recivePacket.getData());

					if (!recivePacket.getData().equals(null)) {
						System.out.println("Recive reply");
						return new String(recivePacket.getData());
					}
					System.out.println(new String(recivePacket.getData()).length());

				} catch (SocketTimeoutException e) {

					System.out.println(username + " Timeout reached sending message again");
					if (numberOftries < 1) {
						s.send(sendPacket);
						numberOftries++;

					} else {
						System.out.println(username + " Message sending failed..");
						s.close();
						break;
					}
				}
			}

		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			System.out.println("FAILD WHEN SENDING MESSAGE: ERROR -> ");
			e.printStackTrace();
		}

		return null;

	}

	public void closeSocket() {
		clientSocket.close();
		System.out.println("Client Socket Closed....");
	}

	@Override
	public void run() {
		System.out.println(username + " CLIENT IS ON.....");

	}

	public void getNeighbours() {
		for (Neighbour n : neighbours) {
			System.out.println(username + " Neighbour address " + n.getIpAddress() + " and port " + n.getPort());
		}
	}

}
