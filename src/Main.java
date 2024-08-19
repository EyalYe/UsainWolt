import Client.network.ClientApp;
import Server.ServerMain;
import Client.UsainWoltMain;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            ServerMain.main(args);
        });
        serverThread.start();
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                UsainWoltMain.skipLogin("restaurant" + (new Random().nextInt(25) + 1), "password", "restaurant");
            });
            thread.start();
        }
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                UsainWoltMain.skipLogin("customer" + (new Random().nextInt(25) + 1), "password", "customer");
            });
            thread.start();
        }
        try {
            // Create an instance of ClientApp
            ClientApp clientApp = new ClientApp("localhost", 12345);

            // 25 restaurant usernames, all with the same password ("password")
            for (int i = 0; i < 25; i++) {
                String restaurantUsername = "restaurant" + (i + 1);
                String restaurantPassword = "password";  // Consistent password

                // Log in to the restaurant account
                Map<String, Object> loginResponse = clientApp.login(restaurantUsername, restaurantPassword);
                System.out.println("Login Response for " + restaurantUsername + ": " + loginResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
