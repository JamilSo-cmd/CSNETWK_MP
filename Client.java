import java.net.Socket;
import java.util.*;


public class Client {
    
    Socket clientEndpoint;
    //driver for Client
    //run java Client <username>
    public static void main(String args[]){

		String sServerAddress;
		String username = args[0];
        String input; 
        int nPort;
        
        boolean flag = true;
        Scanner scanner = new Scanner(System.in);


        while(flag == true){
            System.out.println("Hello " + username +", Enter Command:");
            input = scanner.next();
            
            switch (input){

                case "/join": 
                sServerAddress = scanner.next();
                nPort = scanner.nextInt();
                System.out.println("input is "+sServerAddress+" and " + nPort);
                connectToServer(sServerAddress,nPort);
                break;
                case "/leave":;
                break; 
                case "/register":;
                break;
                case "/store":;
                break;
                case "/dir":;
                break;
                case "/get":;
                break;
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
