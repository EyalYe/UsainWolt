package Server;

import Server.App.ClientHandler;
import Server.Utilities.ImageServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static Server.App.ServerApp.*;

public class ServerMain {
    public static void main(String[] args) {
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
            createFileIfNotExists(USERS_FILE);
            String[] directories = {"restaurant_orders", "customer_orders", "delivery_orders", "menu_data", "menu_item_images", "profile_pictures"};
            for (String directory : directories) {
                File dir = new File(directory);
                if (!dir.exists()) {
                    dir.mkdir();
                }
            }

            // Load data from CSV files
            loadUsersFromCSV();
            loadMenusFromCSV();

            // Set up server socket
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is listening on port " + SERVER_PORT);

            // Server loop to handle clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // Create a new thread to handle the client using ClientHandler
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
