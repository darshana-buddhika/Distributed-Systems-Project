

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileInterface extends Remote {
    public String downloadFile(String fileName) throws RemoteException;
    public String getHash(String fileName) throws RemoteException;

}