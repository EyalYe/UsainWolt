package Client.network;

import Client.model.Restaurant;
import Server.Models.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.Timer;
import java.util.concurrent.TimeUnit;

import static Server.Utilities.CustomDateAdapter.gsonCreator;


public class ClientApp implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;
    private String serverAddress;
    private int port;
    private boolean running;
    private BlockingQueue<Map<String, Object>> requestQueue;
    private BlockingQueue<Map<String, Object>> responseQueue;

    public ClientApp(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.gson = gsonCreator();
        this.requestQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while (true) {
            connectToServer();
        }
    }

    void connectToServer(){
        try {
            System.out.println("Attempting to connect to the server at " + serverAddress + ":" + port);
            clientSocket = new Socket(serverAddress, port);
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            running = true;
            System.out.println("Connected to the server at " + serverAddress + ":" + port);

            // Start the thread that listens for responses from the server
            startListeningForMessages();

            // Process requests (send them with a timeout)
            while (running) {
                Map<String, Object> request = requestQueue.poll(1, TimeUnit.SECONDS);
                if (request != null) {
                    sendRequest(request); // Just send request, no reading of response here
                }

            }
        } catch (java.net.ConnectException e) {
            System.err.println("Connection refused. Retrying in 5 seconds...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    private void startListeningForMessages() {
        // Create a new thread for listening to server messages
        Thread messageListener = new Thread(() -> {
            while (running) {
                try {
                    // Use blocking readLine(), which waits for input without busy-waiting
                    String message = in.readLine();
                    if (message != null) {
                        handleServerMessage(message); // Process the received message
                    } else {
                        // If message is null, it could mean the server closed the connection
                        System.err.println("Server closed connection.");
                        closeConnection();
                        break;
                    }
                } catch (IOException e) {
                    System.err.println("Error reading server message: " + e.getMessage());
                    closeConnection();
                    break;
                }
            }
        });
        messageListener.start();
    }

    public void addRequest(Map<String, Object> request) {
        if(clientSocket == null || clientSocket.isClosed()){
            System.out.println("Connection closed.");
            running = false;
        }
        try {
            requestQueue.put(request); // Add request to the queue
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getResponse() {
        return responseQueue.poll(); // Retrieve and remove the head of the response queue
    }

    private void sendRequest(Map<String, Object> request) {
        try {
            if (out != null && request != null) {
                String jsonRequest = gson.toJson(request);
                System.out.println("Sending request: " + jsonRequest);
                out.println(jsonRequest);  // Send request, do not wait for response here
            }
        } catch (Exception e) {
            System.out.println("Exception occurred while sending request: " + e.getMessage());
            running = false;
        }
    }

    private void handleServerMessage(String message) {
        try {
            // Process the message here, assuming it's JSON
            Map<String, Object> response = getFormattedResponse(message);
            if (response != null) {
                responseQueue.put(response); // Enqueue response
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getFormattedResponse(String response) throws IOException {
        if (response != null && response.startsWith("{")) {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            return gson.fromJson(response, type);
        } else {
            System.out.println("Unexpected server response: " + response);
            return createErrorResponse("Unexpected server response: " + response);
        }
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


    public void getOrdersHistoryAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getOrdersHistory");
        request.put("username", username);
        request.put("password", password);

        addRequest(request);
    }

    // Method to search restaurants
    public void searchRestaurantsAsync(String username, String password, String cuisine, String distance, boolean sendHome, String address) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getRestaurants");
        request.put("username", username);
        request.put("password", password);
        String distanceValue = distance.replace("km", "");
        request.put("distance", distanceValue);
        request.put("cuisine", cuisine);
        request.put("sendHome", String.valueOf(sendHome));
        request.put("address", address);

        addRequest(request);
    }

    public void getMenuAsync(String restaurantName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getMenu");
        request.put("restaurantName", restaurantName);

        addRequest(request);
    }

    public static Map<String, Object> jsonStringToMap(String jsonString) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Gson gson = gsonCreator();
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

    public void requestIncomeDataAsync(String username, String password) {
        // Create a request map to send to the server
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getIncomeData");
        request.put("username", username);
        request.put("password", password);
        addRequest(request);
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

    public void checkIfOnDeliveryAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "checkIfOnDelivery");
        request.put("username", username);
        request.put("password", password);

        addRequest(request);
    }

    public void updateCreditCardAsync(String username, String password, String creditCardNumber, String expirationDate, String cvv) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateCreditCard");
        request.put("username", username);
        request.put("password", password);
        request.put("creditCardNumber", creditCardNumber);
        request.put("expirationDate", expirationDate);
        request.put("cvv", cvv);

        addRequest(request);
    }

    public void disconnect(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "disconnect");
        request.put("username", username);
        request.put("password", password);

        addRequest(request);
    }

    public void getAvailableCuisinesAsync() {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        addRequest(request);
    }

    public Map<String, Object> loginAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "login");
        request.put("username", username);
        request.put("password", password);
        addRequest(request);
        return getResponse();
    }

    public Map<String, Object> signupCustomerAsync(String customerUsername, String customerPassword, String address, String phoneNumber, String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupCustomer");
        request.put("username", customerUsername);
        request.put("password", customerPassword);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        addRequest(request);
        return getResponse();
    }

    public Map<String, Object> signupRestaurantAsync(String restaurantUsername, String restaurantPassword, String address, String phoneNumber, String email, String businessPhoneNumber, String cuisine) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupRestaurant");
        request.put("username", restaurantUsername);
        request.put("password", restaurantPassword);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        request.put("businessPhoneNumber", businessPhoneNumber);
        request.put("cuisine", cuisine);
        addRequest(request);
        return getResponse();
    }

    public Map<String, Object> updateMenuAsync(String restaurantUsername, String restaurantPassword, String fictionalName, String itemName, double price, String description, File itemImage, String add) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateMenu");
        request.put("username", restaurantUsername);
        request.put("password", restaurantPassword);
        request.put("restaurantName", fictionalName);
        request.put("menuItemName", itemName);
        request.put("price", price);
        request.put("description", description);
        request.put("add", add);

        // Encode image to Base64
        if (itemImage != null && itemImage.exists()) {
            try {
                byte[] imageBytes = Files.readAllBytes(itemImage.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                request.put("image", encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            request.put("image", null);
        }

        addRequest(request);
        return getResponse();
    }

    public Map<String, Object> updateProfilePictureAsync(String restaurantUsername, String restaurantPassword, File profilePicture) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateProfilePicture");
        request.put("username", restaurantUsername);
        request.put("password", restaurantPassword);

        // Encode image to Base64
        if (profilePicture != null && profilePicture.exists()) {
            try {
                byte[] imageBytes = Files.readAllBytes(profilePicture.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                request.put("image", encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            request.put("image", null);
        }

        addRequest(request);
        return getResponse();
    }

    public Map<String, Object> signupDeliveryAsync(String Username, String Password, String address, String phoneNumber, String email, String Token) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupDelivery");
        request.put("username", Username);
        request.put("password", Password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        request.put("token", Token);
        addRequest(request);
        return getResponse();
    }
}


