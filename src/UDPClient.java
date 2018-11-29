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

import javax.swing.event.TreeWillExpandListener;

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

	private Hashtable<String, ArrayList<Neighbour>> gossipContent = new Hashtable<String, ArrayList<Neighbour>>(); // about
	// other
	// nodes
	private ArrayList<String> files = new ArrayList<>(); // Set of files have on the node
	private Hashtable<String, Neighbour> knownNodes = new Hashtable<String, Neighbour>(); // List of known nodes in the
																							// network

	// Create the RMI file server and Client
	ServerController server = new ServerController();
	FileClient client = new FileClient(Main.DOWNALOAD_FILE_PATH);

	public UDPClient(int port, String username, String serverIp) throws UnknownHostException, SocketException {
		this.myPort = port;
		this.username = username;
		this.myAddress = InetAddress.getByName(getMyIp());
		this.serverIP = InetAddress.getByName(serverIp);

		fileInitializer(); // Initialize random 3-5 files for the node

		// Start RMI File server
		server.createServer(Main.UPLOAD_FILE_PATH, myPort, myAddress);
	}

	// Get the local network interface IP
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

			String[] response = ACK.split(" ");

			if (Integer.parseInt(response[2].trim()) == 0) {
				System.out.println("WELCOME TO THE NETWORK... YOU ARE THE FIRST....");
			} else if (Integer.parseInt(response[2].trim()) == 9998) {
				System.out.println(username + " IS ALREADY REGISTERED...");
				if (knownNodes.isEmpty()) {
					unregisterNetwork(myAddress.getHostAddress(), myPort);
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

			connect(); // Send JOIN messages to the nodes got from BS server

			gossiping(); // Start gossiping about my known nodes

//			liveCheck(); // Check all the known nodes are live

		} else {
			System.out.println(username + " FAILD TO CONNECT TO SERVER");
		}

	}

	// Node unregister from the network(graceful departure)
	public void unregisterNetwork(String address, int port) {

		String message = " UNREG " + address + " " + port + " " + username;
		message = String.format("%04d", message.length() + 4) + message;

		sendMessageWithBackofftime(message, serverIP, serverPort, false);

	}

	// Node send join other nodes in the neighbor list
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN " + myAddress + " " + myPort;
		message = String.format("%04d", message.length() + 4) + message;

		sendMessageWithBackofftime(message, neghbourAddress, neghbourPort, true);

	}

	// Graceful departure
	public void leaveNetwork() {
		String pre = " LEAVE " + myAddress + " " + myPort;
		String message = String.format("%04d", pre.length() + 4) + pre;

		synchronized (knownNodes) {
			knownNodes.forEach((key, value) -> {
				sendMessageWithBackofftime(message, value.getIpAddress(), value.getPort(), false);
			});
		}

		unregisterNetwork(myAddress.getHostAddress(), myPort);

	}

	// Send live message
	public void liveCheck() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				ArrayList<String> keysToRemove = new ArrayList<>();
				// TODO Auto-generated method stub)
				while (true) {
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Send message to the each known Node
					knownNodes.forEach((key, value) -> {
						String message = "ISLIVE 0";

						String ack = sendMessageWithBackofftime(message, value.getIpAddress(), value.getPort(), true);
						if (ack == null) {
							// Remember nodes that does not reply
							keysToRemove.add(key);

						}
					});

					// Remove the nodes that does not reply from the known nodes
					for (String key : keysToRemove) {
						Neighbour temp = knownNodes.get(key);
						unregisterNetwork(temp.getIpAddress().getHostAddress(), temp.getPort()); // Unregister them from
																									// the network
						knownNodes.remove(key);
					}

					keysToRemove.clear();

				}

			}

		}).start();
	}

	// Send JOIN message to other nodes in a new thread for each node
	public void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {

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

			byte[] data = new byte[65536];
			DatagramPacket inData = new DatagramPacket(data, data.length);
			try {
				clientSocket.receive(inData);
				String response = new String(inData.getData());
//				System.out.println(username + " recive message -> : " + response);

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

				}
				// Handle the gossip information
				else if (a[1].trim().equals("GOS")) {

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

					String ip = a[2].trim().substring(1);
					String port = a[3].trim();

					synchronized (knownNodes) {
						if (knownNodes.containsKey(ip + port)) {
							System.out.println(ip + port);
							knownNodes.remove(ip + port);
							// updated = true;
						}
					}

					sendMessage("LEAVEOK", inData.getAddress(), inData.getPort(), false);

				}
				// Handle SEARCH message
				else if (a[1].trim().equals("SER")) {
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
									sendMessageWithBackofftime(msg, value.getIpAddress(), value.getPort(), true);
								});
							}
						}
					}

				}
				// Handle SEARCH OK messages
				else if (a[1].trim().equals("SEROK")) {

					String message = "ACKOK";
					sendMessage(message, inData.getAddress(), inData.getPort(), false); // Send the acknowledgment for
																						// SERACH OK

					if (!a[2].trim().equals("0")) {
						System.out.println("File found - > " + response);

						String ip = a[3].trim().substring(1); // File node IP
						int port = Integer.parseInt(a[4].trim());// File node port
						String[] fileNameArray = Arrays.copyOfRange(a, 5, a.length); // File name

						// Concatenate file name if it has more than one name
						StringBuilder temp = new StringBuilder();
						for (String name : fileNameArray) {

							temp.append(name + " ");
						}

						String fileName = temp.toString().trim();

						boolean download = client.downloadFile(fileName, ip, (int) port); // Send download command

						// If the neighbour is already in the list
						if (download) {
							if (gossipContent.containsKey(ip + port)) {
								gossipContent.get(fileName).add(new Neighbour(ip, port + ""));

							} else {
								ArrayList<Neighbour> list = new ArrayList<>();
								list.add(new Neighbour(ip, port + ""));
								gossipContent.put(fileName, list);
							}
						}
						
					}

				}

				else if (a[0].trim().equals("ISLIVE")) {
					// Send reply to live message
					sendMessageWithBackofftime("LIVEOK", inData.getAddress(), inData.getPort(), false);

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
			int randomTimeout = Math.toIntExact((mills % 1000) + 2000);

			socket.setSoTimeout(randomTimeout);

			while (true) {
				try {
					socket.receive(reciveDataPacket);

					if (!reciveDataPacket.getData().equals(null)) {

						if (ack) {
							sendMessage("ACKOK", reciveDataPacket.getAddress(), reciveDataPacket.getPort(), false);
						}
						return new String(reciveDataPacket.getData());
					} else {
						break;
					}

				} catch (SocketTimeoutException e) {

					// System.out.println(username + " Timeout reached for message -> " + message);
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
			System.out.println("Message sending faild in th first attempt -> " + message);
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

		synchronized (knownNodes) {
			if (knownNodes.isEmpty()) {
				System.out.println("There are no neghbour nodes yet...");
			} else {
				System.out.println(
						"**************************** Routing Table of " + username + " *******************************");
				knownNodes.forEach((key, value) -> {
					System.out.println(" Ipaddress " + value.getIpAddress().getHostAddress()
							+ " and port " + value.getPort());
				});
			}
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

						}
					}

					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {

						e.printStackTrace();
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
		synchronized (knownNodes) {
			knownNodes.forEach((key, value) -> {
				new Thread(() -> {
					
					sendMessageWithBackofftime(message, value.getIpAddress(), value.getPort(), true);

				}).start();
			});
		}

	}

	// Print files
	public void getFiles() {
		System.out.println("**************** " + username + " FILES " + "******************");
		for (String file : files) {
			System.out.println(file);
		}

		System.out.println("****************************************************************");
	}

	public void getFileRoute() {
		System.out.println("**************** " + username + " FILE ROUTING TABLE " + "******************");
		gossipContent.forEach((key, value) -> {
			System.out.println("File name : " + key + " nodes : " + value.size());
		});
		
		System.out.println("*****************************************************************************");
	}

}
