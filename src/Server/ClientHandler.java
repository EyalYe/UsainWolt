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
                    case "disconnect":
                        response = handleDisconnect(request);
                        break;
                    default:
                        response = handleDefault();
                        break;
                }

                // Send the response back to the client
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
        return gson.toJson(response);
    }

    private String handleLogin(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.getHashedPassword().equals(password)) {
                if (user instanceof RestaurantUser) {
                    ServerApp.addLoggedInRestaurant((RestaurantUser) user);
                }
                return createResponse(true, "Login successful");
            }
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

        CustomerUser newUser = new CustomerUser(username, password, address, phoneNumber, email);
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

        RestaurantUser newRestaurant = new RestaurantUser(username, password, address, phoneNumber, email, businessPhoneNumber, cuisine, 0.0);
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

        // Credit card information
        String creditCardNumber = params.get("creditCardNumber");
        String expirationDate = params.get("expirationDate");
        String cvv = params.get("cvv");

        // Authenticate the customer
        CustomerUser customer = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof CustomerUser && user.getUserName().equals(username) && user.getHashedPassword().equals(password)) {
                customer = (CustomerUser) user;
                break;
            }
        }
        if (customer == null) {
            return createResponse(false, "Authentication failed or customer not found");
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
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.getHashedPassword().equals(password)) {
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
        return createResponse(true, "Credit card updated successfully");
    }

    private String handleGetOrdersHistory(Map<String, String> params) {
        String username = params.get("username");
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username)) {
                if (user instanceof CustomerUser) {
                    return gson.toJson(((CustomerUser) user).getOrderHistory());
                } else if (user instanceof RestaurantUser) {
                    return gson.toJson(((RestaurantUser) user).getOrders());
                }
            }
        }
        return createResponse(false, "User not found");
    }

    private String handleMarkOrderComplete(Map<String, String> params) {
        return createResponse(true, "Order marked as complete");
    }

    private String handleDisableMenuItems(Map<String, String> params) {
        return createResponse(true, "Menu items disabled");
    }

    private String handleEnableMenuItems(Map<String, String> params) {
        return createResponse(true, "Menu items enabled");
    }

    private String handleGetCurrentOrders(Map<String, String> params) {
        return createResponse(true, "Retrieved current orders");
    }

    private String handleDisconnect(Map<String, String> params) {
        return createResponse(true, "Disconnected successfully");
    }

    private String handleDefault() {
        return createResponse(false, "Unknown request type");
    }
}
