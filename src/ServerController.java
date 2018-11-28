

import java.net.InetAddress;
import java.nio.channels.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerController {
	FileServer fs;

	public void createServer(String upPath, int portNum, InetAddress nodeIp) {
		try {
			fs = new FileServer(upPath);
			String nodeIP = nodeIp.getHostAddress();
			System.setProperty("java.rmi.server.hostname", nodeIP);
			FileInterface stub = (FileInterface) UnicastRemoteObject.exportObject(fs, 0);
			Registry registry = LocateRegistry.createRegistry(portNum + Main.UDP_PORT_OFFSET);
			registry.bind("Hello", stub);
			System.out.println("Server is up");

		} catch (AlreadyBoundException | RemoteException e) {
			System.err.println("Server exception: " + e.toString());
		} catch (java.rmi.AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
