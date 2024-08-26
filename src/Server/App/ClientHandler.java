// Group: 6
package Server.App;

import Server.Models.*;
import Server.Utilities.CreditCardAuthenticator;
import Server.Utilities.GeoLocationService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

import static Server.App.ServerApp.DELIVERY_FEE;
import static java.lang.Thread.sleep;
import static Server.ServerMain.IMAGE_URL;

// ClientHandler is responsible for handling communication with a single client in a separate thread
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Gson gson;
    private final GeoLocationService geoLocationService;

    // Constructor to initialize client handler with the client's socket
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gson = ServerApp.gsonCreator();
        this.geoLocationService = new GeoLocationService(); // Initialize the GeoLocationService
    }

    // The main method that handles client requests
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine = " ";
            // Continuously listen for client requests
            while (inputLine != null) {
                try {
                    inputLine = in.readLine(); // Read client's request
                } catch (IOException e) {
                    clientSocket.close(); // Close socket on error
                    e.printStackTrace();
                    break;
                }
                System.out.println("Received from client: " + inputLine);

                // Parse the incoming JSON request
                Map<String, String> request = parseRequest(inputLine);
                if (request == null) {
                    out.println(createResponse(false, "Invalid request format"));
                    continue;
                }
                String type = request.get("type");
                String response;
                try {
                    // Handle request based on its type
                    response = switch (type) {
                        case "login" -> handleLogin(request);
                        case "signupCustomer" -> handleSignUp(request, "customer");
                        case "signupRestaurant" -> handleSignUp(request, "restaurant");
                        case "signupDelivery" -> handleSignUp(request, "delivery");
                        case "getRestaurants" -> handleGetRestaurants(request);
                        case "getMenu" -> handleGetMenu(request);
                        case "placeOrder" -> handlePlaceOrder(request);
                        case "updateMenu" -> handleUpdateMenu(request);
                        case "updateCreditCard" -> handleUpdateCreditCard(request);
                        case "getOrdersHistory" -> handleGetOrdersHistory(request);
                        case "markOrderReadyForPickup" -> handleMarkOrderReadyForPickup(request);
                        case "disableMenuItem" -> handleDisableMenuItems(request);
                        case "enableMenuItem" -> handleEnableMenuItems(request);
                        case "getCurrentOrders" -> handleGetCurrentOrders(request);
                        case "getAvailableCuisines" -> handleGetAvailableCuisines();
                        case "changePassword" -> handleChangePassword(request);
                        case "changeEmail" -> handleChangeEmail(request);
                        case "disconnect" -> handleDisconnect(request);
                        case "uploadProfilePicture" -> handleProfilePictureUpload(request);
                        case "getImage" -> handleGetImage(request);
                        case "changeParameter" -> handleUpdateParameter(request);
                        case "deleteAccount" -> handleDeleteAccount(request);
                        case "getDeliveryOrders" -> handleGetDeliveryOrders(request);
                        case "pickupOrder" -> handlePickupOrder(request);
                        case "checkIfOnDelivery" -> handleCheckIfOnDelivery(request);
                        case "markOrderDelivered" -> handleMarkOrderDelivered(request);
                        case "getUserData" -> handleGetUserData(request);
                        case "getIncomeData" -> handleGetIncomeData(request);
                        default -> handleDefault();
                    };

                    // Send the response back to the client
                    System.out.println("Sending to client: " + response);
                    out.println(response);
                    Thread.sleep(500);
                }
                catch (Exception e) {
                    response = createResponse(false, "Error: " + e.getMessage());
                    out.println(response);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (!clientSocket.isClosed())
                    clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Handle fetching user data based on the request parameters
    private String handleGetUserData(Map<String, String> request) {
        String username = request.get("username");
        User user = authenticateUser(request);
        if (user == null) {
            return createResponse(false, "User not found");
        }
        Map<String, String> userData = new HashMap<>();
        userData.put("username", user.getUserName());
        userData.put("email", user.getEmail());
        userData.put("address", user.getAddress());
        userData.put("phoneNumber", user.getPhoneNumber());
        if (user instanceof CustomerUser customer) {
            if (customer.getCreditCardNumber().length() < 16) {
                userData.put("creditCardNumber", customer.getCreditCardNumber());
            } else {
                userData.put("creditCardNumber", "**** **** **** " + customer.getCreditCardNumber().substring(12));
            }
        }  else if (user instanceof RestaurantUser restaurant) {
            userData.put("businessPhoneNumber", restaurant.getBusinessPhoneNumber());
            userData.put("cuisine", restaurant.getCuisine());
            userData.put("revenue", String.valueOf(restaurant.getRevenue()));
            userData.put("restaurantName", (String) restaurant.getRestaurantName());
            userData.put("profilePicture", restaurant.getProfilePictureUrl());
        }

        return createResponse(true, gson.toJson(userData));
    }

    // Parse the client's JSON request into a Map
    private Map<String, String> parseRequest(String inputLine) {
        Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
        return gson.fromJson(inputLine, type);
    }

    // Create a response JSON string
    private String createResponse(boolean success, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("success", String.valueOf(success));
        response.put("message", message);
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
        String methodName = e.getMethodName();
        response.put("type" , methodName);
        return gson.toJson(response);
    }

    // Save an image for a menu item to the server's file system
    public void saveMenuItemImage(InputStream imageInputStream, String restaurantName, String itemName) throws IOException {
        String directoryPath = "menu_item_images/";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
        String filePath = directoryPath + restaurantName + "_" + itemName + ".jpg"; // Assuming JPG format
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = imageInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    // Hash a password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: SHA-256 algorithm not found!", e);
        }
    }

    // Authenticate a user based on the provided credentials
    private User authenticateUser(Map<String, String> params) {
        String username = params.get("username");
        String password = hashPassword(params.get("password"));
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(password)) {
                handleLogin(params);
                return user;
            }
        }
        return null;
    }

    // Check if an email already exists in the system
    private boolean emailExists(String email) {
        for (User user : ServerApp.allUsers) {
            if (user instanceof CustomerUser && user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    // Check if a username already exists in the system
    private boolean usernameExists(String username) {
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username)) {
                return true;
            }
        }
        return false;
    }


    private int incrementOrderId() {
        String idFilePath = "server_logs/order_id.txt";
        File idFile = new File(idFilePath);
        if (!idFile.exists()) {
            try (PrintWriter writer = new PrintWriter(idFilePath)) {
                writer.println("0");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        int orderID = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(idFilePath)) ) {
            orderID = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (PrintWriter writer = new PrintWriter(idFilePath)) {
            writer.println(orderID + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orderID;
    }

    private boolean checkToken(String token) {
        if (token.length() < 3)
            return false;
        File file = new File("Token.txt");
        List<String> lines = new ArrayList<>();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean tokenFound = false;

        try {
            Scanner scanner = new Scanner(file);

            // Read all lines and store them, skipping the line with the token
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.replace("\n", "").equals(token) && !tokenFound) {
                    tokenFound = true;
                } else {
                    lines.add(line);
                }
            }
            scanner.close();

            // Write all lines back to the file (excluding the deleted token)
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String l : lines) {
                    writer.println(l);
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return tokenFound;
    }

    public boolean containsOnlyLettersAndNumbers(String str) {
        if (str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetter(str.charAt(i)) && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void saveProfilePicture(InputStream pictureInputStream, String username) throws IOException {
        String directoryPath = "profile_pictures/";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        String filePath = directoryPath + username + ".jpg";
        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = pictureInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    // Handle a login request from the client
    private String handleLogin(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        User userToLogIn = null;

        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                userToLogIn = user;
                break;
            }
        }

        if (userToLogIn == null) {
            return createResponse(false, "Invalid username or password");
        }

        switch (userToLogIn) {
            case CustomerUser customerUser -> {
                return createResponse(true, "Logged in as customer");
            }
            case DeliveryUser deliveryUser -> {
                return createResponse(true, "Logged in as delivery");
            }
            case RestaurantUser restaurantUser -> {
                ServerApp.addLoggedInRestaurant(restaurantUser, clientSocket);
                return createResponse(true, "Logged in as restaurant");
            }
            default -> {
                return createResponse(false, "Invalid username or password");
            }
        }
    }

    // Handle a sign-up request from a customer
    private String handleSignUp(Map<String,String> params, String type) throws IOException{
        String username = params.get("username");
        String email = params.get("email");
        String address = params.get("address");
        String phoneNumber = params.get("phoneNumber");
        String password = params.get("password");

        if (usernameExists(username)) {
            return createResponse(false, "Username already exists");
        }

        if (emailExists(email)) {
            return createResponse(false, "Email already exists");
        }

        if (!geoLocationService.validateAddress(address)) {
            return createResponse(false, "Invalid address");
        }

        if (!containsOnlyLettersAndNumbers(username)) {
            return createResponse(false, "Username must contain only letters");
        }

        if (phoneNumber.length() != 10) {
            return createResponse(false, "Phone number must be 10 digits long");
        }

        if (password.length() < 8) {
            return createResponse(false, "Password must be at least 8 characters long");
        }

        return switch (type) {
            case "customer" -> handleSignupCustomer(params);
            case "delivery" -> handleSignupDelivery(params);
            case "restaurant" -> handleSignupRestaurant(params);
            default -> createResponse(false, "Invalid user type");
        };

    }

    // Create a new customer and add to the list of all users
    private String handleSignupCustomer(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address");
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");

        CustomerUser newUser = new CustomerUser(username, hashPassword(password), address, phoneNumber, email);
        ServerApp.addUser(newUser);

        return createResponse(true, "Customer signup successful");
    }

    // Handles signup for a delivery user
    private String handleSignupDelivery(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address");
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");
        String Token = params.get("token");

        if (!checkToken(Token)) {
            return createResponse(false, "Not authorized to create a delivery account");
        }

        // Create a new delivery user and add to server
        DeliveryUser newUser = new DeliveryUser(username, hashPassword(password), address, phoneNumber, email);
        ServerApp.addUser(newUser);

        return createResponse(true, "Customer signup successful");
    }

    // Handles signup for a restaurant user
    private String handleSignupRestaurant(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address");
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");
        String businessPhoneNumber = params.get("businessPhoneNumber");
        String cuisine = params.get("cuisine");

        // Create a new restaurant user and add to server
        RestaurantUser newRestaurant = new RestaurantUser(username, hashPassword(password), address, phoneNumber, email, businessPhoneNumber, cuisine, 0.0);
        ServerApp.addUser(newRestaurant);

        return createResponse(true, "Restaurant signup successful");
    }

    // Handles retrieval of restaurants based on distance and cuisine
    private String handleGetRestaurants(Map<String, String> params) throws IOException {
        User user = authenticateUser(params);

        if(user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        // Get user's address for calculating distance
        String maxDistanceStr = params.get("distance");
        String cuisine = params.get("cuisine");

        // Get customer coordinates
        double[] customerCoordinates = new double[2];
        if (params.get("sendHome").equals("true")){
            customerCoordinates = user.getCoordinates();
        } else {
            if (params.get("address") == null || params.get("address").isEmpty() || !geoLocationService.validateAddress(params.get("address"))) {
                return createResponse(false, "Invalid address");
            }
            customerCoordinates = geoLocationService.getCoordinates(params.get("address"));
        }
        if (customerCoordinates == null) {
            return createResponse(false, "Unable to determine customer location");
        }

        // Parse the max distance, defaulting to 30km if not provided
        double maxDistance = (maxDistanceStr != null && !maxDistanceStr.isEmpty()) ? Double.parseDouble(maxDistanceStr) : 30.0;
        System.out.println("Max distance: " + maxDistance);

        // Prepare the list of restaurant information
        List<Map<String, Object>> restaurantInfoList = new ArrayList<>();

        for (RestaurantUser restaurant : ServerApp.getLoggedInRestaurants()) {
            // Get the restaurant's coordinates
            double[] restaurantCoordinates = restaurant.getCoordinates();
            if (restaurantCoordinates != null) {
                // Calculate the distance between customer and restaurant
                double distance = geoLocationService.calculateDistance(
                        customerCoordinates[0], customerCoordinates[1],
                        restaurantCoordinates[0], restaurantCoordinates[1]);

                // Filter restaurants by distance and cuisine
                if (distance <= maxDistance &&
                        (cuisine == null || cuisine.isEmpty() || restaurant.getCuisine().equalsIgnoreCase(cuisine) ||
                        "all".equalsIgnoreCase(cuisine))) {

                    // Prepare the restaurant info to be sent back to the client
                    Map<String, Object> restaurantInfo = new HashMap<>();
                    restaurantInfo.put("restaurantName", restaurant.getUserName());      // I know this looks weird, but it's saves a lot of time.
                    restaurantInfo.put("restaurantActualName", restaurant.getRestaurantName());
                    restaurantInfo.put("address", restaurant.getAddress());
                    restaurantInfo.put("distance", String.format("%.2f", distance));
                    restaurantInfo.put("cuisine", restaurant.getCuisine());
                    restaurantInfo.put("phoneNumber", restaurant.getBusinessPhoneNumber());

                    // Include profile picture URL if it exists
                    if (restaurant.hasProfilePicture()) {
                        String profilePictureUrl = "http://" + IMAGE_URL + "/profile_pictures/" + restaurant.getUserName() + ".jpg";
                        restaurantInfo.put("profilePictureUrl", profilePictureUrl);
                    } else {

                        restaurantInfo.put("profilePictureUrl", null); // Null if no profile picture
                    }

                    restaurantInfoList.add(restaurantInfo);
                }
            }
        }

        // Return the list of restaurant info as JSON
        return createResponse(true, gson.toJson(restaurantInfoList));
    }


    // Retrieves the address of a user by username
    private String getUserNameAddress(String username) {
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username)) {
                return user.getAddress();
            }
        }
        return null;
    }

    // Retrieves the menu for a specific restaurant
    private String handleGetMenu(Map<String, String> params) {
        String restaurantName = params.get("restaurantName");

        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(restaurantName)) {
                RestaurantUser restaurant = (RestaurantUser) user;

                List<Map<String, Object>> menuItemsList = new ArrayList<>();

                for (Order.Item item : restaurant.getMenu()) {
                    Map<String, Object> itemInfo = new HashMap<>();
                    itemInfo.put("name", item.getName());
                    itemInfo.put("description", item.getDescription());
                    itemInfo.put("price", item.getPrice());
                    itemInfo.put("available", item.isAvailable());


                    // Create a URL for the item image
                    String itemImagePath = "menu_item_images/" + restaurant.getUserName() + "_" + item.getName().replace("'" , "") + ".jpg";
                    File itemImageFile = new File(itemImagePath);

                    if (itemImageFile.exists()) {
                        // Assuming you have a method or base URL that constructs the correct URL for accessing images
                        String imageUrl = "http://" + IMAGE_URL + "/" + itemImagePath;
                        itemInfo.put("photoUrl", imageUrl.replace(" ", "%20"));
                    } else {
                        itemInfo.put("photoUrl", null); // Null if no image
                    }

                    menuItemsList.add(itemInfo);
                }

                // Return the list of menu items as JSON
                return createResponse(true, gson.toJson(menuItemsList));
            }
        }

        return createResponse(false, "Restaurant not found");
    }

    private final CreditCardAuthenticator creditCardAuthenticator = new CreditCardAuthenticator(); // Initialize the mock authenticator

    // Places a new order
    private String handlePlaceOrder(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String restaurantName = params.get("restaurantName");
        Object items = params.get("items");
        String customerNote = params.get("customerNote");
        String status = "Pending"; // Default status
        boolean isSendHome = params.get("sendHome").equalsIgnoreCase("true");
        String address = params.get("address");

        CustomerUser customer = (CustomerUser) authenticateUser(params);

        if (isSendHome)
        {
            assert customer != null;
            address = customer.getAddress();
        }

        if (customer == null) {
            return createResponse(false, "Authentication failed or customer not found");
        }

        // Handle credit card information
        String creditCardNumber;
        String expirationDate;
        String cvv;

        if (params.get("useSavedCard").equals("false")) {
            creditCardNumber = params.get("creditCardNumber");
            expirationDate = params.get("expirationDate");
            cvv = params.get("cvv");
        } else {
            creditCardNumber = customer.getCreditCardNumber();
            expirationDate = customer.getExpirationDate();
            cvv = customer.getCvv();
        }

        // Find the restaurant
        RestaurantUser restaurant = null;
        for (RestaurantUser loggedInRestaurant : ServerApp.getLoggedInRestaurants()) {
            if (loggedInRestaurant.getUserName().equals(restaurantName)) {
                restaurant = loggedInRestaurant;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Restaurant not logged in or does not exist");
        }

        // Authenticate credit card
        if (!creditCardAuthenticator.authenticate(creditCardNumber, expirationDate, cvv)) {
            return createResponse(false, "Credit card authentication failed");
        }

        // Validate address
        if(!geoLocationService.validateAddress(address)){
            return createResponse(false, "Invalid address");
        }

        System.out.println("Address: " + address);
        System.out.println("Restaurant Address: " + restaurant.getAddress());

        // Check distance between customer and restaurant
        if(!GeoLocationService.checkSmallDistance(address, restaurant.getAddress(), 30.1)){
            return createResponse(false, "Restaurant is too far away");
        }

        // Create and save the order
        int orderId = incrementOrderId();
        // Properly handling the JSON parsing
        List<Order.Item> itemsList = (ArrayList<Order.Item>) gson.fromJson(gson.toJson(items), new TypeToken<List<Order.Item>>(){}.getType());
        Order order = new Order(orderId, new Date(), itemsList, customer.getUserName(), restaurantName, status, customerNote, address, restaurant.getAddress());

        // make payment
        double total = 0;
        for (Order.Item item : itemsList) {
            total += item.getPrice() * item.getQuantity();
        }
        if (!creditCardAuthenticator.makePayment(creditCardNumber, expirationDate, cvv, total)) {
            return createResponse(false, "Payment failed");
        }

        order.setTotal(total);

        customer.addOrder(order);
        restaurant.addOrder(order);

        ServerApp.updateOrder(order);

        if (!ServerApp.saveOrder(order)) {
            return createResponse(false, "Failed to save order");
        }

        // Notify the restaurant
        ServerApp.pushUpdateToRestaurant(restaurant, "New order received");
        return createResponse(true, "Order placed successfully with ID: " + orderId);
    }

    // Updates or removes a menu item for a restaurant
    private String handleUpdateMenu(Map<String, String> params) throws IOException {
        String itemName = params.get("itemName");
        boolean isAvailable = true;
        try{
            isAvailable = params.get("isAvailable").equalsIgnoreCase("true");
        }catch (Exception e){
            isAvailable = true;
        }
        boolean isRemove = params.get("action").equalsIgnoreCase("remove");
        double price = 0.0;
        String description = null;
        String encodedImage = null;

        // Authenticate the restaurant
        RestaurantUser restaurant = (RestaurantUser) authenticateUser(params);

        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        // Check if the restaurant is logged in
        if (!ServerApp.isLogged(restaurant)) {
            return createResponse(false, "Restaurant not logged in");
        }

        // Find the existing item in the menu, if it exists
        Order.Item existingItem = null;
        for (Order.Item menuItem : restaurant.getMenu()) {
            if (menuItem.getName().equalsIgnoreCase(itemName)) {
                existingItem = menuItem;
                break;
            }
        }

        if (isRemove) {
            // Handle remove action
            if (existingItem != null) {
                restaurant.removeMenuItem(existingItem.getName());
                ServerApp.saveMenu(restaurant);
                return createResponse(true, "Menu item removed successfully");
            } else {
                return createResponse(false, "Menu item not found");
            }
        } else {
            // Handle add/update action
            try {
                price = Double.parseDouble(params.get("price"));
                description = params.get("description");
                encodedImage = params.get("image");
            } catch (NumberFormatException e) {
                return createResponse(false, "Invalid price format");
            }

            // Decode the Base64 image if provided
            String photoUrl = null;
            if (encodedImage != null && !encodedImage.isEmpty()) {
                byte[] imageBytes;
                try {
                    imageBytes = Base64.getDecoder().decode(encodedImage);
                } catch (IllegalArgumentException e) {
                    return createResponse(false, "Invalid image format");
                }

                // Ensure the directory exists
                File directory = new File("menu_item_images");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String imageFileName = restaurant.getUserName() + "_" + itemName + ".jpg"; // Assuming JPG format
                File imageFile = new File(directory, imageFileName);
                Files.write(imageFile.toPath(), imageBytes);
                photoUrl = "menu_item_images/" + imageFileName;
            }

            if (existingItem != null) {
                // Update the existing item
                existingItem.setPrice(price);
                existingItem.setDescription(description);
                existingItem.setPhotoUrl(photoUrl);
                existingItem.setAvailable(isAvailable);
            } else {
                // Add the new item if it doesn't exist
                Order.Item newItem = new Order.Item(itemName, price, photoUrl, description, isAvailable);
                restaurant.addMenuItem(newItem);
            }

            // Save the updated menu
            ServerApp.saveMenu(restaurant);
            return createResponse(true, "Menu item added/updated successfully");
        }
    }

    // Updates a customers credit card information
    private String handleUpdateCreditCard(Map<String, String> params) {
        // Extract parameters
        String creditCardNumber = params.get("creditCardNumber");
        String expirationDate = params.get("expirationDate");
        String cvv = params.get("cvv");

        // Authenticate the customer
        CustomerUser customer = (CustomerUser) authenticateUser(params);

        if (customer == null) {
            return createResponse(false, "Authentication failed or customer not found");
        }

        if (!creditCardAuthenticator.authenticate(creditCardNumber, expirationDate, cvv)) {
            return createResponse(false, "Credit card authentication failed");
        }

        // Update credit card details
        customer.setCreditCardNumber(creditCardNumber);
        customer.setExpirationDate(expirationDate);
        customer.setCvv(cvv);

        return createResponse(true, "Credit card updated successfully");
    }

    // Retrieves the order history for a user
    private String handleGetOrdersHistory(Map<String, String> params) {
        User authenticatedUser = authenticateUser(params);
        return switch (authenticatedUser) {
            case null -> createResponse(false, "Authentication failed or user not found");
            case CustomerUser customerUser -> createResponse(true, gson.toJson(customerUser.getOrderHistory()));
            case RestaurantUser restaurantUser -> createResponse(true, gson.toJson(restaurantUser.getOrders()));
            case DeliveryUser deliveryUser -> createResponse(true, gson.toJson(deliveryUser.getCurrentOrder()));
            default -> createResponse(false, "User type not recognized");
        };
    }

    // Marks an order as ready for pickup and updates the status in the system
    private String handleMarkOrderReadyForPickup(Map<String, String> params) throws IOException {
        Type type = new TypeToken<Order>(){}.getType();
        Order order_from_user = gson.fromJson(params.get("order"), type);
        int orderId = order_from_user.getOrderId();
        Order order = ServerApp.getOrderById(orderId);

        // Check if the order exists
        if (order == null) {
            return createResponse(false, "Order not found");
        }

        // Authenticate the restaurant user
        RestaurantUser restaurant =  (RestaurantUser) authenticateUser(params);
        if(restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        // Update the order status and remove it from the restaurant's orders
        order.setStatus("Ready For Pickup");
        restaurant.removeOrder(order.getOrderId());
        // Update the customer's order history
        CustomerUser customer = getUserByUsername(order.getCustomerName());
        if (customer == null) {
            return createResponse(false, "Something went horribly wrong");
        }
        customer.removeOrder(order.getOrderId());
        customer.addOrder(order);
        // Save changes for the restaurant, customer, and order
        ServerApp.updateUser(restaurant);
        ServerApp.updateUser(customer);
        ServerApp.updateOrder(order);
        // Return success or failure response
        if (ServerApp.updateOrder(order)) {
            return createResponse(true, "Order status updated successfully");
        } else {
            return createResponse(false, "Failed to update order status");
        }

    }

    // Retrieves a CustomerUser object based on username
    private CustomerUser getUserByUsername(String customerName) {
        for (User user : ServerApp.allUsers) {
            if (user instanceof CustomerUser && user.getUserName().equals(customerName)) {
                return (CustomerUser) user;
            }
        }
        return null;
    }

    // Disables a menu item for a restaurant
    private String handleDisableMenuItems(Map<String, String> params) {
        // Authenticate the restaurant user
        RestaurantUser restaurant = (RestaurantUser) authenticateUser(params);

        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }
        // Disable the menu item
        String menuItemName = params.get("menuItemName");
        restaurant.disableMenuItem(menuItemName);
        try {
            ServerApp.saveMenu(restaurant);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return createResponse(true, "Menu items disabled");
    }

    // Enables a menu item for a restaurant
    private String handleEnableMenuItems(Map<String, String> params) {
        // Authenticate the restaurant user
        RestaurantUser restaurant = (RestaurantUser) authenticateUser(params);
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }
        // Enable the menu item
        String menuItemName = params.get("menuItemName");
        restaurant.enableMenuItem(menuItemName);
        return createResponse(true, "Menu items enabled");
    }

    // Retrieves the current orders for a restaurant
    private String handleGetCurrentOrders(Map<String, String> params) {
        // Authenticate the restaurant user
        RestaurantUser restaurant = (RestaurantUser) authenticateUser(params);

        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }
        // Return current orders as JSON
        return gson.toJson(restaurant.getCurrentOrders());
    }

    // Handles user disconnection
    private String handleDisconnect(Map<String, String> params) {
        // Authenticate the user
        User userToDisconnect = authenticateUser(params);
        // Handle restaurant user logout
        if (userToDisconnect == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        if (userToDisconnect instanceof RestaurantUser) {
            ServerApp.logoutRestaurant((RestaurantUser) userToDisconnect);
            System.out.println("Restaurant " + userToDisconnect.getUserName() + " has been logged out.");
        }

        return createResponse(true, "Disconnected successfully");
    }


    // Retrieves available cuisines
    private String handleGetAvailableCuisines() {
        return createResponse(true, ServerApp.getAvailableCuisines());
    }

    // Changes a user's password
    private String handleChangePassword(Map<String, String> params) throws IOException {
        String newPassword = params.get("newPassword");
        params.put("password", params.get("oldPassword"));
        // Authenticate the user
        User user = authenticateUser(params);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }
        // Update password
        user.setHashedPassword(hashPassword(newPassword));
        ServerApp.updateUser(user);

        return createResponse(true, "Password changed successfully");
    }

    // Changes a user's email address
    private String handleChangeEmail(Map<String, String> params) throws IOException {
        String newEmail = params.get("newEmail");
        // Authenticate the user
        User user = authenticateUser(params);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }
        // Update email
        user.setEmail(newEmail);
        ServerApp.updateUser(user);

        return createResponse(true, "Email changed successfully");
    }

    // Uploads and saves a user's profile picture
    private String handleProfilePictureUpload(Map<String, String> params) {
        String username = params.get("username");
        String encodedImage = params.get("profilePicture");
        // Authenticate the user
        User user = authenticateUser(params);

        try {
            if (user instanceof RestaurantUser && user.getUserName().equals(username)) {
                if (encodedImage != null) {
                    // Decode the Base64 image
                    byte[] imageBytes = Base64.getDecoder().decode(encodedImage);

                    // Save the image in the restaurant's profile picture attribute
                    ((RestaurantUser) user).setProfilePicture(imageBytes);

                    // Optionally, save the image to a file
                    String filePath = "profile_pictures/" + username + ".jpg";
                    try (OutputStream outputStream = new FileOutputStream(filePath)) {
                        outputStream.write(imageBytes);
                    }
                    // Update user data
                    ServerApp.updateUser(user);  // Save updated user data
                    return createResponse(true, "Profile picture uploaded successfully");
                } else {
                    return createResponse(false, "No profile picture provided");
                }
            }
            return createResponse(false, "User not found");
        } catch (IOException e) {
            return createResponse(false, "Failed to save profile picture: " + e.getMessage());
        }
    }

    // Retrieves an image file from the server
    private String handleGetImage(Map<String, String> params) {
        String imagePath = params.get("imagePath"); // The path sent by the client, e.g., "profile_pictures/restaurant_name.jpg"

        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            try {
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

                // Create the response map
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Image retrieved successfully");
                response.put("imageData", Base64.getEncoder().encodeToString(imageBytes)); // Encode image bytes to Base64

                // Convert the response to JSON and return it
                return gson.toJson(response);

            } catch (IOException e) {
                // Return an error response if there is an issue reading the image
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("success", "false");
                errorResponse.put("message", "Error reading image: " + e.getMessage());

                return gson.toJson(errorResponse);
            }
        } else {
            // Return an error response if the image file doesn't exist
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("success", "false");
            errorResponse.put("message", "Image not found");

            return gson.toJson(errorResponse);
        }
    }
    // Deletes a user's account
    private String handleDeleteAccount(Map<String, String> request) {
        // Authenticate the user
        User user = authenticateUser(request);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }
        // Remove the user from the server
        ServerApp.removeUser(user);

        return createResponse(true, "Account deleted successfully");
    }

    // Updates a specific user parameter based on request
    private String handleUpdateParameter(Map<String, String> request) throws IOException {
        String parameter = request.get("parameter");
        String value = request.get("newValue");
        // Authenticate the user
        User user = authenticateUser(request);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }
        // Update the specified parameter
        switch (parameter) {
            case "address" -> user.setAddress(value);
            case "phoneNumber" -> user.setPhoneNumber(value);
            case "email" -> user.setEmail(value);
            case "businessPhoneNumber" -> {
                if (user instanceof RestaurantUser) {
                    ((RestaurantUser) user).setBusinessPhoneNumber(value);
                } else {
                    return createResponse(false, "User is not a restaurant");
                }
            }
            case "cuisine" -> {
                if (user instanceof RestaurantUser) {
                    ((RestaurantUser) user).setCuisine(value);
                } else {
                    return createResponse(false, "User is not a restaurant");
                }
            }
            case "RestaurantName" -> {
                if (user instanceof RestaurantUser) {
                    ((RestaurantUser) user).setRestaurantName(value);
                } else {
                    return createResponse(false, "User is not a restaurant");
                }
            }
            case "password" -> user.setHashedPassword(hashPassword(value));
            default -> {
                return createResponse(false, "Invalid parameter");
            }
        }
        // Save updated user data
        ServerApp.updateUser(user);

        return createResponse(true, "Parameter updated successfully");
    }

    // Retrieves income data for a delivery user
    private String handleGetIncomeData(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        // Authenticate the delivery user
        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);
        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }
        // Return income data as JSON
        return createResponse(true, gson.toJson(deliveryUser.getIncome()));
    }


    // Marks an order as delivered and updates the delivery user's income
    private String handleMarkOrderDelivered(Map<String, String> request) throws IOException {
        // Authenticate the delivery user
        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);
        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }

        // Check if the delivery user has a current order
        if (deliveryUser.getCurrentOrder() == null) {
            return createResponse(false, "You are not on a delivery");
        }

        // Update order status and delivery user's income
        deliveryUser.getCurrentOrder().setStatus("Delivered");
        deliveryUser.addIncome(DELIVERY_FEE);
        ServerApp.updateOrder(deliveryUser.getCurrentOrder());
        deliveryUser.setCurrentOrder(null);
        ServerApp.updateUser(deliveryUser);

        return createResponse(true, "Order marked as delivered");
    }

    // Checks if a delivery user is currently on a delivery
    private String handleCheckIfOnDelivery(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        // Authenticate the delivery user
        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);
        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }

        // Check if the delivery user has a current order
        if (deliveryUser.getCurrentOrder() == null) {
            return createResponse(true, "You are not on a delivery");
        }
        String address = deliveryUser.getCurrentOrder().getAddress();
        return createResponse(true, "You are on a delivery to " + address);
    }

    // Picks up an order for delivery
    private String handlePickupOrder(Map<String, String> request) throws IOException {
        String username = request.get("username");
        String password = request.get("password");
        int orderId = Integer.parseInt(request.get("orderId"));

        // Authenticate the delivery user
        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);
        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }

        // Check if the delivery user is already on a delivery
        if (deliveryUser.getCurrentOrder() != null) {
            return createResponse(false, "You already have an order to deliver");
        }

        // Update the order status
        List<Order> deliveryOrders = List.of(ServerApp.getReadyForPickupOrders());
        for (Order order : deliveryOrders) {
            if (order.getOrderId() == orderId) {
                order.setStatus("Picked Up");
                order.setDeliveryPerson(deliveryUser.getUserName());
                ServerApp.updateOrder(order);
                deliveryUser.setCurrentOrder(order);
            }
        }
        String address = deliveryUser.getCurrentOrder().getAddress();
        return createResponse(true, "Order picked up successfully for delivery to " + address);
    }

    // Retrieves orders available for delivery based on distance from the current location
    private String handleGetDeliveryOrders(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Authenticate the delivery user
        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);
        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }
        double desiredDistance = 0;
        List<Order> deliveryOrders = new ArrayList<>();
        double[] currentLocation = null;
        try {
            currentLocation = geoLocationService.getCoordinates(request.get("address"));
        } catch (Exception e) {
            return createResponse(false, "Invalid address");
        }

        // Check if current location was provided
        if (currentLocation == null || currentLocation.equals(new double[]{0.0, 0.0})) {
            return createResponse(false, "Current location not provided");
        }

        // Parse desired distance
        try {
            desiredDistance = Double.parseDouble(request.get("distance"));
        }
        catch (NumberFormatException e) {
            return createResponse(false, "Invalid distance format");
        }

        // Filter orders based on distance
        double distance = 999;
        for (Order order : ServerApp.getReadyForPickupOrders()) {
            if (order.getLocation() == null) {
                order.setStatus("Cancelled");
                try {
                    ServerApp.updateOrder(order);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!order.getStatus().equals("Ready For Pickup")) {
                System.out.println("order status: " + order.getStatus());
                continue;
            }
            try {
                System.out.println(currentLocation[0] + " " + currentLocation[1] + " " + order.getLocation()[0] + " " + order.getLocation()[1]);
                distance = geoLocationService.calculateDistance(currentLocation[0], currentLocation[1], order.getLocation()[0], order.getLocation()[1]);
                System.out.println("Distance: " + distance);
            }catch (Exception e){
                deliveryOrders.remove(order);
                continue;
            }
            if (distance > desiredDistance) {
                continue;
            }
            order.setDistance(distance);
            deliveryOrders.add(order);
        }
        // Check if there are any delivery orders
        if (deliveryOrders == null || deliveryOrders.isEmpty()) {
            return createResponse(false, "No orders available for delivery");
        }
        return createResponse(true, gson.toJson(deliveryOrders));
    }

    // Default handler for invalid request types
    private String handleDefault() {
        return createResponse(false, "Invalid request type");
    }
}
