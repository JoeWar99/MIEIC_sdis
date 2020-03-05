import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
    java Client <host_name> <remote_object_name> <oper> <opnd>*
        <host_name> : is the DNS name (or the IP address, in the dotted decimal format) where the server is running
        <remote_object_name> : The name to which the remote object shall be bound
        <oper> : can be either "register" or "lookup"
        <opnd>* : for "register" -> <DNS name> <IP address>
                : for "lookup"   -> <DNS name>
*/

public class Client {

    public static void main(String[] args) {
        if(!(args[2].equals("register") && args.length == 4) && !(args[2].equals("lookup") && args.length == 4)) {
            System.out.println("Usage: java Client <host_name> <remote_object_name> <oper> <opnd>*");
        }

        try {
            String response;
            Registry registry = LocateRegistry.getRegistry(args[0]);
            ServerInterface serverStub = (ServerInterface) registry.lookup(args[1]);

            if(args[2].equals("register")) {
                response = serverStub.register(args[3], args[4]);
            }
            else if(args[2].equals("lookup")) {
                response = serverStub.lookup(args[3]);
            }
            else {
                System.err.println("Usage: java Client <host_name> <remote_object_name> <oper> <opnd>");
                return;
            }

            System.out.println("response: " + response);

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}