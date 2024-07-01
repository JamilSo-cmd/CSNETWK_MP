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
                break;

                //closes the client
                case "/close": flag = false;
                break;
                default: System.out.println("try again");
            }
        }

    }
    
    //connects client to server
    public static void connectToServer(String sServerAddress, int nPort){
        
        try{

            Socket clientEndpoint = new Socket(sServerAddress, nPort);

			System.out.println("Client: Connected to server at" + clientEndpoint.getRemoteSocketAddress());
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
