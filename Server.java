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
        File[] files = new File(".").listFiles();
        if (files != null) {
            for (File file : files) {
                writer.println(file.getName());
            }
        }
    }

    private void registerAlias(String alias, PrintWriter writer) {
        if (!aliases.contains(alias)) {
            aliases.add(alias);
            writer.println("1");
        } else {
            writer.println("0");
        }
        writer.flush();
    }    

    private void receiveFile(String filename, InputStream input) throws IOException {
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

    private void sendFile(String filename, OutputStream output) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            output.write(1);
            byte[] sizeBytes = ByteBuffer.allocate(4).putInt((int) file.length()).array();
            output.write(sizeBytes);
            try (FileInputStream fileInput = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInput.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
        } else {
            output.write(0);
        }
        output.flush();
    }
}

