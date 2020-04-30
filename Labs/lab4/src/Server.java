import java.io.*;
import java.net.*;
import java.util.HashMap;

/*
    java Server <port number>
        <port number> : port number the server shall use to provide the service
*/

public class Server {
    public HashMap<String, String> dnsIp;
    public ServerSocket socket;
    public int port;

    public static void main(String args[]) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port number>");
            return;
        }

        Server server = new Server(Integer.parseInt(args[0]));

        while(true) {
            server.run();
        }

        // server.serverSocketClose();
    }

    public Server(int port) throws IOException {
        this.port = port;
        this.socket = new ServerSocket(port);
        this.dnsIp = new HashMap<String, String>();
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
