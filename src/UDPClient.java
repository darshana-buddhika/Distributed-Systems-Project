import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class UDPClient {
	
	private int myPort;
	private String username;
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
		String ACK = sendMessage(message, myAddress).toString();
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);
		
		String response = ACK.split(" ");
		
		if ()
		
	}
	
    // UNREGISTER FROM NETWORK
	public void unregisterNetwork() {
		//FORM THE MESSAGE
		String message = " UNREG "+myAddress+" "+myPort+" "+ username;
		message = String.format("%04d", message.length()+ 4) + message;
		System.out.println("SEND JOIN MESSAGE : " + message);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, myAddress);
		
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);

	}
	
	// JOIN NEBHOUR 
	public void joinNeghbour(InetAddress neghbourAddress, int neghbourPort) {
		String message = " JOIN "+neghbourAddress+" "+neghbourPort;
		message = String.format("%04d",  message.length()+ 4) + message;
		System.out.println("SEND JOIN MESSAGE : " + message);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(message, myAddress);
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);
	}
	
	private void listen() {
		while (true) {
			data = new byte[65536];
			DataPacket = new DatagramPacket(data, data.length);
			try {
				clientSocket.receive(DataPacket);
				String response = DataPacket.getData().toString();
				System.out.println(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	// UDP SEND MESSAGE
	private String sendMessage(String message, InetAddress neibhourAddress) {
		data = message.getBytes();
		
		//SEND DATA
		DataPacket = new DatagramPacket(data,data.length,myAddress,serverPort);
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
