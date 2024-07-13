import java.net.Socket;
import java.util.*;


public class Client {
    
    Socket clientEndpoint;
    //driver for Client
    //run java Client <username>
    public static void main(String args[]){

		String sServerAddress;
		String username = "";
        String input; 
        int nPort;
        
        boolean flag = true;
        Scanner scanner = new Scanner(System.in);


        while(flag == true){
            System.out.println("Hello " + username +", Enter Command:");
            input = scanner.next();
            
            switch (input){

                //connnects client to server
                case "/join": 
                
                sServerAddress = scanner.next();
                nPort = scanner.nextInt();
                System.out.println("input is "+sServerAddress+" and " + nPort);
                connectToServer(sServerAddress,nPort);
                break;

                //disconnects client to server
                case "/leave":;
                break; 

                //registers a unique handle or alias
                case "/register":
                username = scanner.next();
                break;

                //sends file to server
                case "/store":;
                break;

                //request directory file list from a server
                case "/dir":;
                break;

                //fetch a file from a server
                case "/get":;
                break;
                
                //request command to help to output all input Syntax commands for references
                case "/?":;
                System.out.println(
                                    "Connect to the server application          ----- /join <server_ip_add> <port>\r\n" + //
                                    "Disconnect to the server application       ----- /leave\r\n" + //
                                    "Register a unique handle or alias          ----- /register <handle>\r\n" + //
                                    "Send file to server                        ----- /store <filename>\r\n" + //
                                    "Request directory file list from a server  ----- /dir\r\n" + //
                                    "Fetch a file from a server                 ----- /get <filename>\r\n");
                break;

                //closes the client
                case "/close": flag = false;
                break;
                default: System.out.println("Error: Command not found.");
            }
        }

        scanner.close();
    }
    
    //connects client to server
    public static void connectToServer(String sServerAddress, int nPort){
        
        try{

            Socket clientEndpoint = new Socket(sServerAddress, nPort);

			System.out.println("Connection to the File Exchange Server is successful!");
            
        }
        catch(Exception e){
			System.out.println("Error: Connection to the Server\r\n" + //
                                "has failed! Please check IP\r\n" + //
                                "Address and Port Number.");
        }
    }
}
