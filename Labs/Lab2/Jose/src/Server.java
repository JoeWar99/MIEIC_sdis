import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Server {

    // regex used to check if the IP address for the server sent from the command line is a valid one or not
    private static final String IPv4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
    private static final Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

    private static DatagramSocket socket = null;
    private final InetAddress multicastAddress;
    private final int multicastPort;
    private InetAddress serverAddress;
    private final int serverPort;
    private HashMap<String, String> dnsHashMap;
    private DatagramPacket packetReceived;
    private String command;
    private  String response;

    /**
     * class method used to verify the IP address for the server sent from the command line, uses simple regex + Custom Validations
     * @param ip the IP address we wish to validate
     * @return true if valid IP address, false otherwise
     */
    public static boolean isValidInet4Address(String ip) {
        if (ip == null) {
            return false;
        }
        if (!IPv4_PATTERN.matcher(ip).matches())
            return false;
        String[] parts = ip.split("\\.");

        // verify that each of the four subgroups of IPv4 address is legal
        try {
            for (String segment : parts) {
                // x.0.x.x is accepted but x.01.x.x is not
                if (Integer.parseInt(segment) > 255 ||
                        (segment.length() > 1 && segment.startsWith("0"))) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Class method to see if the port is available or not
     * @param port
     * @return true if the port isn't being used by a service, false otherwise
     */
    private static boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    /**
     * Class method used to send a response to a client's command
     * @param messageToSend message that we want to send to the client in string form
     * @param DpReceived datagram we received with the command from the client
     * @param socket the socket the server is using to communicate with the clients
     * @throws IOException IO exception may be thrown when we do socket.send()
     */
    public static void sendMessage(String messageToSend, DatagramPacket DpReceived, DatagramSocket socket) throws IOException{

        // used to store the ip adress of the client obtained from the datagramPacket we received from the client
        InetAddress address;

        // the bytes we are going to send in the datagram packet, obtained from the string messageToSend
        byte[] sentBytes;

        // the effective datagram we are going to be sending to the client
        DatagramPacket DpSent;

        // the client's port obtained form the datagramPacket we received from the client
        int clientPort;

        clientPort = DpReceived.getPort();
        address = DpReceived.getAddress();
        sentBytes = messageToSend.getBytes();
        DpSent = new DatagramPacket(sentBytes, sentBytes.length, address, clientPort);

        // sending the datagramPacket to the client, it may throw an IOexpection, needs to be handled in the main method of the class
        socket.send(DpSent);
    }

    public static void main(String[] args) {

        // checking to see if the server is initialized with the correct parameters that being the number of the port we are
        // going to use for the communication (4445)
        if (args.length != 3) {
            System.out.println("Usage: java Server <srvc_port> <mcast_address> <mcast_port>");
            return;
        }

        if(!isValidInet4Address(args[1])){
            System.out.println("The multicast address is not a valid one");
            return;
        }

        if(!available(Integer.parseInt(args[0]))){
            System.out.println("The port " + args[1] + ", that was to be used by the server to respond to clients commands is already in use by another service");
            return;
        }

        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        InetAddress multicastAddress = null;
        try {
            multicastAddress = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        Server server = new Server(multicastAddress, Integer.parseInt(args[2]),  serverAddress,  Integer.parseInt(args[0]));

        server.multicastThreadSetup();

        try {
            server.openSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        String command;
        String response;

        while(true){


            if(server.receiveCommand()!=0){
                System.out.println("Problem Receiving command from a client");
                continue;
            };

            server.processCommand();


            try {
                server.issueResponse();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Problem sending a response to a client");
            }


        }

    }

    public Server(InetAddress multicastAddress, int multicastPort, InetAddress serverAddress, int serverPort){

        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.dnsHashMap = new HashMap<String, String>();

    }

    public int receiveCommand(){

        byte[] receivedBytes = new byte[65535];
        this.packetReceived = new DatagramPacket(receivedBytes, receivedBytes.length);

        System.out.println("Waiting for command from a client.......");

        // waiting to receive a datagram packet from the client,  the execution of the program stops here until that happens
        try {
            socket.receive(this.packetReceived);
        }
        catch(IOException e){
            System.out.println(e.getMessage());
            return -1;
        }

        command = new String(this.packetReceived.getData());
        command = command.trim();

        return 0;

    }

    public void processCommand() {

        boolean isKeyPresent;
        String[] tokens = command.split(" ", 3);
        response = new String();

        // there are no commands that can come from the client that have less than or equal to 1 argument
        if (tokens.length <= 1) {
            System.out.println("Error in the command received");
            response = "ERROR";
        } else {
            switch (tokens[0]) {
                case "REGISTER":

                    // checking if the command REGISTER that came from the client has the correct number of arguments
                    if (tokens.length != 3) {
                        System.out.println("Error in the messaged received, register command should have 3 arguments");
                    } else {
                        // checking if the key is present or not already in the hashMap
                        isKeyPresent = dnsHashMap.containsKey(tokens[1]);
                        if (isKeyPresent) {
                            System.out.println("The key the client tried to register is already registered, try using the lookup command");

                            //preparing the response in string format to send to the client
                            response = String.valueOf(-1);
                        } else {
                            System.out.println("Key was successfully added to the dns server " + tokens[1] + " " + tokens[2]);

                            // adding the pair DNSname and IpAdress to the HashMap
                            dnsHashMap.put(tokens[1], tokens[2]);

                            // getting the hashMap size to send to the client has a response
                            int hashMapSize = dnsHashMap.size();

                            //preparing the response in string format to send to the client
                            response = String.valueOf(hashMapSize);
                        }
                    }
                    break;
                case "LOOKUP":
                    // checking if the command LOOKUP that came from the client has the correct number of arguments
                    if (tokens.length != 2) {
                        System.out.println("Error in the messaged received, lookup command should have 2 arguments");
                    } else {
                        // checking if the key is present or not already in the hashMap
                        isKeyPresent = dnsHashMap.containsKey(tokens[1]);

                        System.out.println("Does key "
                                + tokens[1]
                                + " exists: "
                                + isKeyPresent);
                        if (isKeyPresent) {
                            System.out.println("The dns name is in the dns server, sending the ip Address to th client");

                            // preparing the response to send to the client in case of success of the command LOOKUP
                            response = tokens[1] + " " + dnsHashMap.get(tokens[1]);
                        } else {
                            System.out.println("the dns name is not in the dns server, try using the register command");

                            // preparing the response to send to the client in case of failure of the command LOOKUP
                            response = "NOT FOUND";
                        }
                    }
                    break;

                default:
                    // preparing and sending a response to the client in case the command that was sent was not a valid one
                    System.out.println("The command is not valid. Valid command are REGISTER and LOOKUP");
                    response = "ERROR";
                    break;
            }
            System.out.println("Server :" + command + " - " + response);
        }
    }
    
    public void issueResponse() throws IOException {

        // used to store the ip adress of the client obtained from the datagramPacket we received from the client
        InetAddress address;

        // the bytes we are going to send in the datagram packet, obtained from the string messageToSend
        byte[] sentBytes;

        // the effective datagram we are going to be sending to the client
        DatagramPacket DpSent;

        // the client's port obtained form the datagramPacket we received from the client
        int clientPort;

        clientPort = this.packetReceived.getPort();
        address = this.packetReceived.getAddress();
        sentBytes = response.getBytes();
        DpSent = new DatagramPacket(sentBytes, sentBytes.length, address, clientPort);

        // sending the datagramPacket to the client, it may throw an IOexpection, needs to be handled in the main method of the class
        socket.send(DpSent);

    }

    /**
     * Class method that creates the datagramSocket and imposes the timeout period in case
     * the client gets stuck for too long sending a command to the server or receiving a
     * response from the server
     */
    public void openSocket() throws SocketException {
        socket = new DatagramSocket(this.serverPort);
    }

    /**
     * Class method that closes the datagramSocket
     */
    public void closeSocket(){
        socket.close();
    }

    /**
     * Class method used to setup the thread responsible for the advertisement part of the server
     */
    public void multicastThreadSetup(){
        Runnable runnable = new MyRunnable(multicastAddress, multicastPort,serverAddress, serverPort);
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
    }


    /**
     * Class that implements interface runnable, used to send, by the other server thread a timed and cyclic advertisement of the server
     * to the multicast group
     */
    public static class MyRunnable implements Runnable {

        private InetAddress multicastAddress;
        private int multicastPort;
        private String advertisement;


        public MyRunnable(InetAddress multicastAddress, int multicastPort, InetAddress serverAddress, int serverPort){

            this.multicastAddress = multicastAddress;
            this.multicastPort = multicastPort;
            this.advertisement = serverAddress + ":" + serverPort;

        }

        public void run() {

            MulticastSocket socket;
            try {
                socket = new MulticastSocket(this.multicastPort);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                socket.setTimeToLive(1);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }


            byte[] buf = this.advertisement.getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.multicastAddress, this.multicastPort);

            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            System.out.println("multicast:" + this.multicastAddress.toString() + " " + this.multicastPort + " : " + this.advertisement);
            socket.close();

        }
    }
}

