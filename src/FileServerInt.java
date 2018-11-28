import java.rmi.*;
public interface FileServerInt extends Remote{
	 
	public boolean login(FileClientInt c, String path) throws RemoteException;
}
