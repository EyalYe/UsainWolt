package Server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Gson gson;
    private GeoLocationService geoLocationService;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gson = new Gson();
        this.geoLocationService = new GeoLocationService(); // Initialize the GeoLocationService
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);

                // Parse the incoming JSON request
                Map<String, String> request = parseRequest(inputLine);
                String type = request.get("type");
                String response;

                switch (type) {
                    case "login":
                        response = handleLogin(request);
                        break;
                    case "signupCustomer":
                        response = handleSignupCustomer(request);
                        break;
                    case "signupRestaurant":
                        response = handleSignupRestaurant(request);
                        break;
                    case "getRestaurants":
                        response = handleGetRestaurants(request);
                        break;
                    case "getMenu":
                        response = handleGetMenu(request);
                        break;
                    case "placeOrder":
                        response = handlePlaceOrder(request);
                        break;
                    case "updateMenu":
                        response = handleUpdateMenu(request);
                        break;
                    case "updateCreditCard":
                        response = handleUpdateCreditCard(request);
                        break;
                    case "getOrdersHistory":
                        response = handleGetOrdersHistory(request);
                        break;
                    case "markOrderComplete":
                        response = handleMarkOrderComplete(request);
                        break;
                    case "disableMenuItems":
                        response = handleDisableMenuItems(request);
                        break;
                    case "enableMenuItems":
                        response = handleEnableMenuItems(request);
                        break;
                    case "getCurrentOrders":
                        response = handleGetCurrentOrders(request);
                        break;
                    case "getAvailableCuisines":
                        response = handleGetAvailableCuisines();
                        break;
                    case "disconnect":
                        response = handleDisconnect(request);
                        break;
                    default:
                        response = handleDefault();
                        break;
                }

                // Send the response back to the client
                System.out.println("Sending to client: " + response);
                out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Map<String, String> parseRequest(String inputLine) {
        Type type = new TypeToken<HashMap<String, String>>() {}.getType();
        return gson.fromJson(inputLine, type);
    }

    private String createResponse(boolean success, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("success", String.valueOf(success));
        response.put("message", message);
        System.out.println("am i getting in here?");
        System.out.println("Response: " + gson.toJson(response));
        return gson.toJson(response);
    }

    public static String hashPassword(String password) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Apply the hash function to the input password
            byte[] encodedHash = digest.digest(password.getBytes());

            // Convert the byte array into a hexadecimal string
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Return the hashed password as a hexadecimal string
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle the exception (SHA-256 algorithm not found)
            throw new RuntimeException("Error: SHA-256 algorithm not found!", e);
        }
    }

    private boolean authenticateUser(Map<String, String> params) {
        String username = params.get("username");
        String password_not_hashed = params.get("password");
        String password = hashPassword(password_not_hashed);
        User authenticatedUser = null;
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(password)) {
                authenticatedUser = user;
                return true;
            }
        }
        return false;
    }

    private boolean emailExists(String email) {
        for (User user : ServerApp.allUsers) {
            if (user instanceof CustomerUser && user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    private boolean usernameExists(String username) {
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private String handleLogin(Map<String, String> params) {
        if (authenticateUser(params)) {
            return createResponse(true, "Login successful");
        }
        return createResponse(false, "Invalid username or password");
    }

    private String handleSignupCustomer(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address");
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");

        // Validate the address using the GeoLocationService
        if (!geoLocationService.validateAddress(address)) {
            return createResponse(false, "Invalid address");
        }

        if (usernameExists(username)) {
            return createResponse(false, "Username already exists");
        }

        if (emailExists(email)) {
            return createResponse(false, "Email already exists");
        }

        CustomerUser newUser = new CustomerUser(username, hashPassword(password), address, phoneNumber, email);
        ServerApp.addUser(newUser);

        return createResponse(true, "Customer signup successful");
    }


    private String handleSignupRestaurant(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address");
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");
        String businessPhoneNumber = params.get("businessPhoneNumber");
        String cuisine = params.get("cuisine");

        // Validate the address using the GeoLocationService
        if (!geoLocationService.validateAddress(address)) {
            return createResponse(false, "Invalid address");
        }

        if (usernameExists(username)) {
            return createResponse(false, "Username already exists");
        }

        if (emailExists(email)) {
            return createResponse(false, "Email already exists");
        }

        RestaurantUser newRestaurant = new RestaurantUser(username, hashPassword(password), address, phoneNumber, email, businessPhoneNumber, cuisine, 0.0);
        ServerApp.addUser(newRestaurant);

        return createResponse(true, "Restaurant signup successful");
    }

    private String handleGetRestaurants(Map<String, String> params) throws IOException {
        String customerAddress = params.get("address");
        String maxDistanceStr = params.get("distance");
        String cuisine = params.get("cuisine");

        // Get customer coordinates
        double[] customerCoordinates = geoLocationService.getCoordinates(customerAddress);
        if (customerCoordinates == null) {
            return createResponse(false, "Unable to determine customer location");
        }

        double maxDistance = (maxDistanceStr != null && !maxDistanceStr.isEmpty()) ? Double.parseDouble(maxDistanceStr) : 30.0;

        List<RestaurantUser> filteredRestaurants = new ArrayList<>();
        for (RestaurantUser restaurant : ServerApp.loggedInRestaurants) {

            // Get the restaurant's coordinates
            double[] restaurantCoordinates = geoLocationService.getCoordinates(restaurant.getAddress());
            if (restaurantCoordinates != null) {
                double distance = geoLocationService.calculateDistance(
                        customerCoordinates[0], customerCoordinates[1],
                        restaurantCoordinates[0], restaurantCoordinates[1]);

                // Filter by distance and cuisine (if provided)
                if (distance <= maxDistance &&
                        (cuisine == null || cuisine.isEmpty() || restaurant.getCuisine().equalsIgnoreCase(cuisine))) {
                    filteredRestaurants.add(restaurant);
                }
            }
        }

        return gson.toJson(filteredRestaurants);
    }


    private String handleGetMenu(Map<String, String> params) {
        String restaurantName = params.get("restaurantName");
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(restaurantName)) {
                return gson.toJson(((RestaurantUser) user).getMenu());
            }
        }
        return createResponse(false, "Restaurant not found");
    }

    private static int orderCounter = 0; // Counter for the number of orders
    private CreditCardAuthenticator creditCardAuthenticator = new CreditCardAuthenticator(); // Initialize the mock authenticator

    private String handlePlaceOrder(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String restaurantName = params.get("restaurantName");
        String items = params.get("items");
        String customerNote = params.get("customerNote");
        String status = params.get("status");


        // Authenticate the customer
        CustomerUser customer = null;
        if (authenticateUser(params)) {
            for (User user : ServerApp.allUsers) {
                if (user instanceof CustomerUser && user.getUserName().equals(username)) {
                    customer = (CustomerUser) user;
                    break;
                }
            }
        }

        if (customer == null) {
            return createResponse(false, "Authentication failed or customer not found");
        }

        // Credit card information

        String creditCardNumber;
        String expirationDate;
        String cvv;

        if(params.get("useSavedCard").equals("false")){
            creditCardNumber = params.get("creditCardNumber");
            expirationDate = params.get("expirationDate");
            cvv = params.get("cvv");
        } else {
            creditCardNumber = customer.getCreditCardNumber();
            expirationDate = customer.getExpirationDate();
            cvv = customer.getCvv();
        }

        // Check if the restaurant is logged in
        RestaurantUser restaurant = null;
        for (RestaurantUser loggedInRestaurant : ServerApp.loggedInRestaurants) {
            if (loggedInRestaurant.getUserName().equals(restaurantName)) {
                restaurant = loggedInRestaurant;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Restaurant not logged in or does not exist");
        }

        // Authenticate the credit card
        if (!creditCardAuthenticator.authenticate(creditCardNumber, expirationDate, cvv)) {
            return createResponse(false, "Credit card authentication failed");
        }

        // Generate a new order ID
        int orderId = ++orderCounter;

        // Parse the items
        List<Order.Item> itemList = ServerApp.parseItems(items);

        // Create the order
        Order order = new Order(orderId, new Date(), itemList, username, restaurantName, status, customerNote);

        // Add the order to the customer's and restaurant's order history
        customer.addOrder(order);
        restaurant.addOrder(order);

        // Save the order to the file
        ServerApp.saveOrder(order);

        return createResponse(true, "Order placed successfully with ID: " + orderId);
    }

    private String handleUpdateMenu(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String restaurantName = params.get("restaurantName");
        String itemName = params.get("itemName");
        double price = Double.parseDouble(params.get("price"));

        // Authenticate the restaurant
        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(password)) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        // Check if the restaurant is logged in
        if (!ServerApp.loggedInRestaurants.contains(restaurant)) {
            return createResponse(false, "Restaurant not logged in");
        }

        // Update the menu
        restaurant.addMenuItem(itemName, price);
        ServerApp.saveMenu(restaurant);

        return createResponse(true, "Menu updated successfully");
    }

    private String handleUpdateCreditCard(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String creditCardNumber = params.get("creditCardNumber");
        String expirationDate = params.get("expirationDate");
        String cvv = params.get("cvv");

        // Authenticate the user
        CustomerUser customer = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof CustomerUser && user.getUserName().equals(username) && user.checkPassword(password)) {
                customer = (CustomerUser) user;
                break;
            }
        }
        if (customer == null) {
            return createResponse(false, "Authentication failed or customer not found");
        }

        // Authenticate the credit card
        if (!creditCardAuthenticator.authenticate(creditCardNumber, expirationDate, cvv)) {
            return createResponse(false, "Credit card authentication failed");
        }

        // Update the credit card information (mocked)
        customer.setCreditCardNumber(creditCardNumber); // Assume setCreditCardNumber exists
        return createResponse(true, "Credit card updated successfully");
    }

    private String handleGetOrdersHistory(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // Authenticate the user
        User authenticatedUser = null;
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(password)) {
                authenticatedUser = user;
                break;
            }
        }
        if (authenticatedUser == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        // Return the order history
        if (authenticatedUser instanceof CustomerUser) {
            return gson.toJson(((CustomerUser) authenticatedUser).getOrderHistory());
        } else if (authenticatedUser instanceof RestaurantUser) {
            return gson.toJson(((RestaurantUser) authenticatedUser).getOrders());
        }
        return createResponse(false, "User type not recognized");
    }

    private String handleMarkOrderComplete(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        int orderId = Integer.parseInt(params.get("orderId"));

        // Authenticate the restaurant
        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(password)) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        // Mark the order as complete (assuming orders are stored in the restaurant user)
        for (Order order : restaurant.getOrders()) {
            if (order.getOrderId() == orderId) {
                order.setStatus("Complete");
                return createResponse(true, "Order marked as complete");
            }
        }

        return createResponse(false, "Order not found");
    }

    private String handleDisableMenuItems(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // Authenticate the restaurant
        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(password)) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }
        String menuItemName = params.get("menuItemName");
        // Disable all menu items with the given name
        restaurant.disableMenuItem(menuItemName);
        return createResponse(true, "Menu items disabled");
    }

    private String handleEnableMenuItems(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // Authenticate the restaurant
        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(password)) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        String menuItemName = params.get("menuItemName");
        // Enable all menu items with the given name
        restaurant.enableMenuItem(menuItemName);
        return createResponse(true, "Menu items enabled");
    }

    private String handleGetCurrentOrders(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // Authenticate the restaurant
        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(password)) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        // Retrieve and return current orders
        return gson.toJson(restaurant.getCurrentOrders()); // Assume getCurrentOrders method exists
    }

    private String handleDisconnect(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // Authenticate the user
        User userToDisconnect = null;
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(password)) {
                userToDisconnect = user;
                break;
            }
        }

        if (userToDisconnect == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        // If the user is a restaurant, remove it from the logged-in restaurants list
        if (userToDisconnect instanceof RestaurantUser) {
            ServerApp.loggedInRestaurants.remove(userToDisconnect);
            System.out.println("Restaurant " + userToDisconnect.getUserName() + " has been logged out.");
        }

        // Any additional cleanup (if needed)
        return createResponse(true, "Disconnected successfully");
    }

    private String handleGetAvailableCuisines() {
        return createResponse(true, ServerApp.getAvailableCuisines());
    }

    private String handleDefault() {
        return createResponse(false, "Invalid request type");
    }
}
