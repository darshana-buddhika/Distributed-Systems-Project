import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.io.*;

public class UDPClient implements Runnable{
	
	private int myPort;
	private String username;
	private InetAddress serverIP = InetAddress.getByName("127.0.0.1");
	private int serverPort = 55555;
	
	private byte[] data = new byte[65536];
	private DatagramSocket clientSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket recivePacket;
	private InetAddress myAddress;
	private ArrayList<String> neghbours = new ArrayList<>(); // contain neghbour nodes
	private ArrayList<String> content = new ArrayList<>();	// contain the list of movies
	
	public UDPClient(int port, String username) throws UnknownHostException, SocketException {
		this.myPort = port;
		this.username = username;
		this.myAddress = InetAddress.getByName("localhost");
		this.clientSocket = new DatagramSocket(port);
	}
	
	// REGISTER TO THE NETWORK
	public void registerNetwork() {
		// FORM THE MESSAGE
		String message = " REG " + myAddress + " " + myPort + " " + username;
		message = String.format("%04d", message.length() + 4) + message;
		System.out.println("SEND REGISTER MESSAGE FROM "+username+" : " + message);

		// SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, serverIP, serverPort);

		System.out.println("SERVER RESPONSE TO "+username+" : "+ACK);

		String[] response = ACK.split(" ");

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				listen();

			}
		}).start();

		if (Integer.parseInt(response[2].trim()) == 9998) {
			System.out.println("CLIENT IS ALREADY REGISTERED...");
			if (neghbours.isEmpty()) {
				unregisterNetwork();
				registerNetwork();
			}
		} else if (Integer.parseInt(response[2].trim()) == 9999) {
			System.out.println("COMMAND ERROR ...");
		} else if (Integer.parseInt(response[2].trim()) == 9997) {
			System.out.println("CANNOT REGISTER PLEASE TRY DIFFRENT PORT OR IP...");
			registerNetwork();
		} else if (Integer.parseInt(response[2].trim()) == 9996) {
			System.out.println("BS IS FULL TRY AGAIN LATER...");
		} else if (Integer.parseInt(response[2].trim()) == 1 || Integer.parseInt(response[2].trim()) == 2) {
			System.out.println("Network has more clients");

			// ONLY TWO OTHER CLIENTS SHOULD GET FROM BS
			System.out.println(username + " SAVING NEIGHBOURS GOT FROM THE SERVER...");
			for (int i = 0; i <= Integer.parseInt(response[2].trim()); i += 2) {
				neghbours.add(response[3 + i] + " " + response[4 + i]);
			}

			System.out.println("SEND OUT THE JOIN MESSAGE..........................");
		} else {
			System.out.println(username+" IS THE FIRST NODE IN THE NETWORK..");
		}

	}
	
    // UNREGISTER FROM NETWORK
	public void unregisterNetwork() {
		//FORM THE MESSAGE
		String message = " UNREG "+myAddress+" "+myPort+" "+ username;
		message = String.format("%04d", message.length()+ 4) + message;
		System.out.println("SEND UNREGISTER MESSAGE FROM "+ username +" : " + message);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, serverIP, serverPort);
		
		
		System.out.print("SERVER RESPONSE TO "+username+": "+ACK);

	}
	
	
	
	// JOIN NEBHOUR 
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN "+myAddress+" "+myPort;
		message = String.format("%04d",  message.length()+ 4) + message;
		System.out.println("SEND JOIN MESSAGE FROM "+ username +" : " + message);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, neghbourAddress, neghbourPort);
		
		System.out.print(username+" GOT A ACKNOLEDGEMENT FROM : "+ACK);
		System.out.println(ACK);
	}
	
	public void connect() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				System.out.println("SENDING CONNECT MESSAGE TO "+ neghbours.size()+" FROM "+username);
				for (int i = 0; i < neghbours.size(); i++) {
					String[] text = neghbours.get(i).split(" ");
					try {
						System.out.println(neghbours.get(i));
						joinNeghbour(InetAddress.getByName("localhost"), Integer.parseInt(text[1].trim()));   //TODO NEED TO CHANGE THE IP
//						Thread.sleep(500);
					} catch (NumberFormatException e) {
						//  Auto-generated catch block
						e.printStackTrace();
					} catch (UnknownHostException e) {
						//  Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		
	}
	
	private void listen() {
		while (true) {
			System.out.println(username+" IS LISTING FOR INCOMMING PACKATES");
			byte[] data_1 = new byte[65536];
			DatagramPacket d = new DatagramPacket(data_1, data_1.length);
			try {
				clientSocket.receive(d);
				String response = new String(d.getData());
				System.out.println(username+ " RECEAVE A MESSAGE : "+response);
				
				String[] a = response.split(" ");
				
				if (a[1].trim().equals("JOIN")) {
					String message = "GOT YOUR MESSAGE SENDING REPLY FROM : "+username + " TO : "+a[3].trim();

					sendMessage(message, InetAddress.getByName("localhost"), Integer.parseInt(a[3].trim()));
				}
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	// UDP SEND MESSAGE
	private String sendMessage(String message, InetAddress neibhourAddress, int neghbourPort) {
		data = message.getBytes();
		
		//SEND DATA
		sendPacket = new DatagramPacket(data,data.length,neibhourAddress,neghbourPort);
		try {
			clientSocket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("FAILD WHEN SENDING MESSAGE: ERROR -> ");
			e.printStackTrace();
			return "none";
		}
		
		//RECIVE THE DATA FROM RESPONSE
		data = new byte[65536];
		recivePacket = new DatagramPacket(data, data.length);
		try {
			clientSocket.receive(recivePacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("NETWORK FAILD WHEN RECIVEING MESSAGE: ERROR -> ");
			e.printStackTrace();
			return "none";
		}
		//RETURN THE RESPONSE
		return new String(recivePacket.getData());
	}
	
	public void closeSocket() {
		//CLOSE THE CONNECTION
		clientSocket.close();
		System.out.println("Client Socket Closed....");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println(username+" CLIENT IS ON.....");
		
		
	}
	
	

}
