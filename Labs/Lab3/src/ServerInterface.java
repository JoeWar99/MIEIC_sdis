import java.rmi.Remote;
import java.rmi.RemoteException;

// All arguments and return values bust be of the type java.io.Serializable (Strings, ...)
public interface ServerInterface extends Remote {
    String lookup(String dnsName) throws RemoteException;
    String register(String dnsName, String ipAddress) throws RemoteException;
}
