import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.*;
import java.util.HashMap;

public class SSLServer {
    public HashMap<String, String> dnsIp;
    public int port;
    private SSLServerSocket socket = null;
    private SSLServerSocketFactory socketFactory = null;
    private String[] cipherSuits;

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java Server <port number> <cipher suites>*");
            return;
        }

        SSLServer server = new SSLServer(args);

        while(true) server.run();
    }

    public SSLServer(String[] args) throws IOException {
        this.port = Integer.parseInt(args[0]);
        this.dnsIp = new HashMap<>();

        this.cipherSuits = new String[args.length - 1];
        for(int i = 1; i < args.length; i++)
            this.cipherSuits[i-1] = args[i];

        this.socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.socket = (SSLServerSocket) this.socketFactory.createServerSocket(port);
        this.socket.setNeedClientAuth(true);
        this.socket.setEnabledProtocols(this.socket.getSupportedProtocols());
        this.socket.setEnabledCipherSuites(this.cipherSuits);
    }

    private void run() throws IOException {
        Socket dataSocket  = this.socket.accept();
        BufferedReader input = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

        String request = input.readLine();
        request.trim();

        PrintWriter output = new PrintWriter(dataSocket.getOutputStream(), true);
        String reply = this.process(request);

        System.out.println("Server: " + request + ": " + reply);
        output.println(reply);

        this.dataSocketClose(dataSocket);
    }

    private String process(String request) {
        String[] tokens = request.trim().split(" ");

        if(tokens[0].equals("register")) {
            if(!this.dnsIp.containsKey(tokens[1])) {
                this.dnsIp.put(tokens[1], tokens[2]);
                return tokens[1] + " " + tokens[2];
            } else {
                return "-1";
            }
        } else if(tokens[0].equals("lookup")) {
            if(!this.dnsIp.containsKey(tokens[1])) {
                return "-1";
            } else {
                return tokens[1] + " " + dnsIp.get(tokens[1]);
            }
        } else {
            System.out.println("Error parsing message");
            return "-1";
        }
    }

    public void dataSocketClose(Socket socket) throws IOException {
        socket.shutdownOutput();
        socket.close();
    }
}
