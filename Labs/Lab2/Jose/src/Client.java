import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * public class used to represent the client of a dns server who intends to either
 * register a dns name to a IP address or wishes to lookup a previous registered IP address for
 * a specific dns name.
 *
 * The client program shall be invoked as follows:
 * java Client <host> <port> <oper> <opnd>*
 * where:
 * <host>
 * is the DNS name (or the IP address, in the dotted decimal format) where the server is running
 * <port>
 * is the port number where the server is providing service
 * <oper>
 * is the operation to request from the server, either "register" or "lookup"
 * <opnd>*
 * is the list of operands of that operation
 * <DNS name> <IP address> for register
 * <DNS name> for lookup
 */

public class Client {

    private InetAddress serverAddress;
    private int serverPort;
    private InetAddress multicastAddress;
    private int multicastPort;
    private DatagramSocket socket;
    private String issuedCommand;
    private String operation;
    private final int numberTimeouts = 3;
    private final int timeToLive = 1;


    // regex used to check if the IP address for the server sent from the command line is a valid one or not
    private static final String IPv4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
    private static final Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

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
     * Generic function to get sub-array of a non-primitive array
     * between specified indices
     * @param array array of elements of a certain type
     * @param beg begin index
     * @param end end index
     * @param <T> the type of variables or objects being stored in the array
     * @return returns an array of the same type as array param
     */
    public static<T> T[] subArray(T[] array, int beg, int end) {
        return Arrays.copyOfRange(array, beg, end + 1);
    }

    /**
     * main method
     * @param args arguments sent from the command line. How the client program shall be invoked is described in detail
     * on top of the declaration of the client Class
     */
    public static void main(String[] args){

        // validating the arguments from the command line
        // verifying the number of arguments passed in the command line
        if(args.length < 4){
            System.out.println("Usage: java Client <mcast_address> <mcast_port> <oper> <opnd>*");
            return;
        }

        // checking if the multicast address for the server is a valid one or not
        if(!isValidInet4Address(args[0])){
            System.out.println("Error: the multicast address " +  args[0] + " being sent in the command line is not a valid IP address (IPv4). The range for IP addresses is 224.0.0.0 to 239.255.255.255.");
            return;
        }

        int multicastPort = Integer.parseInt(args[1]);

        if(multicastPort < 0 || multicastPort > 64738){
            System.out.println("Error: the multicast port " + multicastPort + " being sent in the command line is not a valid one. Port numbers range from 0 all the way up to 64738." );
            return;
        }

        switch(args[2]){
            case "LOOKUP":
                if((args.length - 3) != 1){
                    System.out.println("Error: the operation " + args[2] + " is suppose to only have one operand, the <DNS name>, instead it has "+ (args.length - 3)+".");
                    return;
                }
                break;
            case "REGISTER":
                if((args.length - 3) != 2){
                    System.out.println("Error: the operation " + args[2] + " is suppose to only have two operands, the <DNS name> and <IP address>, instead it has "+ (args.length - 3)+".");
                    return;
                }
                break;
            default:
                System.out.println("Error: the operation " + args[2] + " is not valid one. REGISTER and LOOKUP are the only valid operations." );
                return;
        }

        InetAddress aux;

        try {
            aux = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        String[] command = subArray(args, 2, args.length - 1);

        Client client = new Client(aux, multicastPort, command);

        try {
            client.openSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        client.executeRequest();

        client.closeSocket();

        return;
    }

    /**
     * Client's constructor
     * @param multicastAddress the multicast address
     * @param multicastPort the multicast port
     * @param command the command to send to the server
     */
    public Client(InetAddress multicastAddress, int multicastPort, String[] command){

        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;

        this.issuedCommand = new String();
        for(String element : command){
            this.issuedCommand += element + " ";
        }

        this.operation = command[0];

    }

    /**
     * Class method that creates the datagramSocket and imposes the timeout period in case
     * the client gets stuck for too long sending a command to the server or receiving a
     * response from the server
     */
    public void openSocket() throws SocketException {
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(10000);
    }

    /**
     * Class method that closes the datagramSocket
     */
    public void closeSocket(){
        this.socket.close();
    }

    /**
     * Class method used to send a command to the server and receive response.
     * Also handles an type of timeout exception that may happen in case the server
     * doesnt respond in time.
     * @throws IOException IO exception may be thrown when we do socket.send()
     */
    public void executeRequest(){

        int timeouts = 0;

        // Test case basic, server and client running in the same machine
        /*  this.serverPort = 4445;
        try {
            this.serverAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }*/

        while(timeouts != this.numberTimeouts){
            try {
                this.receiveMulticast();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                timeouts++;
                continue;
            }
        }


        if(timeouts != this.numberTimeouts) {
            timeouts = 0;
            while (timeouts != this.numberTimeouts) {

                try {
                    this.sendCommand();
                } catch (IOException e) {
                    e.printStackTrace();
                    timeouts++;
                    continue;
                }

                try {
                    this.receiveResponse();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    timeouts++;
                }
            }
        }

        if(timeouts == this.numberTimeouts){
            System.out.println("Client : " + this.issuedCommand + " : " + "ERROR number of timeouts exceeded");
        }
        return;
    }


    /**
     * Class method used to send a command to server
     * @throws IOException IO exception may be thrown when we do socket.send()
     */
    public void sendCommand() throws IOException{

        // creating the datagramPacket from the command the user want to be sent to the server
        byte[] sentData = this.issuedCommand.getBytes();
        DatagramPacket commandDatagram = new DatagramPacket(sentData, sentData.length, this.serverAddress, this.serverPort);

        // sending the datagramPacket to the Server, it may throw an IOexpection, needs to be handled in the main method of the class
        this.socket.send(commandDatagram);
    }

    /**
     * Class method used to receive the response to a command that was sent to the server
     * @throws IOException
     */
    public void receiveResponse() throws IOException{

        // creating the datagramPacket to where the response from the server will be stored
        byte[] receivedData = new byte[65535];
        DatagramPacket responseDatagram = new DatagramPacket(receivedData, receivedData.length);

        // receiving from the socket
        this.socket.receive(responseDatagram);
        String receivedResponse = new String(responseDatagram.getData());

        // trimming the string because of the extra additional bytes at the beginning or end
        receivedResponse = receivedResponse.trim();

        // display response
        System.out.println("Client : " + this.issuedCommand + " : " + receivedResponse);

    }

    /**
     * class method used to receive the multicast advertisement sent out by the server
     * @throws IOException
     */
    public void receiveMulticast() throws IOException {

        MulticastSocket multicastSocket = null;
        multicastSocket = new MulticastSocket(this.multicastPort);

        multicastSocket.setLoopbackMode(false); // Use 'false' to enable support for more than one node on the same machine.

        InetAddress group = this.multicastAddress;
        multicastSocket.joinGroup(group);
        if (this.timeToLive != -1)
            multicastSocket.setTimeToLive(this.timeToLive);



        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        multicastSocket.receive(packet);

        String received = new String(packet.getData(), 0, packet.getLength());
        received = received.trim();
        multicastSocket.leaveGroup(group);
        multicastSocket.close();

        String[] tokens = received.split(":", 3);

        System.out.println("Client was able to receive the IP address of the server " + received);

        // The IP address of the local host is like this DESKTOP-H3MJCCQ/10.227.144.41:53
        // gives an error when using InetAddress getbyname method
        String[] tokens1 = tokens[0].split("/", 3);
        this.serverAddress = InetAddress.getByName(tokens1[1]);
        this.serverPort = Integer.parseInt(tokens[1]);
    }





}


