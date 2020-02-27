import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


public class client {

    /**
     * Class method used to send a command to server
     * @param messageToSend command that we want to send to the client in string form
     * @param serverAddress the server's ip address
     * @param serverPort the server's port
     * @param socket the socket the server is using to communicate with the clients
     * @throws IOException IO exception may be thrown when we do socket.send()
     */
    public static void sendCommand(String messageToSend, InetAddress serverAddress, int serverPort,  DatagramSocket socket) throws IOException{

        // the bytes we are going to send in the datagram packet, obtained from the string messageToSend
        byte[] commandBytes;

        // the effective datagram we are going to be sending to the server
        DatagramPacket comamndDatagram;

        commandBytes = messageToSend.getBytes();
        comamndDatagram = new DatagramPacket(commandBytes, commandBytes.length, serverAddress, serverPort);

        // sending the datagramPacket to the Server, it may throw an IOexpection, needs to be handled in the main method of the class
        socket.send(comamndDatagram);
    }

    /**
     * Class method used to receive the response to a command that was sent to the server
     * @param socket socket we are using for comunication
     * @param operation the operation we used in the command, used to provide to the user a better and more well suited response
     * @throws IOException
     */
    public static void receiveResponse(DatagramSocket socket, String operation) throws IOException{

        byte[] rbuf = new byte[65535];

        // the effective datagram we are going to be receiving from the server
        DatagramPacket responseDatagram;

        responseDatagram = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(responseDatagram);
        // display response
        String received = new String(responseDatagram.getData());
        received = received.trim();

        // the meaning of the response from the server
        String meaning = "";

        switch(operation){
            case "REGISTER":
                if(received.equals("-1")){
                    meaning = "The DNS name is already registered in the server use the LOOKUP command instead";
                }
                else{
                    meaning = "The <DNS name> <ip address> pair was registered with success";
                }
                break;

            case "LOOKUP":

                if(received.equals("NOT FOUND")){
                    meaning = "The DNS name is not registered in the server use the REGISTER command to do so";
                }
                else{
                    meaning = "The server returned with an IP address";
                }

                break;
        }

        System.out.println("Response received: " + received + " - " + meaning);
    }



    public static void main(String[] args) throws IOException {

        if (args.length < 4) {
            System.out.println("Usage: java Client <host> <port> <oper> <opnd>*");
            return;
        }

        InetAddress address = InetAddress.getLocalHost();
        String str = address.getHostAddress();
        System.out.println(str);

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(10000);

        // In the script provided by the teachers we have to provide the Ip address of the server but
        // since the server is supposed to be running in our computer in conjunction with the client
        // the ip adress of the server is the same as the client
        //InetAddress address = InetAddress.getByName(args[0]);

        int serverPort = Integer.parseInt(args[1]);
        DatagramPacket packet;
        byte[] commandBytes;
        String command = "";
        boolean resend = true;


        while(resend) {
            switch (args[2]) {
                case "REGISTER":
                    if (args.length - 3 != 2) {
                        System.out.println("Usage: java Client <host> <port> REGISTER <DNS NAME> <IP ADDRESS>");
                    } else {
                        command = args[2] + " " + args[3] + " " + args[4];
                    }
                    break;
                case "LOOKUP":
                    if (args.length - 3 != 1) {
                        System.out.println("Usage: java Client <host> <port> LOOKUP <DNS NAME>");
                    } else {
                        command = args[2] + " " + args[3];
                    }
                    break;
                default:
                    System.out.println("Command not recognized please try again");
                    break;
            }

            try {
                sendCommand(command, address, serverPort, socket);
                System.out.println("Command sent to the server: " + command);
                try {
                    receiveResponse(socket, args[2]);
                    resend = false;
                }
                catch(SocketTimeoutException e){
                    System.out.println(e.getMessage());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
        socket.close();
        return;
    }


}


