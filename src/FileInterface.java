

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileInterface extends Remote {
    public byte[] downloadFile(String fileName) throws RemoteException;
//    public int addTwo (int a, int b) throws RemoteException;

}