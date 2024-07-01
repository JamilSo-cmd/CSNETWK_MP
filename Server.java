import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public static void main(String args[]){

        int nPort = Integer.parseInt(args[0]);
		System.out.println("Server: Listening on port " + args[0] + "...");
		ServerSocket serverSocket;
		Socket serverEndpoint;

        try{
            serverSocket = new ServerSocket(nPort);
            serverEndpoint = serverSocket.accept();

			System.out.println("Server: New client connected: " + serverEndpoint.getRemoteSocketAddress());


            serverSocket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
			System.out.println("Server: Connection is terminated.");
        }

    }


}