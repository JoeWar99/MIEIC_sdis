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
    public DatagramSocket socket;
    public int port;

    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port number>");
            return;
        }

        System.setProperty("java.rmi.server.codebase", "file:///C://Users/Martim/Desktop/MIEIC_sdis/Labs/Lab3/out/production/RMI/");

        Server obj = new Server();
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

        // Bind the remote object's stub in the registry
        Registry registry = LocateRegistry.getRegistry();
        // rebind is to avoid bind exceptions (in case of already use)
        registry.rebind(args[0], stub);
    }

    public Server(){
        this.dnsIp = new HashMap<String, String>();
    }

    public String lookup(String dnsName){

        String response = "lookup " + dnsName;

        if(!this.dnsIp.containsKey(dnsName)) {
            response = response + ": -1";
            System.out.println(response);
            return response;
        } else {
            response = response + ": " + this.dnsIp.size();
            System.out.println(response);
            return response;
        }
    }

    public String register(String dnsName, String ipAddress){
        String response;

        response = "register " + dnsName + " " + ipAddress + " : ";

        if(!this.dnsIp.containsKey(dnsName)) {
            this.dnsIp.put(dnsName, ipAddress);
            response = "";
            return this.dnsIp.size() + "\n" + dnsName + " " + ipAddress;
        } else {
            return -1 + "\n" + dnsName + " " + ipAddress;
        }
    }
}
