import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
    java Client <host> <port> <oper> <opnd>*
        <host> : is the DNS name (or the IP address, in the dotted decimal format) where the server is running
        <port> : port number the server is using to provide the service
        <oper> : can be either "register" or "lookup"
        <opnd>* : for "register" -> <DNS name> <IP address>
                : for "lookup"   -> <DNS name>
*/

public class Client {

    public static void main(String[] args) throws RemoteException, NotBoundException {
        if(!(args[2].equals("register") && args.length == 5) && !(args[2].equals("lookup") && args.length == 4)) {
            System.out.println("Usage: java Client <host> <port> <oper> <opnd>*");
        }

        String host_name = args[0];
        String object_name = args[1];
        String operation = args[2];
        String dns_name = args[3];
        // Client client = new Client(args[0], args[1], args[2], args[3]);


        Registry registry = LocateRegistry.getRegistry(null);
        ServerInterface serverStub = (ServerInterface) registry.lookup(object_name);
        //ServerInterface serverStub = (ServerInterface) registry.register(object_name);
        System.out.println(serverStub.lookup(dns_name));
    }

    /*public Client(String hostName, String objectName, String operation, String dnsName) throws IOException {
        public Client(String hostName, String objectName, String operation, String dnsName) throws IOException {
            this.hostName = hostName;
            this.objectName = objectName;
            this.operation = operation;
            this.dnsName = dnsName;
        }
    }*/
}





/*
*  usar a classe naming ou o outro metodo static
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
*
* */