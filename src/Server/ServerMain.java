// Group: 6
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

/**
 * ServerMain is the entry point for the server application.
 * It initializes and starts the server, sets up necessary directories,
 * handles incoming client connections, and runs an image server.
 */
public class ServerMain {
    public static String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 12345;
    public static final int IMAGE_SERVER_PORT = 8080;
    public static final int THREAD_POOL_SIZE = 30;
    public static final boolean RUNNING_ON_SERVER = false;
    public static final String IMAGE_URL = RUNNING_ON_SERVER ? "images.usainwolt.xyz" : SERVER_IP + ":" +   IMAGE_SERVER_PORT;

    /**
     * The main method starts the server application.
     * It sets up the server IP, starts the image server, initializes directories,
     * loads data from JSON files, and begins listening for client connections.
     */
    public static void main(String[] args) {
        // Get the local IP address of the server
        if (RUNNING_ON_SERVER)
            SERVER_IP = getLocalIpAddress();
        System.out.println("Server IP: " + SERVER_IP);
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

            // Load users and menus data from JSON files
            loadUsersFromJSON();
            loadMenusFromJSON();


            // Set up server socket to listen for incoming client connections
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is listening on port " + SERVER_PORT);

            // Initialize a thread pool for handling client connections
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


            // Main server loop to handle incoming client connections
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

    /**
     * Retrieves the local IP address of the server.
     * It filters out loopback, down, and virtual interfaces, and returns an IPv4 address in the private range.
     *
     * return A string representing the local IP address, or "127.0.0.1" if none is found.
     */
    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Skip loopback, down interfaces, and virtual interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // Return only IPv4 addresses and ensure it's in the private range
                    if (inetAddress instanceof InetAddress && inetAddress.isSiteLocalAddress()) {
                        String hostAddress = inetAddress.getHostAddress();

                        // Filter to return only addresses in the 192.168.x.x range
                        if (hostAddress.startsWith("192.168.")) {
                            return hostAddress;  // Return the desired IP
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";  // Fallback to loopback address if no other IP is found
    }

}
