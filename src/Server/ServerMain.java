package Server;

import Server.App.ClientHandler;
import Server.App.ServerApp;
import Server.Models.Order;
import Server.Utilities.ImageServer;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Server.App.ServerApp.*;

public class ServerMain {
    public static String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 12345;
    public static final int IMAGE_SERVER_PORT = 8080;

    public static final int THREAD_POOL_SIZE = 10;
    public static void main(String[] args) {
        // Get the local IP address of the server
        SERVER_IP = getLocalIpAddress();
        try {
            // Start the image server in a new thread
            new Thread(() -> {
                try {
                    ImageServer.startServer(IMAGE_SERVER_PORT); // Start the image server
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Create the files if they don't exist
            String[] directories = {"menu_data", "menu_item_images", "profile_pictures", "server_logs", "server_logs/users"};
            for (String directory : directories) {
                File dir = new File(directory);
                if (!dir.exists()) {
                    dir.mkdir();
                }
            }

            loadUsersFromJSON();
            loadMenusFromJSON();


            // Set up server socket
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is listening on port " + SERVER_PORT);

            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


            // Server loop to handle clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                clientSocket.setSoTimeout(30000); // Set a timeout of 30 seconds for the client socket

                // Create a new thread to handle the client using ClientHandler
                executorService.execute(new ClientHandler(clientSocket));
                ServerApp.cleanUpLoggedInRestaurants();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Ignore loopback and non-up interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // Return only IPv4 addresses, skip link-local or site-local addresses
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();  // Return the actual IP address
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";  // Fallback to loopback address if no other IP is found
    }

}
