import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;

/*
    java Client <host> <port> <oper> <opnd>* <cypher suites>*
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
        if(!(args[2].equals("register") && args.length >= 5) && !(args[2].equals("lookup") && args.length >= 4)) {
            System.out.println("Usage: java Client <host> <port> <oper> <opnd>* <cypher suites>*");
        }

        SSLClient client = new SSLClient(args);
        client.send(client.build(args));
        client.print(client.receive(), args);
        client.destroy();
    }

    public SSLClient(String[] args) throws IOException {
        this.port = Integer.parseInt(args[1]);
        this.address = InetAddress.getLocalHost();

        int operationLength;
        if(operationIsRegister(args)) {
            operationLength = 5;
        } else {
            operationLength = 4;
        }

        this.cipherSuits = new String[args.length - operationLength];
        for(int i = operationLength; i < args.length; i++)
            this.cipherSuits[i-operationLength] = args[i];

        this.sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        this.sslSocket = (SSLSocket) this.sslSocketFactory.createSocket(this.address, this.port);
        this.sslSocket.setEnabledProtocols(this.sslSocket.getSupportedProtocols());
        this.sslSocket.setEnabledCipherSuites(this.cipherSuits);

        this.sslSocket.startHandshake();

        this.input = new BufferedReader(new InputStreamReader(this.sslSocket.getInputStream()));
        this.output = new PrintWriter(this.sslSocket.getOutputStream(), true);
    }

    private void send(String message) {
        output.println(message);
    }

    private String receive() throws IOException {
        return input.readLine().trim();
    }

    private String build(String[] args) {
        String message = args[2];

        for (int i = 3; i < args.length; i++)
            message += " " + args[i];

        return message;
    }

    private void print(String result, String[] args) {
        if(!result.equals("-1")) {
            System.out.println("Client: " + this.build(args) + ": " + result);
        } else {
            System.out.println("Client: " + this.build(args) + ": ERROR");
        }
    }

    private void destroy() throws IOException {
        this.sslSocket.shutdownOutput();
        this.sslSocket.close();
    }

    private boolean operationIsRegister(String[] args) {
        return args[2].equals("register");
    }
}