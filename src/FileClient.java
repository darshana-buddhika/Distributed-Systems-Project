

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class FileClient {
	String downlFilePath;
//	public static int TCP_PORT_OFFSET = 10;
	SHAHash shaHash = new SHAHash();

	public FileClient(String dFilePath) {
		downlFilePath = dFilePath;
	}

	public void downloadFile(String fileName, String serverIp, int serverPort) {

		try {
			
			Registry registry = LocateRegistry.getRegistry(serverIp, serverPort + Main.UDP_PORT_OFFSET);
			FileInterface stub = (FileInterface) registry.lookup("Hello");
			// byte[] fileData = stub.downloadFile("TestFile.txt");
			String fileData = stub.downloadFile(fileName);
			System.out.printf(Integer.toString(fileData.length()));
//			File file = new File(fileName);
//			try (BufferedOutputStream output = new BufferedOutputStream(
//					new FileOutputStream(downlFilePath + "/" + file.getName()))) {
//				output.write(fileData, 0, fileData.length);
//				output.flush();
//			}
			
			String originalHash = stub.getHash(fileName);
			System.out.println(originalHash);
			
			String localHash = shaHash.hashString(fileData);
			System.out.println(localHash);
			
			if (localHash.equals(originalHash)) {
				System.out.println("Hash is maching file download successfull");
			}else {
				System.out.println("Hash mismatch file download faild");
			}
			
//			String response = (stub.downloadFile(fileName));
//			System.out.println("response: " + response);
		} catch (IOException | NotBoundException e) {
			System.err.println("FileServer exception: " + e.getMessage());
		}
	}
	

}
