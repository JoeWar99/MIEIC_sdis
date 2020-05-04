import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.*;
import java.util.HashMap;

/*
    java Server <port number>
        <port number> : port number the server shall use to provide the service
*/

public class SSLServer {
    public HashMap<String, String> dnsIp;
    private SSLServerSocket socket = null;
    private SSLServerSocketFactory socketFactory = null;
    public int port;
    private String[] cipherSuits;

    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port number>");
            return;
        }

        SSLServer server = new SSLServer(Integer.parseInt(args[0]), args);

        while(true) {
            server.run();
        }

        // server.serverSocketClose();
    }

    public SSLServer(int port, String[] args) throws IOException {

        this.port = port;
        this.dnsIp = new HashMap<String, String>();

        this.socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        this.socket = (SSLServerSocket) this.socketFactory.createServerSocket(port);

        this.socket.setNeedClientAuth(true);
        this.socket.setEnabledProtocols(this.socket.getSupportedProtocols());

        for(int i = 1; i < args.length; i++){
            this.cipherSuits[i-1] = args[i];
        }
        this.socket.setEnabledCipherSuites(this.cipherSuits);

        /*System.setProperty("javax.net.ssl.trustStore", "truststore");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
        System.setProperty("javax.net.ssl.keyStore", "server.keys");*/
    }

    private void run() throws IOException {

        Socket dataSocket  = this.socket.accept();

        BufferedReader input = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

        // parse request
        String request = input.readLine();
        request.trim();

        PrintWriter output = new PrintWriter(dataSocket.getOutputStream(), true);
        String reply = this.process(request);

        System.out.println(request + "::" + reply);

        output.println(reply);

        this.dataSocketClose(dataSocket);
    }

    private String process(String request) {
        String[] tokens = request.trim().split(" ");
        String reply;

        if(tokens[0].equals("register")) {
            if(!this.dnsIp.containsKey(tokens[1])) {
                this.dnsIp.put(tokens[1], tokens[2]);
                return this.dnsIp.size() + "\n" + tokens[1] + " " + tokens[2];
            } else {
                return -1 + "\n" + tokens[1] + " " + tokens[2];
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
            return "-1";
        }


    }

    public void dataSocketClose(Socket socket) throws IOException {
        socket.shutdownOutput();
        socket.close();
    }

    public void serverSocketClose() throws IOException {
        this.socket.close();
    }
}
