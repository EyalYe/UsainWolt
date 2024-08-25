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


/**
 * ClientApp manages communication between the client and the server.
 * It handles establishing a connection, sending requests, receiving responses, and managing reconnections.
 */
public class ClientApp implements Runnable {
    private Socket clientSocket; // Socket for client-server communication
    private BufferedReader in; // Input stream reader for receiving data from the server
    private PrintWriter out; // Output stream writer for sending data to the server
    private Gson gson; // Gson instance for JSON serialization/deserialization
    private String serverAddress; // Server address to connect to
    private int port; // Server port to connect to
    private boolean running; // Flag to keep the client running
    private BlockingQueue<Map<String, Object>> requestQueue; // Queue for outgoing requests
    private BlockingQueue<Map<String, Object>> responseQueue; // Queue for incoming responses
    private boolean isRestaurant; // Flag indicating if the client is a restaurant
    private Map<String,Object> restaurantReconnectRequests = new HashMap<>(); // Stores requests needed to reconnect for restaurants
    private Map<String,Object> latestRequest = new HashMap<>(); // Stores the latest request if connection fails
    private boolean firstTime = true; // Flag indicating if it's the first connection attempt
    private boolean restartConnection = false; // Flag to restart connection if needed

    // Constructor to initialize the ClientApp with server address and port
    public ClientApp(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.gson = gsonCreator();
        this.requestQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();
        this.running = true;
        isRestaurant = false;
        firstTime = true;
    }


    // The main run method of the ClientApp, responsible for connecting to the server and handling reconnections
    @Override
    public void run() {
        do {
            connectToServer();
            firstTime = false;
            System.out.println("First time: " + firstTime);
        } while (isRestaurant);
        while (true) {
            while (!restartConnection) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
            restartConnection = false;
            connectToServer();
        }
    }


    // Attempts to connect to the server and initializes necessary streams for communication
    void connectToServer(){
        try {
            System.out.println("Attempting to connect to the server at " + serverAddress + ":" + port);
            clientSocket = new Socket(serverAddress, port);
            out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            running = true;
            System.out.println("Connected to the server at " + serverAddress + ":" + port);

            if(isRestaurant && !firstTime && !restaurantReconnectRequests.isEmpty()){
                requestQueue.put(restaurantReconnectRequests);
            }
            if (!latestRequest.isEmpty()){
                requestQueue.put(latestRequest);
                latestRequest = new HashMap<>();
            }

            // Start a new thread to listen for responses from the server
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

    // Starts a new thread to listen for messages from the server
    private void startListeningForMessages() {
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

    // Adds a request to the request queue for sending to the server
    public void addRequest(Map<String, Object> request) {
        if(clientSocket == null || clientSocket.isClosed()){
            System.out.println("Connection closed.");
            running = false;
            latestRequest = request; // Store the latest request for retry
            restartConnection = true; // Indicate the need to restart the connection
            return;
        }
        try {
            requestQueue.put(request); // Add request to the queue
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Retrieves the next response from the response queue
    public Map<String, Object> getResponse() {
        return responseQueue.poll(); // Retrieve and remove the head of the response queue
    }

    // Sends a request to the server by converting it to JSON format
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

    // Handles incoming messages from the server by parsing them into a map format
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

    // Parses a JSON string response into a map format
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

    // Creates an error response map
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }

    // Closes the connection to the server and stops the client
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

    // Sets the client as a restaurant with the provided credentials
    public void setRestaurant(boolean restaurant, String username, String password){
        isRestaurant = restaurant;
        if(restaurant){
            restaurantReconnectRequests.put("username", username);
            restaurantReconnectRequests.put("password", password);
            restaurantReconnectRequests.put("type", "login");
        }
    }

    // Method to place an order
    public void placeOrderAsync(String username, String password, String restaurantName, List<Map<String, Object>> items, String customerNote, boolean useSavedCard, String creditCardNumber, String expirationDate, String cvv, boolean sendHome, String address) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "placeOrder");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("items", items);  // List of items being ordered
        request.put("customerNote", customerNote);  // Any notes from the customer
        request.put("useSavedCard", String.valueOf(useSavedCard));  // Boolean flag for saved card usage
        request.put("creditCardNumber", creditCardNumber);  // Credit card number for payment
        request.put("expirationDate", expirationDate);  // Expiry date of the credit card
        request.put("cvv", cvv);  // CVV number of the credit card
        request.put("sendHome", String.valueOf(sendHome));  // Boolean flag if delivery is to home address
        request.put("address", address);  // Delivery address
        addRequest(request);  // Adds the request to the queue for processing
    }

    // Method to fetch the order history for a user
    public void getOrdersHistoryAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getOrdersHistory");
        request.put("username", username);
        request.put("password", password);

        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to search for restaurants
    public void searchRestaurantsAsync(String username, String password, String cuisine, String distance, boolean sendHome, String address) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getRestaurants");
        request.put("username", username);
        request.put("password", password);
        String distanceValue = distance.replace("km", "");  // Extracts numeric distance
        request.put("distance", distanceValue);  // Sets the distance for search
        request.put("cuisine", cuisine);  // Specifies cuisine type for search
        request.put("sendHome", String.valueOf(sendHome));  // Boolean flag for home delivery option
        request.put("address", address);  // Address to filter nearby restaurants

        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to fetch the menu of a specific restaurant
    public void getMenuAsync(String restaurantName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getMenu");
        request.put("restaurantName", restaurantName); // Specifies which restaurant's menu to fetch

        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to convert JSON string to a Map
    public static Map<String, Object> jsonStringToMap(String jsonString) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Gson gson = gsonCreator();
        return gson.fromJson(jsonString, type); // Converts JSON string to Map using Gson
    }

    // Method to change a user parameter
    public void changeParameterAsync(String username, String password, String parameter, String newValue) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changeParameter");
        request.put("username", username);
        request.put("password", password);
        request.put("parameter", parameter); // Specifies which parameter to change
        request.put("newValue", newValue); // New value for the specified paramete
        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to delete a user account
    public void deleteAccountAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "deleteAccount");
        request.put("username", username);
        request.put("password", password);
        addRequest(request); // Adds the request to the queue for processing
    }

