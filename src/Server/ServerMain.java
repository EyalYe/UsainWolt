package Server;

import Server.App.ClientHandler;
import Server.Models.Order;
import Server.Utilities.ImageServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

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
