import java.io.*;
import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/*
    java Server <remote_object_name>
        <remote_object_name> : The name to which the remote object shall be bound
*/

public class Server implements ServerInterface{
    private HashMap<String, String> dnsIp;

    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        // Receives the object name in the command line
        String objectName = args[0];
        // Programmatically set the value of the property java.rmi.server.codebase to the location of the codebase
        System.setProperty("java.rmi.server.codebase", "file:///C://Users/Martim/Desktop/MIEIC_sdis/Labs/Lab3/out/production/RMI/");

        try {
            // Instantiate the "remote object".
            Server obj = new Server();
            // "Export" the remote object and produce the respective stub
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);
            // Register the stub (returns an object that represents the rmi registry)
            Registry registry = LocateRegistry.createRegistry(2020);
            // It is preferable to use rebind(…), as bind(…) will throw an exception if the previously registered name is reused
            registry.rebind(objectName, stub);
            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public Server(){
        this.dnsIp = new HashMap<String, String>();
    }

    public String lookup(String dnsName){
        String response = "lookup " + dnsName + " : ";

        if(!this.dnsIp.containsKey(dnsName)) {
            response += "-1";
        } else {
            response += this.dnsIp.size();
        }

        System.out.println(response);
        return response;
    }

    public String register(String dnsName, String ipAddress){
        String response = "register " + dnsName + " " + ipAddress + " : ";

        if(!this.dnsIp.containsKey(dnsName)) {
            this.dnsIp.put(dnsName, ipAddress);
            response += this.dnsIp.size();
        } else {
            response += "-1";
        }
        System.out.println(response);
        return response;
    }
}