    // Closes the connection with the server
    public void close() {
        closeConnection();
    }

    // Method to disable a menu item for a restaurant
    public void disableItemAsync(String username, String password, String itemName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "disableMenuItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName); // Menu item to be disabled
        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to enable a previously disabled menu item
    public void enableItemAsync(String username, String password, String itemName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "enableMenuItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName); // Menu item to be enabled
        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to remove a menu item from a restaurant
    public void removeItemAsync(String username, String password, String itemName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "removeItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName); // Menu item to be removed
        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to change the image of a menu item
    public void changeItemImageAsync(String username, String password, String itemName, File imageFile) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changeItemImage");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName); // Menu item whose image is to be changed

        // Encode image to Base64
        if (imageFile != null && imageFile.exists()) {
            try {
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                request.put("image", encodedImage); // Add encoded image to request
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            request.put("image", null); // If no image file, set image to null
        }

        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to add a new item to a restaurant's menu
    public void addNewItemAsync(String username, String password, String itemName, double price, String description, File imageFile) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "addNewItem");
        request.put("username", username);
        request.put("password", password);
        request.put("menuItemName", itemName); // Name of the new menu item
        request.put("price", price); // Price of the new menu item
        request.put("description", description); // Description of the new menu item

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

        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to request income data for a restaurant
    public void requestIncomeDataAsync(String username, String password) {
        // Create a request map to send to the server
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getIncomeData");
        request.put("username", username);
        request.put("password", password);
        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to search for delivery orders
    public void searchDeliveriesAsync(String username, String password, String address, String distance) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getDeliveryOrders");
        request.put("username", username);
        request.put("password", password);
        request.put("distance", distance); // Search radius for delivery orders
        request.put("address", address); // Address to filter nearby deliveries


        addRequest(request); // Adds the request to the queue for processing
    }

    // Method to mark a delivery as complete
    public void markDeliveryCompleteAsync(String username, String password, int deliveryId) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "markDeliveryComplete");
        request.put("username", username);
        request.put("password", password);
        request.put("deliveryId", deliveryId); // ID of the delivery to be marked as complete

        addRequest(request);
    }

    // Method to asynchronously check if the user is currently on a delivery
    public void checkIfOnDeliveryAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "checkIfOnDelivery");
        request.put("username", username);
        request.put("password", password);

