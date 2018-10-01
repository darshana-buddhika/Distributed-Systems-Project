import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class UDPClient{
	
	private int myPort;
	private String username;
	private InetAddress serverIP = InetAddress.getByName("127.0.0.1");
	private int serverPort = 55555;
	
	private byte[] data = new byte[65536];
	private DatagramSocket clientSocket;
	private DatagramPacket DataPacket;
	private InetAddress myAddress;
	private ArrayList<String> neghbours = new ArrayList<>();
	
	public UDPClient(int port, String username) throws UnknownHostException, SocketException {
		this.myPort = port;
		this.username = username;
		this.myAddress = InetAddress.getByName("localhost");
		this.clientSocket = new DatagramSocket();
	}
	
	// REGISTER TO THE NETWORK
	public void registerNetwork() {
		//FORM THE MESSAGE
		String message = " REG "+myAddress+" "+myPort+" "+username;
		message = String.format("%04d", message.length()+ 4) + message;
		System.out.println("SEND JOIN MESSAGE : " + message);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, serverIP , serverPort);
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);
		
		String[] response = ACK.split(" ");
		
		if(Integer.parseInt(response[2].trim()) == 9998) {
			System.out.println("CLIENT IS ALREADY REGISTERED...");
		}else if (Integer.parseInt(response[2].trim()) == 9999) {
			System.out.println("COMMAND ERROR ...");
		}else if(Integer.parseInt(response[2].trim()) == 9997) {
			System.out.println("CANNOT REGISTER PLEASE TRY DIFFRENT PORT OR IP...");
		}else if (Integer.parseInt(response[2].trim()) == 9996) {
			System.out.println("BS IS FULL TRY AGAIN LATER...");
		}else if (Integer.parseInt(response[2].trim()) == 1 || Integer.parseInt(response[2].trim()) == 2 ) {
			System.out.println("Network has more clients");
			
			//ONLY TWO OTHER CLIENTS SHOULD GET FROM BS
			for (int i = 0; i <= Integer.parseInt(response[2].trim()); i += 2) {
				neghbours.add(response[3+i]+ " " +response[4+i]);
			}
			
			connect();
		}
		
	}
	
    // UNREGISTER FROM NETWORK
	public void unregisterNetwork() {
		//FORM THE MESSAGE
		String message = " UNREG "+myAddress+" "+myPort+" "+ username;
		message = String.format("%04d", message.length()+ 4) + message;
		System.out.println("SEND JOIN MESSAGE : " + message);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, serverIP, serverPort);
		
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);

	}
	
	
	
	// JOIN NEBHOUR 
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN "+myAddress+" "+myPort;
		message = String.format("%04d",  message.length()+ 4) + message;
		System.out.println("SEND JOIN MESSAGE : " + message);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, neghbourAddress, neghbourPort);
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);
	}
	
	private void connect() {
		for (int i = 0; i < neghbours.size(); i++) {
			String[] text = neghbours.get(i).split(" ");
			try {
				joinNeghbour(InetAddress.getByName("localhost"), Integer.parseInt(text[1].trim()));   //TODO NEED TO CHANGE THE IP
				listen();
			} catch (NumberFormatException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void listen() {
		while (true) {
			System.out.println(username+" is listening for incoming");
			data = new byte[65536];
			DataPacket = new DatagramPacket(data, data.length);
			try {
				clientSocket.receive(DataPacket);
				String response = DataPacket.getData().toString();
				System.out.println(response);
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
		DataPacket = new DatagramPacket(data,data.length,neibhourAddress,neghbourPort);
		try {
			clientSocket.send(DataPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("JOIN TO NETWORK FAILD WHEN SENDING MESSAGE: ERROR -> ");
			e.printStackTrace();
		}
		
		//RECIVE THE DATA FROM RESPONSE
		data = new byte[65536];
		DataPacket = new DatagramPacket(data, data.length);
		try {
			clientSocket.receive(DataPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("JOIN TO NETWORK FAILD WHEN RECIVEING MESSAGE: ERROR -> ");
			e.printStackTrace();
		}
		//RETURN THE RESPONSE
		return new String(DataPacket.getData());
	}
	
	public void closeSocket() {
		//CLOSE THE CONNECTION
		clientSocket.close();
		System.out.println("Client Socket Closed....");
	}
	
	

}
