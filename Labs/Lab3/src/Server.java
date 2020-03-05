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

        

        Server obj = new Server();
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

        // Bind the remote object's stub in the registry
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(args[0], stub);

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

    public Server(){
        this.dnsIp = new HashMap<String, String>();
    }
    /*
    private void run() throws IOException {
        // get request
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        this.socket.receive(packet);
        // process request and reply's
        this.process(packet);
    }

    private void process(DatagramPacket packet) throws IOException {
        // process and prints request
        String received = new String(packet.getData()).trim();
        this.print(new String[]{"Server:", received});
        // parse the reply
        String reply = this.parse(received);

        if(reply == null) {
            reply = Integer.toString(-1);
        }

        byte[] buf = reply.getBytes();

        int clientPort = packet.getPort();
        InetAddress clientAddress = packet.getAddress();

        DatagramPacket newPacket = new DatagramPacket(buf, buf.length, clientAddress, clientPort);
        this.socket.send(newPacket);
    }

    private String parse(String received) {
        String[] tokens = received.trim().split(" ");
        String reply;

        if(tokens[0].equals("register")) {
            if(!this.dnsIp.containsKey(tokens[1])) {
                this.dnsIp.put(tokens[1], ipAddress);
                return this.dnsIp.size() + "\n" + tokens[1] + " " + ipAddress;
            } else {
                return -1 + "\n" + tokens[1] + " " + ipAddress;
            }
        } else if(tokens[0].equals("lookup")) {
            String name;
            if(!this.dnsIp.containsKey(tokens[1])) {
                return -1 + "\n" + tokens[1] + " " + dnsIp.get(tokens[1]);
            } else {
                return this.dnsIp.size() + "\n" + tokens[1] + " " + dnsIp.get(tokens[1]);
            }
        } else {
            System.out.println("Error parsing message");
            return null;
        }
    }*/

}