        addRequest(request);
    }

    // Method to update the user's credit card information
    public void updateCreditCardAsync(String username, String password, String creditCardNumber, String expirationDate, String cvv) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateCreditCard");
        request.put("username", username);
        request.put("password", password);
        request.put("creditCardNumber", creditCardNumber); // New credit card number
        request.put("expirationDate", expirationDate); // Expiration date of the credit card
        request.put("cvv", cvv); // CVV of the credit card

        addRequest(request);
    }

    // Method to disconnect a user session
    public void disconnect(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "disconnect");
        request.put("username", username);
        request.put("password", password);

        addRequest(request);
    }

    // Method to fetch the available cuisines
    public void getAvailableCuisinesAsync() {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        addRequest(request);
    }

    // Method to login a user and get the response
    public Map<String, Object> loginAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "login");
        request.put("username", username);
        request.put("password", password);
        addRequest(request);
        return getResponse(); // Waits for and returns the server response
    }

    // Method to sign up a new customer and get the response
    public Map<String, Object> signupCustomerAsync(String customerUsername, String customerPassword, String address, String phoneNumber, String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupCustomer");
        request.put("username", customerUsername);
        request.put("password", customerPassword);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        addRequest(request);
        return getResponse(); // Waits for and returns the server response
    }

    // Method to sign up a new restaurant and get the response
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
        return getResponse(); // Waits for and returns the server response
    }


    // Method to update a restaurant's menu and get the response
    public Map<String, Object> updateMenuAsync(String restaurantUsername, String restaurantPassword, String fictionalName, String itemName, double price, String description, File itemImage, String add) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateMenu");
        request.put("username", restaurantUsername);
        request.put("password", restaurantPassword);
        request.put("restaurantName", fictionalName);
        request.put("menuItemName", itemName);
        request.put("price", price);
        request.put("description", description);
        request.put("add", add); // Indicates if item is to be added or removed

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
        return getResponse(); // Waits for and returns the server response
    }

    // Method to update a restaurant's profile picture and get the response
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
        return getResponse(); // Waits for and returns the server response
    }

    // Method to sign up a new delivery person and get the response
    public Map<String, Object> signupDeliveryAsync(String Username, String Password, String address, String phoneNumber, String email, String Token) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "signupDelivery");
        request.put("username", Username);
        request.put("password", Password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        request.put("token", Token); // Token associated with the delivery person
        addRequest(request);
        return getResponse(); // Waits for and returns the server response
    }

    // Method to update a menu item
    public void updateMenuItemAsync(String username, String password, String itemName, double itemPrice, String itemDescription, boolean isAvailable, String encodedImage, String action) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateMenu");
        request.put("username", username);
        request.put("password", password);
        request.put("itemName", itemName);
        request.put("price", String.valueOf(itemPrice));  // Convert Double to String
        request.put("description", itemDescription != null ? itemDescription : "");  // Handle null description
        request.put("isAvailable", String.valueOf(isAvailable));  // Convert boolean to String
        request.put("image", encodedImage != null ? encodedImage : "");  // Handle null image
        request.put("action", action);  // Specify action as add

        addRequest(request);
    }

    // Method to remove a menu item
    public void removeMenuItemAsync(String username, String password, String restaurantName, String itemName) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "updateMenu");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("itemName", itemName);
        request.put("action", "remove");  // Specify action as remove

        addRequest(request);
    }

    // Method to mark an order as ready for pickup
    public void markOrderAsReadyAsync(String username, String password, Order order) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "markOrderReadyForPickup");
        request.put("username", username);
        request.put("password", password);
        request.put("order" , gson.toJson(order)); // Converts the order object to JSON format
        addRequest(request);
    }

    // Method to fetch user data
    public void getUserDataAsync(String username, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getUserData");
        request.put("username", username);
        request.put("password", password);
        addRequest(request);
    }

    // Method to upload a profile picture
    public void uploadProfilePictureAsync(String username, String password, String encodedImage) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "uploadProfilePicture");
        request.put("username", username);
        request.put("password", password);
        request.put("profilePicture", encodedImage);

        addRequest(request);
    }
}


