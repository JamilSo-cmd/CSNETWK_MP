import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private static volatile boolean running = true;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        System.out.println("Server: Listening on port " + port + "...");
        
        // Reinitialize aliases list each time server starts
        List<String> aliases = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executorService = Executors.newCachedThreadPool();

        // Shutdown hook to stop the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server: Shutting down...");
            running = false;
            executorService.shutdown();
            try {
                executorService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Server: Shutdown complete.");
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                try {
                    serverSocket.setSoTimeout(1000); // Set timeout to check running flag periodically
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Server: New client connected: " + clientSocket.getRemoteSocketAddress());
                    executorService.execute(new ClientHandler(clientSocket, aliases));
                } catch (SocketTimeoutException e) {
                    // Timeout exception is expected, ignore it
                }
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
    private String clientAlias;

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
                        if (clientAlias != null) {
                            System.out.println("Server: Client " + clientAlias + " has disconnected.");
                            synchronized (aliases) {
                                aliases.remove(clientAlias);
                            }
                        } else {
                            System.out.println("Server: A client has disconnected.");
                        }
                        break;
                    case "dir":
                        sendDir(writer);
                        break;
                    case "register":
                        String alias = reader.readLine();
                        String registrationResult = registerAlias(alias);
                        writer.println(registrationResult); // Send registration result
                        if ("1".equals(registrationResult)) {
                            clientAlias = alias;
                            System.out.println("Server: Client registered with alias: " + alias);
                        } else {
                            System.out.println("Server: Alias registration failed for: " + alias);
                        }
                        break;
                    case "store":
                        String filenameToStore = reader.readLine();
                        receiveFile(filenameToStore, input);
                        break;
                    case "get":
                        String filenameToGet = reader.readLine();
                        sendFile(filenameToGet, output);
                        break;
                    case "shutdown":
                        System.out.println("Server: Shutdown command received.");
                        System.exit(0);
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

    private String registerAlias(String alias) {
        synchronized (aliases) {
            if (!aliases.contains(alias)) {
                aliases.add(alias);
                return "1"; // Success
            } else {
                return "0"; // Failure
            }
        }
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

