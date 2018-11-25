import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.io.*;

public class UDPClient implements Runnable {

	boolean updated = false;

	// Bootstrap server ip and the port
	private InetAddress serverIP; // = InetAddress.getByName(getMyIp());
	private final int serverPort = 55555;

	// Node details
	private int myPort;
	private String username;
	private InetAddress myAddress;
	private DatagramSocket clientSocket;

	// private List<Neighbour> neighbours = Collections.synchronizedList(new
	// ArrayList<Neighbour>()); // contain neighbour
	// private Vector<Neighbour> knownList = new Vector<>();
	private Hashtable<String, ArrayList<Neighbour>> gossipContent = new Hashtable<String, ArrayList<Neighbour>>(); // about
	// other
	// nodes
	private ArrayList<String> files = new ArrayList<>(); // set of files have on the node
	private Hashtable<String, Neighbour> knownNodes = new Hashtable<String, Neighbour>(); // contain the list of movies

	public UDPClient(int port, String username, String serverIp) throws UnknownHostException, SocketException {
		this.myPort = port;
		this.username = username;
		this.myAddress = InetAddress.getByName(getMyIp());
		this.serverIP = InetAddress.getByName(serverIp);

		fileInitializer(); // Initialize files for the nodes

	}

	// Get local mechine IP adderss
	private String getMyIp() {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress().getHostAddress();
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Register with the network for the first time
	public void registerNetwork() {

		String message = " REG " + myAddress + " " + myPort + " " + username;
		message = String.format("%04d", message.length() + 4) + message;
		// System.out.println(username + " SENDING REGISTER MESSAGE TO BOOTSTRAP SERVER
		// --> MESSAGE : " + message);

		String ACK = sendMessageWithBackofftime(message, serverIP, serverPort, false);

		if (ACK != null) {
			// System.out.println("SERVER RESPONSE TO " + username + " : " + ACK);

			String[] response = ACK.split(" ");

			if (Integer.parseInt(response[2].trim()) == 0) {
				System.out.println("WELCOME TO THE NETWORK... YOU ARE THE FIRST....");
			} else if (Integer.parseInt(response[2].trim()) == 9998) {
				System.out.println(username + " IS ALREADY REGISTERED...");
				if (knownNodes.isEmpty()) {
					unregisterNetwork();
					registerNetwork();
				}
			} else if (Integer.parseInt(response[2].trim()) == 9999) {
				System.out.println("COMMAND ERROR ...");
			} else if (Integer.parseInt(response[2].trim()) == 9997) {
				System.out.println("CANNOT REGISTER PLEASE TRY DIFFRENT PORT OR IP...");
				Scanner input = new Scanner(System.in);
				int newPort = input.nextInt();
				this.myPort = newPort;
				registerNetwork();
			} else if (Integer.parseInt(response[2].trim()) == 9996) {
				System.out.println("BOOTSTRAP IS FULL TRY AGAIN LATER...");
			} else {
				System.out.println(username + " got other nodes in the network from bootstrap server");

				// ONLY TWO OTHER CLIENTS SHOULD GET FROM bootstrap server
				for (int i = 0; i <= Integer.parseInt(response[2].trim()); i += 2) {
					if (!knownNodes.containsKey((response[3 + i]).substring(1) + response[4 + i].trim())) {
						knownNodes.put((response[3 + i]).substring(1) + response[4 + i].trim(),
								new Neighbour((response[3 + i]).substring(1), response[4 + i].trim()));
					}

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

		gossiping();

	}

	// Node unregister from the network(graceful departure)
	public void unregisterNetwork() {

		String message = " UNREG " + myAddress + " " + myPort + " " + username;
		message = String.format("%04d", message.length() + 4) + message;

		// System.out.println("SEND UNREGISTER MESSAGE FROM " + username + " : " +
		// message);

		String ACK = sendMessageWithBackofftime(message, serverIP, serverPort, false);

		leaveNetwork();

		// System.out.print("SERVER RESPONSE TO " + username + ": " + ACK);

	}

	// Node send join other nodes in the neighbor list
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN " + myAddress + " " + myPort;
		message = String.format("%04d", message.length() + 4) + message;
		// System.out.println("SEND JOIN MESSAGE FROM " + username + " : " + message);

		String ACK = sendMessageWithBackofftime(message, neghbourAddress, neghbourPort, true);

		// System.out.println(username + " GOT A ACKNOLEDGEMENT FROM : " + ACK);
	}

	public void leaveNetwork() {
		String pre = " LEAVE " + myAddress + " " + myPort;
		String message = String.format("%04d", pre.length() + 4) + pre;

		knownNodes.forEach((key, value) -> {
			sendMessageWithBackofftime(message, value.getIpAddress(), value.getPort(), false);
		});

	}

	// Send JOIN message to other nodes in a new thread for each node
	public void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				// System.out.println(username + " SENDING JOIN MESSAGE TO " + neighbours.size()
				// + " NEIGHBOURS");

				synchronized (knownNodes) {
					knownNodes.forEach((key, value) -> {
						joinNeghbour(value.getIpAddress(), value.getPort());

					});
				}

			}

		}).start();

	}

	// Listen for incoming packets
	private void listen() {

		// Bind socket to client port for incoming listing
		if (clientSocket == null) {
			try {
				clientSocket = new DatagramSocket(myPort);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		while (true) {
			// System.out.println(username + " IS LISTING FOR INCOMMING PACKATES");

			byte[] data = new byte[65536];
			DatagramPacket inData = new DatagramPacket(data, data.length);
			try {
				clientSocket.receive(inData);
				String response = new String(inData.getData());
				System.out.println(username + " recive message -> : " + response);

				String[] a = response.split(" ");

				// Handle JOIN message
				if (a[1].trim().equals("JOIN")) {

					String message = " JOIN OK " + 1;
					message = String.format("%04d", message.length() + 4) + message;
					sendMessage(message, inData.getAddress(), inData.getPort(), false);

					// Add new neighbor to the list of neighbors
					Neighbour tempNeighbour = new Neighbour(a[2].trim().substring(1), a[3].trim());
					synchronized (knownNodes) {
						if (!knownNodes
								.containsKey(tempNeighbour.getIpAddress().getHostAddress() + tempNeighbour.getPort())) {
							knownNodes.put(tempNeighbour.getIpAddress().getHostAddress() + tempNeighbour.getPort(),
									tempNeighbour);
							updated = true;
						}
					}

					// Handle the gossip information
				} else if (a[1].trim().equals("GOS")) {

					String[] nodeDetails = Arrays.copyOfRange(a, 2, a.length);

					for (int i = 0; i < nodeDetails.length; i += 2) {
						synchronized (knownNodes) {
							if (!knownNodes.containsKey(nodeDetails[i].trim() + nodeDetails[i + 1].trim())) {

								knownNodes.put(nodeDetails[i].trim() + nodeDetails[i + 1].trim(),
										new Neighbour(nodeDetails[i].trim(), nodeDetails[i + 1].trim()));
								
								updated = true;

							}
						}
					}

				}
				// Handle LEAVE message
				else if (a[1].trim().equals("LEAVE")) {

					String ip = a[2].trim();
					String port = a[3].trim();

					synchronized (knownNodes) {
						if (knownNodes.containsKey(ip + port)) {
							knownNodes.remove(ip + port);
							updated = true;
						}
					}

					sendMessage("LEAVEOK", inData.getAddress(), inData.getPort(), false);

					// Handle SEARCH message
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

					sendMessageWithBackofftime("ACKOK", inData.getAddress(), inData.getPort(), false); //
					//

					if (numberOfMatches > 0) {
						sendMessageWithBackofftime(message, InetAddress.getByName(a[2].trim().substring(1)),
								Integer.parseInt(a[3].trim()), true);
					} else {
						int hops = Integer.parseInt(a[5].trim());
						if (hops > 0) {

							synchronized (knownNodes) {
								knownNodes.forEach((key, value) -> {
									String msg = response.trim().substring(0, response.trim().length() - 1)
											+ (hops - 1);
									// System.out.println(msg);
									sendMessageWithBackofftime(msg, value.getIpAddress(), value.getPort(), true);
								});
							}
						}
					}

					// Handle SEARCH OK messages
				} else if (a[1].trim().equals("SEROK")) {

					String message = "ACKOK";
					sendMessage(message, inData.getAddress(), inData.getPort(), false); // Send the acknowledgment for
																						// SERACH OK

					if (!a[2].trim().equals("0")) {
						System.out.println("File found - > " + response);
						// System.out.println(response);

						String ip = a[3].trim().substring(1);
						String port = a[4].trim();
						String fileName = a[4].trim();

						// If the neighbour is already in the list
						if (gossipContent.containsKey(port)) {
							gossipContent.get(fileName).add(new Neighbour(ip, port));

						} else {
							ArrayList<Neighbour> list = new ArrayList<>();
							list.add(new Neighbour(ip, port));
							gossipContent.put(fileName, list);
						}
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
						System.out.println(username + " Got a reply -> " + new String(reciveDataPacket.getData()));
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

	// Send message twice with random timeout
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

	// Close socket
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
		if (knownNodes.isEmpty()) {
			System.out.println("There are no neghbour nodes yet...");
		} else {
			System.out.println("**************************** Neighbours of " + username+" *******************************");
			knownNodes.forEach((key, value) -> {
				System.out
						.println("Ipaddress " + value.getIpAddress().getHostAddress() + " and port " + value.getPort());
			});
		}
		
		System.out.println("*******************************************************************");

	}

	public void gossiping() {

		new Thread(new Runnable() {
			String temp = " GOS";

			@Override
			public void run() {

				while (true) {
					synchronized (this) {
						if (updated) {

							knownNodes.forEach((key, value) -> {

								knownNodes.forEach((key_2, v) -> {
									if (!key.equals(key_2)) {
										temp = temp + " " + v.getIpAddress().getHostAddress() + " " + v.getPort();
									}
								});
								String message = String.format("%04d", temp.length() + 4) + temp;
								sendMessageWithBackofftime(message, value.getIpAddress(), value.getPort(), false);
								temp = " GOS";
							});
							updated = false;
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						} else {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {

								e.printStackTrace();
							}
						}
					}
				}

			}
		}).start();

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

		knownNodes.forEach((key, value) -> {
			new Thread(() -> {
				String ack = sendMessageWithBackofftime(message, value.getIpAddress(), value.getPort(), false);

			}).start();
		});

	}

	// Print files
	public void getFiles() {
		System.out.println("**************** "+username+" FILE "+"******************");
		for (String file : files) {
			System.out.println(file);
		}
		
		System.out.println("********************************************************");
	}
	
	public void getFileRoute() {
		gossipContent.forEach((key,value)->{
			System.out.println("File name : "+key+" nodes : "+value.size());
		});
	}

}
