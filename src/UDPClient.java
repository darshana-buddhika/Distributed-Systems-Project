import java.net.*;
import java.io.*;

public class UDPClient {
	
	private int myPort;
	private int ID;
	private int serverPort = 55555;
	
	private byte[] data = new byte[65536];
	private DatagramSocket clientSocket;
	private DatagramPacket DataPacket;
	private InetAddress myAddress;
	
	public UDPClient(int port) throws UnknownHostException, SocketException {
		this.myPort = port;
		this.myAddress = InetAddress.getByName("localhost");
		this.clientSocket = new DatagramSocket();
	}
	
	public void joinNetwork() {
		//FORM THE MESSAGE
		String joinMessage = " REG "+myAddress+" "+myPort+" 1234abcd";
		joinMessage = String.format("%04d", joinMessage.length()+ 4) + joinMessage;
		System.out.println("SEND JOIN MESSAGE : " + joinMessage);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(joinMessage, myAddress);
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);
		
	}
	
	public void unregisterNetwork() {
		//FORM THE MESSAGE
		String joinMessage = " UNREG "+myAddress+" "+myPort+" 1234abcd";
		joinMessage = String.format("%04d", joinMessage.length()+ 4) + joinMessage;
		System.out.println("SEND JOIN MESSAGE : " + joinMessage);
		
		//SEND THE MESSAGE AND RECIVE THE RESPONCE FROM THE SERVER
		String ACK = sendMessage(joinMessage, myAddress);
		
		System.out.print("SERVER RESPONSE : ");
		System.out.println(ACK);

	}
	
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
