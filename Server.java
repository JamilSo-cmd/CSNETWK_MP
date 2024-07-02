import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        System.out.println("Server: Listening on port " + port + "...");
        List<String> aliases = Collections.synchronizedList(new ArrayList<>());

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ExecutorService executorService = Executors.newCachedThreadPool();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Server: New client connected: " + clientSocket.getRemoteSocketAddress());
                executorService.execute(new ClientHandler(clientSocket, aliases));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server: Connection is terminated.");
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final List<String> aliases;

    public ClientHandler(Socket clientSocket, List<String> aliases) {
        this.clientSocket = clientSocket;
        this.aliases = aliases;
    }

    @Override
    public void run() {
        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output, true);
            boolean isConnected = true;

            while (isConnected) {
                String command = reader.readLine();
                if (command == null) {
                    continue;
                }
                switch (command) {
                    case "leave":
                        isConnected = false;
                        clientSocket.close();
                        break;
                    case "dir":
                        sendDir(writer);
                        break;
                    case "register":
                        String alias = reader.readLine();
                        registerAlias(alias, writer);
                        break;
                    case "store":
                        String filenameToStore = reader.readLine();
                        receiveFile(filenameToStore, input);
                        break;
                    case "get":
                        String filenameToGet = reader.readLine();
                        sendFile(filenameToGet, output);
                        break;
                    default:
                        writer.println("Invalid command");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDir(PrintWriter writer) {
    
    }

    private void registerAlias(String alias, PrintWriter writer) {
       
    }

    private void receiveFile(String filename, InputStream input) throws IOException {
       
    }

    private void sendFile(String filename, OutputStream output) throws IOException {
      
    }
}

