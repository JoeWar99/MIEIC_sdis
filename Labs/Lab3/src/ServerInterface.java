import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    String lookup(String dnsName);
    String register(String dnsName, String ipAddress);

}
