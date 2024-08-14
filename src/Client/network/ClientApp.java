package Client.network;

import Client.model.Restaurant;
import Server.Models.Order;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;


public class ClientApp implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;
    private String serverAddress;
    private int port;
    private boolean running;
    // Queues for requests and responses
    private BlockingQueue<Map<String, Object>> requestQueue;
    private BlockingQueue<Map<String, Object>> responseQueue;


    public ClientApp(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.gson = new Gson();
        this.requestQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>(); // Initialize responseQueue

    }

    @Override
    public void run() {
        while (!running) {
            try {
                System.out.println("Attempting to connect to the server at " + serverAddress + ":" + port);
                clientSocket = new Socket(serverAddress, port);
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                running = true;
                System.out.println("Connected to the server at " + serverAddress + ":" + port);

                // Start processing requests
                while (running) {
                    Map<String, Object> request = requestQueue.take(); // Blocking call
                    Map<String, Object> response = sendRequest(request);
                    responseQueue.put(response); // Enqueue response
                }
            } catch (java.net.ConnectException e) {
                System.err.println("Connection refused. Retrying in 5 seconds...");
                try {
                    Thread.sleep(5000); // Wait for 5 seconds before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Retry attempt interrupted");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                closeConnection();
                break;
            }
        }
    }

    public void addRequest(Map<String, Object> request) {
        try {
            requestQueue.put(request); // Add request to the queue
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getResponse() {
        return responseQueue.poll(); // Retrieve and remove the head of the response queue
    }

    private Map<String, Object> sendRequest(Map<String, Object> request) throws Exception {
        try {
            // Attempt to establish a connection if necessary
            if (out == null || clientSocket == null || clientSocket.isClosed() || !clientSocket.isConnected()) {
                System.out.println("Output stream is not initialized or connection is closed. Attempting to establish a connection...");
                establishConnection();
            }

            if (out != null) {
                String jsonRequest = gson.toJson(request);
                System.out.println("Sending request: " + jsonRequest);
                out.println(jsonRequest);

                // Read the response from the server
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
                    Type type = new TypeToken<Map<String, Object>>() {}.getType();
                    return gson.fromJson(jsonResponse, type);
                } else {
                    System.out.println("Unexpected server response: " + jsonResponse);
                    return createErrorResponse("Unexpected server response: " + jsonResponse);
                }
            } else {
                System.out.println("Failed to initialize the output stream.");
                return createErrorResponse("Failed to establish connection.");
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            running = false;
            return createErrorResponse("Connection failed or dropped: " + e.getMessage());
        }
    }

    private void establishConnection() throws Exception {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
        clientSocket = new Socket(serverAddress, port);
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }

    public void closeConnection() {
        try {
            running = false; // Stop the client loop
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                System.out.println("Connection closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public Map<String, Object> placeOrder(String username, String password, String restaurantName, List<Order.Item> items, String customerNote, boolean useSavedCard, String creditCardNumber, String expirationDate, String cvv, boolean sendHome, String address) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "placeOrder");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("items", items);
        request.put("customerNote", customerNote);
        request.put("useSavedCard", String.valueOf(useSavedCard));
        request.put("creditCardNumber", creditCardNumber);
        request.put("expirationDate", expirationDate);
        request.put("cvv", cvv);
        request.put("sendHome", String.valueOf(sendHome));
        request.put("address", address);
        return sendRequest(request);
    }
    public void placeOrderAsync(String username, String password, String restaurantName, List<Map<String, Object>> items, String customerNote, boolean useSavedCard, String creditCardNumber, String expirationDate, String cvv, boolean sendHome, String address) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "placeOrder");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("items", items);
        request.put("customerNote", customerNote);
        request.put("useSavedCard", String.valueOf(useSavedCard));
        request.put("creditCardNumber", creditCardNumber);
        request.put("expirationDate", expirationDate);
        request.put("cvv", cvv);
        request.put("sendHome", String.valueOf(sendHome));
        request.put("address", address);
        addRequest(request);
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

    public void getOrdersHistoryAsync(String username, String password){
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getOrdersHistory");
        request.put("username", username);
        request.put("password", password);

        addRequest(request);
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
    // Method to search restaurants
    public void searchRestaurantsAsync(String username, String password, String cuisine, String distance) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getRestaurants");
        request.put("username", username);
        request.put("password", password);
        String distanceValue = distance.replace("km", "");
        request.put("distance", distanceValue);
        request.put("cuisine", cuisine);

        addRequest(request);

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

    public void getMenuAsync(String restaurantName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getMenu");
        request.put("restaurantName", restaurantName);

        addRequest(request);
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


    public Map<String, Object> signupDelivery(String username, String password, String address, String phoneNumber, String email, String token) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupDelivery");
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        request.put("token", token);

        return sendRequest(request);
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
            System.out.println(clientApp.sendRequest(jsonStringToMap(" {\"password\":\"e\",\"useSavedCard\":\"false\",\"cvv\":\"111\",\"sendHome\":\"true\",\"address\":\"\",\"restaurantName\":\"restaurant1\",\"creditCardNumber\":\"1111222233334444\",\"customerNote\":\"rrr\",\"type\":\"placeOrder\",\"items\":[{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item0.jpg\",\"price\":10.0,\"name\":\"Item0\",\"available\":true,\"description\":\"Description0\",\"quantity\":2},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item1.jpg\",\"price\":11.0,\"name\":\"Item1\",\"available\":true,\"description\":\"Description1\",\"quantity\":0},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item2.jpg\",\"price\":12.0,\"name\":\"Item2\",\"available\":true,\"description\":\"Description2\",\"quantity\":4},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item3.jpg\",\"price\":13.0,\"name\":\"Item3\",\"available\":true,\"description\":\"Description3\",\"quantity\":0},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item4.jpg\",\"price\":14.0,\"name\":\"Item4\",\"available\":true,\"description\":\"Description4\",\"quantity\":0},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item5.jpg\",\"price\":15.0,\"name\":\"Item5\",\"available\":true,\"description\":\"Description5\",\"quantity\":0},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item6.jpg\",\"price\":16.0,\"name\":\"Item6\",\"available\":true,\"description\":\"Description6\",\"quantity\":0},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item7.jpg\",\"price\":17.0,\"name\":\"Item7\",\"available\":true,\"description\":\"Description7\",\"quantity\":0},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item8.jpg\",\"price\":18.0,\"name\":\"Item8\",\"available\":true,\"description\":\"Description8\",\"quantity\":0},{\"photoUrl\":\"http://localhost:8080/menu_item_images/restaurant1_Item9.jpg\",\"price\":19.0,\"name\":\"Item9\",\"available\":true,\"description\":\"Description9\",\"quantity\":0}],\"username\":\"e\",\"expirationDate\":\"11/11\"}")));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Map<String,Object> jsonStringToMap(String jsonString) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(jsonString, type);
    }


    public void changeParameterAsync(String username, String password, String parameter, String newValue) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changeParameter");
        request.put("username", username);
        request.put("password", password);
        request.put("parameter", parameter);
        request.put("newValue", newValue);
        addRequest(request);
    }

    public void deleteAccountAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "deleteAccount");
        request.put("username", username);
        request.put("password", password);
        addRequest(request);
    }

    public void close() {
        closeConnection();
    }

    public void disableItemAsync(String username, String password, String itemName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "disableMenuItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName);
        addRequest(request);
    }

    public void enableItemAsync(String username, String password, String itemName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "enableMenuItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName);
        addRequest(request);
    }


    public void removeItemAsync(String username, String password, String itemName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "removeItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName);
        addRequest(request);
    }


    public void changeItemImageAsync(String username, String password, String itemName, File imageFile) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changeItemImage");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName);

        // Encode image to Base64
        if (imageFile != null && imageFile.exists()) {
            try {
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                request.put("image", encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            request.put("image", null);
        }

        addRequest(request);
    }

    public void addNewItemAsync(String username, String password, String itemName, double price, String description, File imageFile) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "addNewItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName);
        request.put("price", price);
        request.put("description", description);

        // Encode image to Base64
        if (imageFile != null && imageFile.exists()) {
            try {
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                request.put("image", encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            request.put("image", null);
        }

        addRequest(request);
    }

    public void requestIncomeDataAsync(String username, String password, Consumer<Double> callback) {
        // Create a request map to send to the server
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getIncomeData");
        request.put("username", username);
        request.put("password", password);

        // Send the request asynchronously
        new Thread(() -> {
            try {
                Map<String, Object> response = sendRequest(request);

                if ("true".equals(response.get("success"))) {
                    // Parse the income data from the response
                    double income = Double.parseDouble(response.get("income").toString());
                    // Pass the income data to the callback
                    callback.accept(income);
                } else {
                    // Handle the error scenario
                    String errorMessage = response.get("message").toString();
                    System.err.println("Failed to get income data: " + errorMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void searchDeliveriesAsync(String username, String password, String address, String distance) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getDeliveryOrders");
        request.put("username", username);
        request.put("password", password);
        request.put("distance", distance);
        request.put("address", address);

        addRequest(request);
    }

    public void markDeliveryCompleteAsync(String username, String password, int deliveryId) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "markDeliveryComplete");
        request.put("username", username);
        request.put("password", password);
        request.put("deliveryId", deliveryId);

        addRequest(request);
    }
}

