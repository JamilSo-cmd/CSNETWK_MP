import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;


public class Client {
    
    private static Socket clientEndpoint;
    private static String serverIP;
    private static int portNum;
    //driver for Client
    //run java Client <username>
    public static void main(String args[]){
        String[] tokens;
        String input;
        String command;
        boolean flag = true;

        Scanner scanner = new Scanner(System.in);

        while(flag == true){
            System.out.print("Enter Command: ");
            input = scanner.nextLine();
            tokens = input.split(" ");
            command = tokens[0];

            switch (command){
                //connnects client to server
                case "/join": 
                    if(tokens.length == 3) {
                        Client.serverIP = tokens[1];
                        Client.portNum = Integer.parseInt(tokens[2]);
                        Client.joinCommand();
                    } else {
                        System.out.println("Invalid command syntax. Usage: /join <server_ip_add> <port>");
                    }
                break;

                //disconnects client to server
                case "/leave":
                    Client.leaveCommand();
                break; 

                //registers a unique handle or alias
                case "/register":
                    if(tokens.length == 2) {
                        String alias = tokens[1];
                        Client.registerCommand(alias);
                    } else {
                        System.out.println("Invalid command syntax. Usage: /register <username>");
                    }
                break;

                //sends file to server
                case "/store":
                    if(tokens.length == 2) {
                        Client.storeCommand(tokens[1]);
                    } else {
                        System.out.println("Invalid command syntax. Usage: /store <file name>");
                    }
                break;

                //request directory file list from a server
                case "/dir":;
                break;

                //fetch a file from a server
                case "/get":;
                    if(tokens.length == 2) {
                        Client.getCommand(tokens[1]);
                    } else {
                        System.out.println("Invalid command syntax. Usage: /store <file name>");
                    }
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

    
    private static void joinCommand() {
        try{
            //connecting to Server
            System.out.println("Connecting at " + Client.serverIP + " at " + Client.portNum + "..."); 
            Client.clientEndpoint = new Socket(Client.serverIP, Client.portNum);

            OutputStream outputStream = clientEndpoint.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            writer.write("/join\n"); // Sending the command
            writer.write(serverIP + "\n"); // Sending server IP
            writer.write(portNum + "\n"); // Sending port number
            writer.flush();
            System.out.println("Connection to the server is successful!");
        }
        catch(Exception e){
        //send error message
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.\n");
        }
    }

    private static void registerCommand(String handle) {
        try {
            OutputStream outputStream = clientEndpoint.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
    
            // Sending registration command to server
            writer.println("register");
            writer.println(handle);
    
            // Optionally, read response from server if needed
            InputStream inputStream = clientEndpoint.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();
            if ("1".equals(response)) {
                System.out.println("Welcome " + handle + "!");
            } else {
                System.out.println("Error: Registration failed. Handle or alias already exists.");
            }
    
        } catch (IOException e) {
            System.err.println("Error: Failed to send registration command.");
            e.printStackTrace();
        }
    }

    private static void leaveCommand() {
        try{
            Client.clientEndpoint.close();
            System.out.println("Connection closed. Thank you!");
            //send notif to server that client has left
        }
        catch(Exception e){
            System.out.println("Error: There is no connection to close.");
        }
    }

    private static void storeCommand(String filename) {
        try {
            File fileToSend = new File(filename);
            if (!fileToSend.exists()) {
                System.out.println("Error: File not found.");
                return;
            }

            // Send 'store' command
            OutputStream outputStream = clientEndpoint.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("store");

            // Send filename
            writer.println(filename);

            // Send file size
            long fileSize = fileToSend.length();
            ByteBuffer sizeBuffer = ByteBuffer.allocate(4).putInt((int) fileSize);
            outputStream.write(sizeBuffer.array());

            // Send file content
            byte[] buffer = new byte[8192];
            FileInputStream fileInputStream = new FileInputStream(fileToSend);
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            fileInputStream.close();
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Error: Failed to send file to server.");
        }
    }

    private static void getCommand(String filename) {
        try {
            // Send 'get' command
            OutputStream outputStream = clientEndpoint.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("get");

            // Send filename
            writer.println(filename);

            // Receive response from server
            InputStream inputStream = clientEndpoint.getInputStream();
            byte[] responseBytes = new byte[1];
            inputStream.read(responseBytes);
            int response = ByteBuffer.wrap(responseBytes).get();

            if (response == 1) {
                // File exists on server, receive the file
                receiveFile(filename, inputStream);
                System.out.println("File received from Server: " + filename);
            } else {
                // File does not exist on server
                System.out.println("Error: File not found on the server.");
            }

            // Clean up
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to retrieve file from server.");
        }
    }

    private static void receiveFile(String filename, InputStream inputStream) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.out.println("Error: Failed to write file.");
        }
    }
}
