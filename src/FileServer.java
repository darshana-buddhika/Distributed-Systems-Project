import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
 
 
public class FileServer  extends UnicastRemoteObject implements FileServerInt {
	
	private String file="";
	protected FileServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
 
	public void setFile(String f){
		file=f;
	}
	
	public boolean login(FileClientInt c, String path) throws RemoteException{
		/*
		 *
		 * Sending The File...
		 * 
		 */
		 try{
			 File f1=new File(path);			 
			 FileInputStream in=new FileInputStream(f1);			 				 
			 byte [] mydata=new byte[1024*1024];						
			 int mylen=in.read(mydata);
			 while(mylen>0){
				 c.sendData(f1.getName(), mydata, mylen);	 
				 mylen=in.read(mydata);				 
			 }
		 }catch(Exception e){
			 e.printStackTrace();
			 
		 }
		
		return true;
	}	
}