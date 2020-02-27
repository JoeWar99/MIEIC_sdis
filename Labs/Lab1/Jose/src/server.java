
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class server {
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


    public static void main(String[] args) throws IOException {

        // checking to see if the server is initialized with the correct parameters that being the number of the port we are
        // going to use for the communication (4445)
        if (args.length != 1) {
            System.out.println("Usage: java Server <port number>");
            return;
        }

        // byte array used to receive the datagram data since it comes in bytes
        byte[] receivedBytes;

        // the string we are going to use to store the converted bytes into readable text UTF8
        String receivedString;

        // The datagramPacket variable we are going to store the datagram we received from the client
        DatagramPacket DpReceived;

        // The response string we are gonna prepare to send to the client
        String response;

        // the port we are going to be use to open the datagramSocket, for this work it is 4445
        int port = Integer.parseInt(args[0]);

        // creating the socket to send messages to and receive from
        DatagramSocket socket = new DatagramSocket(port);

        // hashmap to hold all the combinations of dns names and ip addresses
        HashMap<String, String> dnsHashMap = new HashMap<String, String>();

        // Used to determine if the dns name we receive from the client is present in the hash table
        boolean isKeyPresent;

        // used to store the hashmapsize, when the user asks to register a dns name if sucessfull we are going
        // to respond with number of dns name already registered
        int hashMapSize;

        // used to store the parameters and operation received from the client
        String[] tokens;

        // main part of the server's code
        while(true){
            response = "";
            receivedBytes =  new byte[65535];
            // creating a new datagramPacket so that we can received and store the one received from the client
            DpReceived = new DatagramPacket(receivedBytes, receivedBytes.length);

            System.out.println("Waiting for command from a client.......");

            // waiting to receive a datagram packet from the client,  the execution of the program stops here until that happens
            try {
                socket.receive(DpReceived);
            }
            catch(IOException e){
                System.out.println(e.getMessage());
                continue;
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                continue;
            }
            // converting the bytes into string format UTF8
            receivedString = new String(DpReceived.getData());
            receivedString = receivedString.trim();

            System.out.println("(1) Client's commmand:" + receivedString);

            // splitting the command up for processing
            tokens = receivedString.split(" ", 3);

            // there are no commands that can come from the client that have less than or equal to 1 argument
            if(tokens.length <= 1){
                System.out.println("Error in the command received");
                continue;
            }

            switch (tokens[0]){
                case "REGISTER":

                    // checking if the command REGISTER that came from the client has the correct number of arguments
                    if(tokens.length != 3){
                        System.out.println("Error in the messaged received, register command should have 3 arguments");
                    }
                    else{
                        // checking if the key is present or not already in the hashMap
                        isKeyPresent = dnsHashMap.containsKey(tokens[1]);
                        if(isKeyPresent == true){
                            System.out.println("The key the client tried to register is already registered, try using the lookup command");

                            //preparing the response in string format to send to the client
                            response = String.valueOf(-1);
                        }
                        else {
                            System.out.println("Key was successfully added to the dns server " + tokens[1] + " " + tokens[2]);

                            // adding the pair DNSname and IpAdress to the HashMap
                            dnsHashMap.put(tokens[1], tokens[2]);

                            // getting the hashMap size to send to the client has a response
                            hashMapSize = dnsHashMap.size();

                            //preparing the response in string format to send to the client
                            response = String.valueOf(hashMapSize);
                        }

                        // sending the response to the client
                        try {
                            sendMessage(response, DpReceived, socket);
                        }
                        catch(IOException e){
                            System.out.println(e.getMessage());
                            continue;
                        }
                    }
                    break;
                case "LOOKUP":
                    // checking if the command LOOKUP that came from the client has the correct number of arguments
                    if(tokens.length != 2){
                        System.out.println("Error in the messaged received, lookup command should have 2 arguments");
                    }
                    else{
                        // checking if the key is present or not already in the hashMap
                        isKeyPresent = dnsHashMap.containsKey(tokens[1]);

                        System.out.println("Does key "
                                + tokens[1]
                                + " exists: "
                                + isKeyPresent);
                        if(isKeyPresent == true){
                            System.out.println("The dns name is in the dns server, sending the ip Address to th client");

                            // preparing the response to send to the client in case of success of the command LOOKUP
                            response = tokens[1] + " " + dnsHashMap.get(tokens[1]);
                        }
                        else {
                            System.out.println("the dns name is not in the dns server, try using the register command");

                            // preparing the response to send to the client in case of failure of the command LOOKUP
                            response = "NOT FOUND";
                        }

                        // sending the response to the client
                        try {
                            sendMessage(response, DpReceived, socket);
                        }
                        catch(IOException e){
                            System.out.println(e.getMessage());
                            continue;
                        }
                    }
                    break;

                default:
                    // preparing and sending a response to the client in case the command that was sent was not a valid one
                    System.out.println("The command is not valid. Valid command are REGISTER and LOOKUP");
                    response = String.valueOf(-2);

                    // sending the response to the client
                    try {
                        sendMessage(response, DpReceived, socket);
                    }
                    catch(IOException e){
                        System.out.println(e.getMessage());
                        continue;
                    }
                    break;
            }
            System.out.println("Server's Response: " + response);
        }
    }
}

