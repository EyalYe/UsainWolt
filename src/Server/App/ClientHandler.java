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
import static Server.ServerMain.SERVER_IP;
import static Server.ServerMain.IMAGE_SERVER_PORT;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Gson gson;
    private final GeoLocationService geoLocationService;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.gson = ServerApp.gsonCreator();
        this.geoLocationService = new GeoLocationService(); // Initialize the GeoLocationService
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine = " ";
            while (inputLine != null) {
                try {
                    inputLine = in.readLine();
                } catch (IOException e) {
                    clientSocket.close();
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
        }

        return createResponse(true, gson.toJson(userData));
    }

    private Map<String, String> parseRequest(String inputLine) {
        Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
        return gson.fromJson(inputLine, type);
    }

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
            case AdminUser adminUser -> {
                return createResponse(true, "Logged in as admin");
            }
            default -> {
                return createResponse(false, "Invalid username or password");
            }
        }
    }

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

        DeliveryUser newUser = new DeliveryUser(username, hashPassword(password), address, phoneNumber, email);
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

        RestaurantUser newRestaurant = new RestaurantUser(username, hashPassword(password), address, phoneNumber, email, businessPhoneNumber, cuisine, 0.0);
        ServerApp.addUser(newRestaurant);

        return createResponse(true, "Restaurant signup successful");
    }

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
                        String profilePictureUrl = "http://" + SERVER_IP + ":" + IMAGE_SERVER_PORT + "/profile_pictures/" + restaurant.getUserName() + ".jpg";
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

    private String getUserNameAddress(String username) {
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username)) {
                return user.getAddress();
            }
        }
        return null;
    }

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
                        String imageUrl = "http://" + SERVER_IP + ":" + IMAGE_SERVER_PORT + "/" + itemImagePath;
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

        if (!creditCardAuthenticator.authenticate(creditCardNumber, expirationDate, cvv)) {
            return createResponse(false, "Credit card authentication failed");
        }

        if(!geoLocationService.validateAddress(address)){
            return createResponse(false, "Invalid address");
        }

        System.out.println("Address: " + address);
        System.out.println("Restaurant Address: " + restaurant.getAddress());

        if(!GeoLocationService.checkSmallDistance(address, restaurant.getAddress(), 30.1)){
            return createResponse(false, "Restaurant is too far away");
        }

        int orderId = incrementOrderId();
        // Properly handling the JSON parsing
        List<Order.Item> itemsList = (ArrayList<Order.Item>) gson.fromJson(gson.toJson(items), new TypeToken<List<Order.Item>>(){}.getType());

        Order order = new Order(orderId, new Date(), itemsList, customer.getUserName(), restaurantName, status, customerNote, address, restaurant.getAddress());

        customer.addOrder(order);
        restaurant.addOrder(order);

        ServerApp.updateOrder(order);

        if (!ServerApp.saveOrder(order)) {
            return createResponse(false, "Failed to save order");
        }

        ServerApp.pushUpdateToRestaurant(restaurant, "New order received");
        return createResponse(true, "Order placed successfully with ID: " + orderId);
    }


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

            ServerApp.saveMenu(restaurant);
            return createResponse(true, "Menu item added/updated successfully");
        }
    }

    private String handleUpdateCreditCard(Map<String, String> params) {
        String creditCardNumber = params.get("creditCardNumber");
        String expirationDate = params.get("expirationDate");
        String cvv = params.get("cvv");

        CustomerUser customer = (CustomerUser) authenticateUser(params);

        if (customer == null) {
            return createResponse(false, "Authentication failed or customer not found");
        }

        if (!creditCardAuthenticator.authenticate(creditCardNumber, expirationDate, cvv)) {
            return createResponse(false, "Credit card authentication failed");
        }

        customer.setCreditCardNumber(creditCardNumber);
        customer.setExpirationDate(expirationDate);
        customer.setCvv(cvv);

        return createResponse(true, "Credit card updated successfully");
    }

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

    private String handleMarkOrderReadyForPickup(Map<String, String> params) throws IOException {
        Type type = new TypeToken<Order>(){}.getType();
        Order order_from_user = gson.fromJson(params.get("order"), type);
        int orderId = order_from_user.getOrderId();
        Order order = ServerApp.getOrderById(orderId);

        if (order == null) {
            return createResponse(false, "Order not found");
        }

        RestaurantUser restaurant =  (RestaurantUser) authenticateUser(params);
        if(restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        order.setStatus("Ready For Pickup");
        restaurant.removeOrder(order.getOrderId());
        CustomerUser customer = getUserByUsername(order.getCustomerName());
        if (customer == null) {
            return createResponse(false, "Something went horribly wrong");
        }
        customer.removeOrder(order.getOrderId());
        customer.addOrder(order);
        ServerApp.updateUser(restaurant);
        ServerApp.updateUser(customer);
        ServerApp.updateOrder(order);
        if (ServerApp.updateOrder(order)) {
            return createResponse(true, "Order status updated successfully");
        } else {
            return createResponse(false, "Failed to update order status");
        }

    }

    private CustomerUser getUserByUsername(String customerName) {
        for (User user : ServerApp.allUsers) {
            if (user instanceof CustomerUser && user.getUserName().equals(customerName)) {
                return (CustomerUser) user;
            }
        }
        return null;
    }

    private String handleDisableMenuItems(Map<String, String> params) {

        RestaurantUser restaurant = (RestaurantUser) authenticateUser(params);

        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }
        String menuItemName = params.get("menuItemName");
        restaurant.disableMenuItem(menuItemName);
        try {
            ServerApp.saveMenu(restaurant);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return createResponse(true, "Menu items disabled");
    }

    private String handleEnableMenuItems(Map<String, String> params) {

        RestaurantUser restaurant = (RestaurantUser) authenticateUser(params);
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        String menuItemName = params.get("menuItemName");
        restaurant.enableMenuItem(menuItemName);
        return createResponse(true, "Menu items enabled");
    }

    private String handleGetCurrentOrders(Map<String, String> params) {
        RestaurantUser restaurant = (RestaurantUser) authenticateUser(params);

        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        return gson.toJson(restaurant.getCurrentOrders());
    }

    private String handleDisconnect(Map<String, String> params) {

        User userToDisconnect = authenticateUser(params);

        if (userToDisconnect == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        if (userToDisconnect instanceof RestaurantUser) {
            ServerApp.logoutRestaurant((RestaurantUser) userToDisconnect);
            System.out.println("Restaurant " + userToDisconnect.getUserName() + " has been logged out.");
        }

        return createResponse(true, "Disconnected successfully");
    }

    private String handleGetAvailableCuisines() {
        return createResponse(true, ServerApp.getAvailableCuisines());
    }

    private String handleChangePassword(Map<String, String> params) throws IOException {
        String newPassword = params.get("newPassword");
        params.put("password", params.get("oldPassword"));

        User user = authenticateUser(params);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        user.setHashedPassword(hashPassword(newPassword));
        ServerApp.updateUser(user);

        return createResponse(true, "Password changed successfully");
    }

    private String handleChangeEmail(Map<String, String> params) throws IOException {
        String newEmail = params.get("newEmail");

        User user = authenticateUser(params);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        user.setEmail(newEmail);
        ServerApp.updateUser(user);

        return createResponse(true, "Email changed successfully");
    }

    private String handleProfilePictureUpload(Map<String, String> params) {
        String username = params.get("username");
        String encodedImage = params.get("profilePicture");

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

    private String handleDeleteAccount(Map<String, String> request) {

        User user = authenticateUser(request);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        ServerApp.removeUser(user);

        return createResponse(true, "Account deleted successfully");
    }

    private String handleUpdateParameter(Map<String, String> request) throws IOException {
        String parameter = request.get("parameter");
        String value = request.get("newValue");

        User user = authenticateUser(request);

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

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

        ServerApp.updateUser(user);

        return createResponse(true, "Parameter updated successfully");
    }

    private String handleGetIncomeData(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);
       if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }

        return createResponse(true, gson.toJson(deliveryUser.getIncome()));
    }

    private String handleMarkOrderDelivered(Map<String, String> request) throws IOException {
        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);

        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }

        if (deliveryUser.getCurrentOrder() == null) {
            return createResponse(false, "You are not on a delivery");
        }

        deliveryUser.getCurrentOrder().setStatus("Delivered");
        deliveryUser.addIncome(DELIVERY_FEE);
        ServerApp.updateOrder(deliveryUser.getCurrentOrder());
        deliveryUser.setCurrentOrder(null);
        ServerApp.updateUser(deliveryUser);

        return createResponse(true, "Order marked as delivered");
    }

    private String handleCheckIfOnDelivery(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);

        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }

        if (deliveryUser.getCurrentOrder() == null) {
            return createResponse(true, "You are not on a delivery");
        }
        String address = deliveryUser.getCurrentOrder().getAddress();
        return createResponse(true, "You are on a delivery to " + address);
    }

    private String handlePickupOrder(Map<String, String> request) throws IOException {
        String username = request.get("username");
        String password = request.get("password");
        int orderId = Integer.parseInt(request.get("orderId"));

        DeliveryUser deliveryUser = (DeliveryUser) authenticateUser(request);

        if (deliveryUser == null) {
            return createResponse(false, "Authentication failed or delivery user not found");
        }

        if (deliveryUser.getCurrentOrder() != null) {
            return createResponse(false, "You already have an order to deliver");
        }

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

    private String handleGetDeliveryOrders(Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");


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

        if (currentLocation == null || currentLocation.equals(new double[]{0.0, 0.0})) {
            return createResponse(false, "Current location not provided");
        }
        try {
            desiredDistance = Double.parseDouble(request.get("distance"));
        }
        catch (NumberFormatException e) {
            return createResponse(false, "Invalid distance format");
        }

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
        if (deliveryOrders == null || deliveryOrders.isEmpty()) {
            return createResponse(false, "No orders available for delivery");
        }
        return createResponse(true, gson.toJson(deliveryOrders));
    }

    private String handleDefault() {
        return createResponse(false, "Invalid request type");
    }
}
