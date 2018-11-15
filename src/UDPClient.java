import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class UDPClient implements Runnable {

	private int myPort;
	private String username;
	private InetAddress serverIP = InetAddress.getByName("127.0.0.1");
	private final int serverPort = 55555;

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
		// this.clientSocket = new DatagramSocket();
	}

	// Register method
	public void registerNetwork() {
		// Constructing the register message
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
				System.out.println("Network has more clients");

				// ONLY TWO OTHER CLIENTS SHOULD GET FROM BS
				System.out.println(username + " SAVING NEIGHBOURS GOT FROM THE SERVER...");
				for (int i = 0; i <= Integer.parseInt(response[2].trim()); i += 2) {
					neighbours.add(new Neighbour((response[3 + i]).substring(1), response[4 + i].trim()));
				}

			}
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					listen();
				}
			}).start();

		} else {
			System.out.println(username + " FAILD TO CONNECT TO SERVER");
		}

	}

	// Unregister method
	public void unregisterNetwork() {
		// Constructing the unregister message
		String message = " UNREG " + myAddress + " " + myPort + " " + username;
		message = String.format("%04d", message.length() + 4) + message;

		System.out.println("SEND UNREGISTER MESSAGE FROM " + username + " : " + message);

		// SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, serverIP, serverPort);
		closeSocket();
		System.out.print("SERVER RESPONSE TO " + username + ": " + ACK);

	}

	// JOIN NEBHOUR
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN " + myAddress + " " + myPort;
		message = String.format("%04d", message.length() + 4) + message;
		System.out.println("SEND JOIN MESSAGE FROM " + username + " : " + message);

		// SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, neghbourAddress, neghbourPort);

		System.out.print(username + " GOT A ACKNOLEDGEMENT FROM : " + ACK);
		System.out.println(ACK);
	}

	public void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				System.out.println(username + " SENDING CONNECT MESSAGE TO " + neighbours.size() + " NEIGHBOURS FROM ");
				for (int i = 0; i < neighbours.size(); i++) {
		
					try {
						System.out.println(neighbours.get(i).getPort());
						joinNeghbour(InetAddress.getByName("127.0.0.1"), neighbours.get(i).getPort()); // TODO
																										// NEED
						// TO CHANGE
						// THE IP
						// Thread.sleep(500);
					} catch (NumberFormatException e) {
						// Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						// Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				System.out.println("neighbours of" + username);
				getNeighbours();
			}
			
		}).start();

	}

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
				System.out.println(username + " RECEAVE A MESSAGE : " + response);

				String[] a = response.split(" ");

				if (a[1].trim().equals("JOIN")) {
					String message = "GOT YOUR MESSAGE SENDING REPLY FROM : " + username + " TO : " + a[3].trim();

					sendMessage(message, d.getAddress(), d.getPort());
					
//	Add new neighbour to the list of neighbours
					neighbours.add(new Neighbour(a[2].trim().substring(1),a[3].trim()));
				}
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// UDP SEND MESSAGE
	private String sendMessage(String message, InetAddress neibhourAddress, int neghbourPort) {
		data = message.getBytes();
		boolean reply = false;
		int numberOftyies = 0;

		try {
			DatagramSocket s = new DatagramSocket();

			sendPacket = new DatagramPacket(data, data.length, neibhourAddress, neghbourPort);

			s.send(sendPacket);

			// RECIVE THE DATA FROM RESPONSE
			data = new byte[65536];
			recivePacket = new DatagramPacket(data, data.length);

			s.setSoTimeout(3000);

			while (!reply) {
				try {
					s.receive(recivePacket);
					// return new String(recivePacket.getData());

					if (!recivePacket.getData().equals(null)) {
						System.out.println("Recive reply");
						return new String(recivePacket.getData());
					}
					System.out.println(new String(recivePacket.getData()).length());

				} catch (SocketTimeoutException e) {

					System.out.println("Timeout reached sending message again");
					if (numberOftyies < 1) {
						s.send(sendPacket);
						numberOftyies++;

					} else {
						s.close();
						break;
					}

				}
			}

			return null;
			// RETURN THE RESPONSE

		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("FAILD WHEN SENDING MESSAGE: ERROR -> ");
			e.printStackTrace();
		}

		return null;
		// SEND DATA

	}

	public void closeSocket() {
		// CLOSE THE CONNECTION
		clientSocket.close();
		System.out.println("Client Socket Closed....");
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

		System.out.println(username + " CLIENT IS ON.....");

	}

	public void getNeighbours() {
		for (Neighbour n : neighbours) {
			
			System.out.println(username + "Neighbour address " + n.getIpAddress() + " and port " + n.getPort());
		}
	}

}
