import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
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

public class SSLClient {
    private int port;
    private SSLSocket sslSocket = null;
    private SSLSocketFactory sslSocketFactory = null;
    private InetAddress address;
    private BufferedReader input;
    private PrintWriter output;
    private String[] cipherSuits;

    public static void main(String[] args) throws IOException {
        if(!(args[2].equals("register") && args.length == 5) && !(args[2].equals("lookup") && args.length == 4)) {
            System.out.println("Usage: java Client <host> <port> <oper> <opnd>*");
        }

        SSLClient client = new SSLClient(args[0], Integer.parseInt(args[1]), args);

        // send packet
        client.send(client.build(args));

        // receive packet and print result
        client.print(client.receive(), args);

        // close socket
        client.destroy();
    }

    public SSLClient(String address, int port, String[] args) throws IOException {
        this.port = port;
        this.address = InetAddress.getLocalHost();
        this.sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        this.sslSocket = (SSLSocket) this.sslSocketFactory.createSocket(this.address, port);

        /*System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
        System.setProperty("javax.net.ssl.keyStore", "client.keys");*/

        this.sslSocket.setEnabledProtocols(this.sslSocket.getSupportedProtocols());

        for(int i = 2; i < args.length; i++){
            this.cipherSuits[i-1] = args[i];
        }
        this.sslSocket.setEnabledCipherSuites(this.cipherSuits);

        this.input = new BufferedReader(new InputStreamReader(this.sslSocket.getInputStream()));
        this.output = new PrintWriter(this.sslSocket.getOutputStream(), true);
    }

    private void send(String message) throws IOException {
        output.println(message);
    }

    private String receive() throws IOException {
        // receive response
        String response = input.readLine().trim();
        return response;
    }

    private String build(String[] args) {
        String message = args[2];

        for (int i = 3; i < args.length; i++) {
            message += " " + args[i];
        }

        return message;
    }

    private void print(String result, String[] args) {
        if(!result.equals("-1")) {
            System.out.println("Client: " + this.build(args) + " : " + result);
        } else {
            System.out.println("Client: " + this.build(args) + " : ERROR");
        }
    }

    public void destroy() throws IOException {
        this.sslSocket.shutdownOutput();
        this.sslSocket.close();
    }
}