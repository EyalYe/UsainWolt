package Client;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientApp implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;
    private String serverAddress;
    private int port;
    private boolean running;

    public ClientApp(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            // Attempt to establish a connection to the server
            System.out.println("Attempting to connect to the server at " + serverAddress + ":" + port);
            clientSocket = new Socket(serverAddress, port);
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            running = true;

            System.out.println("Connected to the server at " + serverAddress + ":" + port);

            // Main loop to handle incoming messages from the server
            while (running) {
                String serverMessage = in.readLine();
                if (serverMessage != null) {
                    handleServerMessage(serverMessage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }


    // Handle messages received from the server
    private void handleServerMessage(String serverMessage) {
        try {
            // Assume the message is in JSON format
            Map<String, String> messageMap = gson.fromJson(serverMessage, Map.class);
            String messageType = messageMap.get("type");

            switch (messageType) {
                case "notification":
                    // Example: Display a notification to the user
                    System.out.println("Notification from server: " + messageMap.get("content"));
                    break;
                case "update":
                    // Example: Update local data based on the server's message
                    System.out.println("Update from server: " + messageMap.get("content"));
                    break;
                default:
                    // Handle unknown or unexpected messages
                    System.out.println("Unknown message type received from server: " + serverMessage);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Failed to handle server message: " + serverMessage);
            e.printStackTrace();
        }
    }

    private Map<String, String> sendRequest(Map<String, String> request) throws Exception {
        if (out == null) {
            System.out.println("Output stream is not initialized. Attempting to establish a connection...");
            // Try to re-establish the connection if out is not initialized
            if (clientSocket == null || clientSocket.isClosed()) {
                clientSocket = new Socket(serverAddress, port);
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            }
        }

        if (out != null) {
            String jsonRequest = gson.toJson(request);
            System.out.println("Sending request: " + jsonRequest);
            out.println(jsonRequest);
            String jsonResponse = in.readLine();
            System.out.println("Received response: " + jsonResponse);

            if (jsonResponse != null && jsonResponse.startsWith("{")) {
                return gson.fromJson(jsonResponse, Map.class);
            } else {
                System.out.println("Unexpected server response: " + jsonResponse);
                return Map.of("success", "false", "message", "Unexpected server response: " + jsonResponse);
            }
        } else {
            System.out.println("Failed to initialize the output stream.");
            return Map.of("success", "false", "message", "Failed to establish connection.");
        }
    }


    // Method to login a user
    public Map<String, String> login(String username, String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "login");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    // Method to sign up a customer
    public Map<String, String> signupCustomer(String username, String password, String address, String phoneNumber, String email) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "signupCustomer");
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        return sendRequest(request);
    }

    // Method to sign up a restaurant
    public Map<String, String> signupRestaurant(String username, String password, String address, String phoneNumber, String email, String businessPhoneNumber) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "signupRestaurant");
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        request.put("businessPhoneNumber", businessPhoneNumber);
        return sendRequest(request);
    }

    // Method to place an order
    public Map<String, String> placeOrder(String username, String password, String restaurantName, String items, String customerNote, String creditCardNumber, String expirationDate, String cvv) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "placeOrder");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("items", items);
        request.put("customerNote", customerNote);
        request.put("creditCardNumber", creditCardNumber);
        request.put("expirationDate", expirationDate);
        request.put("cvv", cvv);
        return sendRequest(request);
    }

    // Method to update the restaurant menu
    public Map<String, String> updateMenu(String username, String password, String restaurantName, String itemName, double price) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "updateMenu");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("itemName", itemName);
        request.put("price", String.valueOf(price));
        return sendRequest(request);
    }

    // Method to update a customer's credit card information
    public Map<String, String> updateCreditCard(String username, String password, String creditCardNumber, String expirationDate, String cvv) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "updateCreditCard");
        request.put("username", username);
        request.put("password", password);
        request.put("creditCardNumber", creditCardNumber);
        request.put("expirationDate", expirationDate);
        request.put("cvv", cvv);
        return sendRequest(request);
    }

    // Method to get a customer's or restaurant's order history
    public Map<String, String> getOrdersHistory(String username, String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "getOrdersHistory");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    // Method to mark an order as complete
    public Map<String, String> markOrderComplete(String username, String password, int orderId) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "markOrderComplete");
        request.put("username", username);
        request.put("password", password);
        request.put("orderId", String.valueOf(orderId));
        return sendRequest(request);
    }

    // Method to disconnect a user
    public Map<String, String> disconnect(String username, String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "disconnect");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    // Method to get available cuisines
    public Map<String, String> getAvailableCuisines() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        return sendRequest(request);
    }

    // Close the connection to the server
    public void closeConnection() {
        try {
            running = false;  // Stop the client loop
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Connection closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ClientApp clientApp = new ClientApp("localhost", 8080);
            Thread clientThread = new Thread(clientApp);
            clientThread.start();

            // Example of logging in a user
            Map<String, String> response = clientApp.login("john_doe", "password123");
            System.out.println("Login Response: " + response);

            clientApp.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
