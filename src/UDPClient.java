import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.io.*;

public class UDPClient implements Runnable {

	private int myPort;
	private String username;
	private InetAddress serverIP = InetAddress.getByName("127.0.0.1");
	private final int serverPort = 55555;

	private byte[] data = new byte[65536];
	private DatagramSocket clientSocket;
	// private DatagramPacket sendPacket;
	// private DatagramPacket recivePacket;
	private InetAddress myAddress;

	private List<Neighbour> neighbours = Collections.synchronizedList(new ArrayList<Neighbour>()); // contain neighbour
	private Vector<Neighbour> knownList = new Vector<>();
	private Hashtable<String, ArrayList<Neighbour>> gossipContent = new Hashtable<String, ArrayList<Neighbour>>(); // about
	// other
	// nodes
	private ArrayList<String> files = new ArrayList<>(); // set of files have on the node
	private Map<Integer, Neighbour> tempList = new HashMap<Integer, Neighbour>(); // contain the list of movies

	public UDPClient(int port, String username) throws UnknownHostException, SocketException {
		this.myPort = port;
		this.username = username;
		this.myAddress = InetAddress.getByName("127.0.0.1"); // InetAddress.getLocalHost().getHostAddress().substring(1)

		fileInitializer(); // Initialize files for the nodes

	}

	// Register with the network for the first time
	public void registerNetwork() {

		String message = " REG " + myAddress + " " + myPort + " " + username;
		message = String.format("%04d", message.length() + 4) + message;
		System.out.println(username + " SENDING REGISTER MESSAGE TO BOOTSTRAP SERVER --> MESSAGE : " + message);

		// Send message
		String ACK = sendMessageWithBackofftime(message, serverIP, serverPort, false);

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
			} else {
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

		String ACK = sendMessageWithBackofftime(message, serverIP, serverPort, false);

		System.out.print("SERVER RESPONSE TO " + username + ": " + ACK);

	}

	// Node send join other nodes in the neighbor list
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN " + myAddress + " " + myPort;
		message = String.format("%04d", message.length() + 4) + message;
		System.out.println("SEND JOIN MESSAGE FROM " + username + " : " + message);

		// SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessageWithBackofftime(message, neghbourAddress, neghbourPort, true);

