import java.net.Socket;
import java.util.*;


public class Client {
    
    public static void main(String args[]){

		String sServerAddress = args[0];
		int nPort = Integer.parseInt(args[1]);
        
        try{

            Socket clientEndpoint = new Socket(sServerAddress, nPort);

			System.out.println("Client: Connected to server at" + clientEndpoint.getRemoteSocketAddress());
            
			clientEndpoint.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
			System.out.println("Client: Connection is terminated.");
        }
    }

}
