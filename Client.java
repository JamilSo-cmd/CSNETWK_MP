import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class Client {
    
    private static Socket clientEndpoint;
    private static String serverIP;
    private static int portNum;
    private static String alias = "";
    //driver for Client
    //run java Client <username>
    public static void main(String[] args) {
        String[] tokens;
        String input;
        String command;
        boolean flag = true;
    
        Scanner scanner = new Scanner(System.in);
    
        while (flag) {
            System.out.print("Enter Command: ");
            input = scanner.nextLine();
            tokens = input.split(" ");
            command = tokens[0];
    
            switch (command) {
                // Connects client to server
                case "/join":
                    if (tokens.length == 3) {
                        Client.serverIP = tokens[1];
                        Client.portNum = Integer.parseInt(tokens[2]);
                        Client.joinCommand();
                    } else {
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                        System.out.println("Usage: /join <server_ip_add> <port>");
                    }
                    break;
    
                // Disconnects client from server
                case "/leave":
                    Client.leaveCommand();
                    break;
    
                // Registers a unique handle or alias
                case "/register":
                    if (tokens.length == 2) {
                        alias = tokens[1];
                        Client.registerCommand(alias);
                    } else {
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                        System.out.println("Usage: /register <username>");
                    }
                    break;
    
                // Sends file to server
                case "/store":
                    if (tokens.length == 2) {
                        Client.storeCommand(tokens[1]);
                    } else {
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                        System.out.println("Usage: /store <file name>");
                    }
                    break;
    
                // Request directory file list from a server
                case "/dir":
                    Client.dirCommand();
                    break;
    
                // Fetch a file from a server
                case "/get":
                    if (tokens.length == 2) {
                        Client.getCommand(tokens[1]);
                    } else {
                        System.out.println("Error: Command parameters do not match or is not allowed.");
                        System.out.println("Usage: /get <file name>");
                    }
                    break;
    
                // Request command to help to output all input Syntax commands for references
                case "/?":
                    System.out.println(
                            "Connect to the server application          ----- /join <server_ip_add> <port>\r\n" +
                            "Disconnect to the server application       ----- /leave\r\n" +
                            "Register a unique handle or alias          ----- /register <handle>\r\n" +
                            "Send file to server                        ----- /store <filename>\r\n" +
                            "Request directory file list from a server  ----- /dir\r\n" +
                            "Fetch a file from a server                 ----- /get <filename>\r\n");
                    break;
    
                // Closes the client
                case "/close":
                    flag = false;
                    break;
    
                default:
                    System.out.println("Error: Command not found.");
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
            System.out.println("Connection to the File Exchange " + //
                                "Server is successful!");
        }
        catch(Exception e){
        //send error message
            System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.\n");
        }
    }

    private static void leaveCommand() {
        if (Client.clientEndpoint == null || Client.clientEndpoint.isClosed()) {
            System.out.println("Error: Disconnection failed. Please connect to the server first.");
        } 
        else {
            try {
                // Notify the server that the client is leaving
                OutputStream outputStream = Client.clientEndpoint.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                writer.println("leave");
    
                // Close the connection
                Client.clientEndpoint.close();
                System.out.println("Connection closed. Thank you!");
            } catch (Exception e) {
                System.out.println("Error: There is no connection to close.");
            }
        }
    }

    private static void registerCommand(String handle) {
        try {
            if (Client.clientEndpoint == null || Client.clientEndpoint.isClosed()) {
                throw new IllegalStateException("Error: Please connect to the server first using /join command.");
            }
    
            OutputStream outputStream = clientEndpoint.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
    
            // Sending registration command to server
            writer.println("register");  // Correct command format
            writer.println(handle);
    
            // Optionally, read response from server if needed
            InputStream inputStream = clientEndpoint.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String response = reader.readLine();
            //System.out.println("Client received response: " + response); // Debugging output
            if ("1".equals(response)) {
                System.out.println("Welcome " + handle + "!");
            } else {
                System.out.println("Error: Registration failed. Handle or alias already exists.");
            }
    
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.err.println("Error: Failed to send registration command.");
            e.printStackTrace();
        }
    }

    private static void storeCommand(String filename) {
        try {
            if (Client.clientEndpoint == null || Client.clientEndpoint.isClosed()) {
                throw new IllegalStateException("Error: Please connect to the server first using /join command.");
            } 
            else if ("".equals(alias)){
                System.out.println("Error: User is unregistered. Please register first.");
            }
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
            ByteBuffer sizeBuffer = ByteBuffer.allocate(8).putLong(fileSize);
            outputStream.write(sizeBuffer.array());
    
            // Send file content
            byte[] buffer = new byte[8192];
            try (FileInputStream fileInputStream = new FileInputStream(fileToSend)) {
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz);
            String nowAsISO = df.format(new Date());

            outputStream.flush();
            System.out.println( alias +"<"+ nowAsISO +">: Uploaded "+ filename);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Error: Failed to send file to server.");
            e.printStackTrace();
        }
    }
    

    private static void dirCommand() {
        if (Client.clientEndpoint == null || Client.clientEndpoint.isClosed()) {
            System.out.println("Error: Please connect to the server first using /join command.");
            return;
        }
        else if ("".equals(alias)){
            System.out.println("Error: User is unregistered. Please register first.");
            return;
        }
        try {
            // Send 'dir' command
            OutputStream outputStream = clientEndpoint.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("dir");
    
            // Receive directory list from server
            InputStream inputStream = clientEndpoint.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String fileName;
            System.out.println("Directory file list:");
            while ((fileName = reader.readLine()) != null) {

                if ("END_OF_LIST".equals(fileName)) {
                    break;
                }
                if("ClientHandler.class".equals(fileName) || "Server.class".equals(fileName) || "Server.java".equals(fileName) ){}
                else{System.out.println(fileName);}
            }
        } catch (IOException e) {
            System.out.println("Error: Failed to request directory file list from server.");
            e.printStackTrace();
        }
    }
    

    private static void getCommand(String filename) {
        if (Client.clientEndpoint == null || Client.clientEndpoint.isClosed()) {
            System.out.println("Error: Please connect to the server first using /join command.");
            return;
        }
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

        } catch (IOException e) {
            System.out.println("Error: Failed to retrieve file from server.");
        }
    }

    private static void receiveFile(String filename, InputStream input) throws IOException {
        byte[] sizeBytes = input.readNBytes(4);
        int fileSize = ByteBuffer.wrap(sizeBytes).getInt();
        try (FileOutputStream fileOutput = new FileOutputStream(filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalRead = 0;
            while ((bytesRead = input.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                if (totalRead >= fileSize) {
                    break;
                }
            }
        }
    }
}
