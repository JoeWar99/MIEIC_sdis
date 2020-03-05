import java.rmi.Remote;
import java.rmi.RemoteException;

// All arguments and return values bust be of the type java.io.Serializable
public interface ServerInterface extends Remote {
    String lookup(String dnsName) throws RemoteException;
    String register(String dnsName, String ipAddress) throws RemoteException;
}


/*
* MyServerless class {
* implementa lookup e register
*
* 1) set codevase property - necessario para criari o stub
* 2) instanciar o remote object
* 3)create the stub
* 4) register the stub
*
*classe naming - bind rebind unbind lookup
*
* classe name ou com um metodo estatico a outra claasse que e o get registry
*
* rebind e preferivel
*
* */