		System.out.println(username + " GOT A ACKNOLEDGEMENT FROM : " + ACK);
	}

	// Send JOIN message to other nodes in a new thread for each node
	public void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				System.out.println(username + " SENDING JOIN MESSAGE TO " + neighbours.size() + " NEIGHBOURS");
				int numberOfNodes = neighbours.size();

				for (int i = 0; i < numberOfNodes; i++) {
					try {

						joinNeghbour(neighbours.get(i).getIpAddress(), neighbours.get(i).getPort());

					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}

				System.out.println("Neighbours of " + username);

			}

		}).start();

	}

	// Listen for incoming packets
	private void listen() {

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
				System.out.println(username + " RECEAVE DATA : " + response);

				String[] a = response.split(" ");

				// Handle JOIN command
				if (a[1].trim().equals("JOIN")) {
					// String message = "JOIN OK";

					// sendMessage(message, d.getAddress(), d.getPort());

					// Add new neighbor to the list of neighbors
					Neighbour tempNeighbour = new Neighbour(a[2].trim().substring(1), a[3].trim());

					if (tempList.containsKey(tempNeighbour.getPort())) {
						continue;
					} else {
						tempList.put(tempNeighbour.getPort(), tempNeighbour);
					}

					// Handle SEARCH operation
				} else if (a[1].trim().equals("SER")) {
					String fileName = a[4].trim();

					int numberOfMatches = 0;
					String message = ""; // length SEROK no_files IP port hops filename1 filename2 ... ...

					for (String file : files) {
						if (file.toLowerCase().contains(fileName.toLowerCase())) {
							message = " " + file;
							numberOfMatches++;
						}
					}

					message = " SEROK " + numberOfMatches + " " + myAddress + " " + myPort + message;
					message = String.format("%04d", message.length() + 4) + message;

					sendMessageWithBackofftime("ACKOK", d.getAddress(), d.getPort(), false); //
					//

					if (numberOfMatches > 0) {
						sendMessageWithBackofftime(message, InetAddress.getByName(a[2].trim().substring(1)),
								Integer.parseInt(a[3].trim()), true);
					} else {

					}

				} else if (a[1].trim().equals("SEROK")) {

					if (!a[2].trim().equals("0")) {
						System.out.println("Node found with the file");
						System.out.println(response);
					}
					

				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	// UDP message sending protocol
	private String sendMessage(String message, InetAddress neibhourAddress, int neghbourPort, boolean ack) {
		byte[] data = new byte[65536];

		DatagramPacket sendDataPacket;
		DatagramPacket reciveDataPacket;

		try {
			DatagramSocket socket = new DatagramSocket();

			reciveDataPacket = new DatagramPacket(data, data.length);

			data = message.getBytes();
			sendDataPacket = new DatagramPacket(data, data.length, neibhourAddress, neghbourPort);

			socket.send(sendDataPacket);

			// Generate a random number between 1000 and 3000 to set the socket timeout
			Calendar calendar = Calendar.getInstance();
			long mills = calendar.getTimeInMillis();
			int randomTimeout = Math.toIntExact((mills % 2000) + 1000);

			socket.setSoTimeout(randomTimeout);

			while (true) {
				try {
					socket.receive(reciveDataPacket);

					if (!reciveDataPacket.getData().equals(null)) {
						System.out.println(username + " Got a reply for the message -> " + message);
						if (ack) {
							sendMessage("ACKOK", reciveDataPacket.getAddress(), reciveDataPacket.getPort(), false);
						}
						return new String(reciveDataPacket.getData());
					} else {
						break;
					}

				} catch (SocketTimeoutException e) {

					System.out.println(username + " Timeout reached for message -> " + message);
					socket.close();
					break;

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

	private String sendMessageWithBackofftime(String message, InetAddress neibhourAddress, int neghbourPort,
			boolean ack) {

		String reply = sendMessage(message, neibhourAddress, neghbourPort, ack);
		if (reply == null) {
			System.out.println("Faild to connect first time attempting for the second time message -> " + message);
			String secondReply = sendMessage(message, neibhourAddress, neghbourPort, ack);

			return secondReply;
		}
		return reply;

	}

	public void closeSocket() {
		clientSocket.close();
		System.out.println("Client Socket Closed....");
	}

	@Override
	public void run() {
		System.out.println(username + " CLIENT IS ON.....");

	}

	// Print set of nodes known to given node & merge two list to one
	public void getNeighbours() {
		for (Neighbour n : neighbours) {
			System.out.println(username + " Neighbour address " + n.getIpAddress() + " and port " + n.getPort());
			knownList.add(n);
		}

		for (Neighbour n : tempList.values()) {
			System.out.println(username + " Neighbour address " + n.getIpAddress() + " and port " + n.getPort());
			knownList.add(n);
		}
	}

	public void gossiping() {

	}

	// Randomly initialize five files for each node from the list of 20
	private void fileInitializer() {
		String[] allFiles = { "Adventures of Tintin", "Jack and Jill", "Glee", "The Vampire Diarie", "King Arthur",
				"Windows XP", "Harry Potter", "Kung Fu Panda", "Lady Gaga", "Twilight", "Windows 8",
				"Mission Impossible", "Turn Up The Music", "Super Mario", "American Pickers", "Microsoft Office 2010",
				"Happy Feet", "Modern Family", "American Idol", "Hacking for Dummies" };

		Random rand = new Random();

		for (int i = 0; i < 5; i++) {
			String name = allFiles[rand.nextInt(20)];
			if (!files.contains(name)) {
				files.add(name);
			}

		}

	}

	// Search for a file within the network
	public void searchFiles(String fileName, int hops) {
		String pre = " SER " + myAddress + " " + myPort + " " + fileName + " " + hops;
		String message = String.format("%04d", pre.length() + 4) + pre; // length SER IP port file_name hops
		synchronized (knownList) {
			for (Neighbour node : knownList) {

				new Thread(() -> {
					String ack = sendMessage(message, node.getIpAddress(), node.getPort(), false);

				}).start();

			}

		}

	}

	// Print files
	public void getFiles() {
		for (String file : files) {
			System.out.println(file);
		}
	}

}
