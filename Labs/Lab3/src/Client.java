import java.io.*;
import java.net.*;

/*
    java Client <host> <port> <oper> <opnd>*
        <host> : is the DNS name (or the IP address, in the dotted decimal format) where the server is running
        <port> : port number the server is using to provide the service
        <oper> : can be either "register" or "lookup"
        <opnd>* : for "register" -> <DNS name> <IP address>
                : for "lookup"   -> <DNS name>
*/

public class Client {
    private int port;
    private DatagramSocket socket;
    private InetAddress address;

    public static void main(String[] args) throws IOException {
        if(!(args[2].equals("register") && args.length == 5) && !(args[2].equals("lookup") && args.length == 4)) {
            System.out.println("Usage: java Client <host> <port> <oper> <opnd>*");
        }

        Client client = new Client(args[0], Integer.parseInt(args[1]));
        // send packet
        client.send(client.build(args));
        // receive packet and print result
        client.print(client.receive(), args);
        // close socket
        client.destroy();
    }

    public Client(String address, int port) throws IOException {
        this.port = port;
        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(address);
    }

    private void send(String message) throws IOException {
        // create buffer
        byte[] buf = message.getBytes();
        // create packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        // send packet
        this.socket.send(packet);
    }

    private int receive() throws IOException {
        // create buffer
        byte[] buf = new byte[256];
        // create packet
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        // sets timeout
        this.socket.setSoTimeout(2000);
        // receive packet
        this.socket.receive(packet);
        // returns the result value
        return Integer.parseInt(new String(packet.getData()).split("\n")[0]);
    }

    private String build(String[] args) {
        String message = args[2];

        for (int i = 3; i < args.length; i++) {
            message += " " + args[i];
        }

        return message;
    }

    // Prints the operation request and the result
    private void print(int result, String[] args) {
        if(result != -1) {
            System.out.println("Client: " + this.build(args) + " : " + result);
        } else {
            System.out.println("Client: " + this.build(args) + " : ERROR");
        }
    }

    public void destroy() {
        this.socket.close();
    }
}