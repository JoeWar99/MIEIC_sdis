import java.io.*;
import java.net.*;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/*
    java Server <port number>
        <port number> : port number the server shall use to provide the service
*/

public class Server implements ServerInterface{
    public HashMap<String, String> dnsIp;

    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port number>");
            return;
        }

        // Programmatically set the value of the property java.rmi.server.codebase to the location of the codebase
        System.setProperty("java.rmi.server.codebase", "file:///C://Users/Martim/Desktop/MIEIC_sdis/Labs/Lab3/out/production/RMI/");
        // instantiate the "remote object".
        Server obj = new Server();
        // "export" the remote object and produce the respective stub
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);
        // Register the stub (returns an object that represents the rmi registry)
        Registry registry = LocateRegistry.getRegistry(2001);
        // It is preferable to use rebind(…), as bind(…) will throw an exception if the previously registered name is reused
        registry.rebind(args[0], stub);
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
