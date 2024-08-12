package Client;

import Server.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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
            Map<String, Object> messageMap = gson.fromJson(serverMessage, Map.class);
            String messageType = (String) messageMap.get("type");

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

    private Map<String, Object> sendRequest(Map<String, Object> request) throws Exception {
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
            StringBuilder jsonResponseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                jsonResponseBuilder.append(line);
                if (line.endsWith("}")) { // Simple check for end of JSON object
                    break;
                }
            }
            String jsonResponse = jsonResponseBuilder.toString();
            System.out.println("Received response: " + jsonResponse);

            if (jsonResponse != null && jsonResponse.startsWith("{")) {
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                return gson.fromJson(jsonResponse, type);
            } else {
                System.out.println("Unexpected server response: " + jsonResponse);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Unexpected server response: " + jsonResponse);
                return errorResponse;
            }
        } else {
            System.out.println("Failed to initialize the output stream.");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to establish connection.");
            return errorResponse;
        }
    }

    // Method to login a user
    public Map<String, Object> login(String username, String password) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "login");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    // Method to sign up a customer
    public Map<String, Object> signupCustomer(String username, String password, String address, String phoneNumber, String email) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupCustomer");
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        return sendRequest(request);
    }

    // Method to sign up a restaurant
    public Map<String, Object> signupRestaurant(String username, String password, String address, String phoneNumber, String email, String businessPhoneNumber, String cuisine) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupRestaurant");
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        request.put("businessPhoneNumber", businessPhoneNumber);
        request.put("cuisine", cuisine);
        return sendRequest(request);
    }

    // Method to place an order
    public Map<String, Object> placeOrder(String username, String password, String restaurantName, String items, String customerNote, String creditCardNumber, String expirationDate, String cvv) throws Exception {
        Map<String, Object> request = new HashMap<>();
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

    // Method to update the restaurant menu with an image sent as Base64 encoded string
    public Map<String, Object> updateMenu(String username, String password, String restaurantName, String itemName, double price, String description, File imageFile, String action) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateMenu");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("itemName", itemName);
        request.put("price", String.valueOf(price));
        request.put("description", description);
        request.put("action", action);

        // Encode image to Base64
        if (imageFile != null && imageFile.exists()) {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
            request.put("image", encodedImage);
        } else {
            request.put("image", null);
        }

        return sendRequest(request);
    }


    // Method to update a customer's credit card information
    public Map<String, Object> updateCreditCard(String username, String password, String creditCardNumber, String expirationDate, String cvv) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateCreditCard");
        request.put("username", username);
        request.put("password", password);
        request.put("creditCardNumber", creditCardNumber);
        request.put("expirationDate", expirationDate);
        request.put("cvv", cvv);
        return sendRequest(request);
    }

    // Method to get a customer's or restaurant's order history
    public List<Order> getOrdersHistory(String username, String password) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getOrdersHistory");
        request.put("username", username);
        request.put("password", password);

        Map<String, Object> response = sendRequest(request);
        if ("true".equals(response.get("success"))) {
            // Parse the JSON array of orders from the response
            String ordersJson = (String) response.get("message");
            Type orderListType = new TypeToken<List<Order>>(){}.getType();
            return gson.fromJson(ordersJson, orderListType);
        } else {
            throw new Exception("Failed to retrieve order history: " + response.get("message"));
        }
    }

    // Method to search restaurants
    public List<Restaurant> searchRestaurants(String username, String password, String cuisine, String distance) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getRestaurants");
        request.put("username", username);
        request.put("password", password);
        String distanceValue = distance.replace("km", "");
        request.put("distance", distanceValue);
        request.put("cuisine", cuisine);

        Map<String, Object> response = sendRequest(request);
        if ("true".equals(response.get("success"))) {
            Type restaurantListType = new TypeToken<List<Restaurant>>(){}.getType();
            return gson.fromJson((String) response.get("message"), restaurantListType);
        } else {
            throw new Exception("Failed to search restaurants: " + response.get("message"));
        }
    }


    // Method to get menu of a restaurant with item images
    public List<Map<String, Object>> getMenu(String restaurantName) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getMenu");
        request.put("restaurantName", restaurantName);

        Map<String, Object> response = sendRequest(request);
        if ("true".equals(response.get("success"))) {
            Type menuListType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            return gson.fromJson((String) response.get("message"), menuListType);
        } else {
            throw new Exception("Failed to fetch menu: " + response.get("message"));
        }
    }

    // Method to mark an order as complete
    public Map<String, Object> markOrderComplete(String username, String password, int orderId) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "markOrderComplete");
        request.put("username", username);
        request.put("password", password);
        request.put("orderId", String.valueOf(orderId));
        return sendRequest(request);
    }

    // Method to disconnect a user
    public Map<String, Object> disconnect(String username, String password) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "disconnect");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    // Method to get available cuisines
    public Map<String, Object> getAvailableCuisines() throws Exception {
        Map<String, Object> request = new HashMap<>();
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

    // Change Password Method
    public Map<String, Object> changePassword(String username, String oldPassword, String newPassword) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changePassword");
        request.put("username", username);
        request.put("oldPassword", oldPassword);
        request.put("newPassword", newPassword);
        return sendRequest(request);
    }

    // Change Email Method
    public Map<String, Object> changeEmail(String username, String password, String newEmail) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changeEmail");
        request.put("username", username);
        request.put("password", password);
        request.put("newEmail", newEmail);
        return sendRequest(request);
    }

    // Update Profile Picture Method
    public Map<String, Object> updateProfilePicture(String username, String password, File profilePicture) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "uploadProfilePicture");
        request.put("username", username);
        request.put("password", password);

        // Encode the profile picture to Base64
        if (profilePicture.exists()) {
            byte[] imageBytes = Files.readAllBytes(profilePicture.toPath());
            String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
            request.put("profilePicture", encodedImage); // Add the Base64 image to the request
        } else {
            throw new FileNotFoundException("Profile picture file not found: " + profilePicture.getAbsolutePath());
        }

        // Send the request and receive the response
        return sendRequest(request);
    }


    // Get Menu Method
    public List<Order.Item> getMenu(String username, String password, String restaurantName) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getMenu");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);

        Map<String, Object> response = sendRequest(request);
        if ("true".equals(response.get("success"))) {
            // Parse the JSON array of items from the response
            String itemsJson = (String) response.get("message");
            Type itemListType = new TypeToken<List<Order.Item>>() {}.getType();
            return gson.fromJson(itemsJson, itemListType);
        } else {
            throw new Exception("Failed to retrieve menu: " + response.get("message"));
        }
    }

    public static void main(String[] args) {
        try {
            // Create an instance of ClientApp
            ClientApp clientApp = new ClientApp("localhost", 12345);

            // Assuming you have already registered these restaurants and have their credentials
            String[] usernames = {
                    "restaurant1", "restaurant2", "restaurant3",
                    "restaurant4", "restaurant5", "restaurant6",
                    "restaurant7", "restaurant8", "restaurant9", "restaurant10"
            };
            String[] passwords = {
                    "password1", "password2", "password3",
                    "password4", "password5", "password6",
                    "password7", "password8", "password9", "password10"
            };

            // Loop through the credentials and log in to each restaurant
            for (int i = 0; i < usernames.length; i++) {
                String username = usernames[i];
                String password = passwords[i];

                // Log in to the restaurant account
                Map<String, Object> loginResponse = clientApp.login(username, password);
                for(int j = 0; j < 10; j++) {
                    clientApp.updateMenu(username, password, username, "Item" + j, 10.0 + j, "Description" + j, new File("src/Client/menu_item_25.png"), "add");
                }
                System.out.println("Login Response for " + username + ": " + loginResponse);
            }

            System.out.println("All restaurants logged in successfully.");
            List<Restaurant> restaurants = clientApp.searchRestaurants("e", "e", "All", "30km");
            System.out.println("Restaurants: " + restaurants);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

