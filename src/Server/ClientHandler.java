package Server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
                try {
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
                        case "signupDelivery":
                            response = handleSignupDelivery(request);
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
                        case "changePassword":
                            response = handleChangePassword(request);
                            break;
                        case "changeEmail":
                            response = handleChangeEmail(request);
                            break;
                        case "disconnect":
                            response = handleDisconnect(request);
                            break;
                        case "uploadProfilePicture":
                            response = handleProfilePictureUpload(request);
                            break;
                        case "getImage":
                            response = handleGetImage(request);
                            break;
                        default:
                            response = handleDefault();
                            break;
                    }

                    // Send the response back to the client
                    System.out.println("Sending to client: " + response);
                    out.println(response);
                }
                catch (Exception e) {
                    response = createResponse(false, "Error: " + e.getMessage());
                    out.println(response);
                }

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

    private boolean authenticateUser(Map<String, String> params) {
        String username = params.get("username");
        String password = hashPassword(params.get("password"));
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(password)) {
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

        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                if (user instanceof RestaurantUser) {
                    if (ServerApp.loggedInRestaurants.contains(user)) {
                        return createResponse(false, "Restaurant already logged in");
                    }
                    ServerApp.loggedInRestaurants.add((RestaurantUser) user);
                }
                if (user instanceof CustomerUser) {
                    return createResponse(true, "Logged in as customer");
                }
                if (user instanceof DeliveryUser) {
                    return createResponse(true, "Logged in as delivery");
                }
                if (user instanceof RestaurantUser) {
                    return createResponse(true, "Logged in as restaurant");
                }
                if (user instanceof AdminUser) {
                    return createResponse(true, "Logged in as admin");
                }
            }
        }

        return createResponse(false, "Invalid username or password");
    }

    private String handleSignupCustomer(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address").replace(",", ""); // Remove commas from the address
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");

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

    private String handleSignupDelivery(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address").replace(",", ""); // Remove commas from the address
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");
        String Token = params.get("Token");

        if (!geoLocationService.validateAddress(address)) {
            return createResponse(false, "Invalid address");
        }

        if (usernameExists(username)) {
            return createResponse(false, "Username already exists");
        }

        if (emailExists(email)) {
            return createResponse(false, "Email already exists");
        }

        if (!checkToken(Token)) {
            return createResponse(false, "Token already exists");
        }

        CustomerUser newUser = new CustomerUser(username, hashPassword(password), address, phoneNumber, email);
        ServerApp.addUser(newUser);

        return createResponse(true, "Customer signup successful");
    }

    private boolean checkToken(String token)
    {
        File file = new File("Token.txt");
        List<String> lines = new ArrayList<>();
        boolean tokenFound = false;

        try {
            Scanner scanner = new Scanner(file);

            // Read all lines and store them, skipping the line with the token
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals(token)) {
                    tokenFound = true;
                    continue; // Skip adding this line to the list
                }
                lines.add(line);
            }
            scanner.close();

            // Write all lines back to the file (excluding the deleted token)
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                for (String l : lines) {
                    writer.println(l);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return tokenFound;
    }

    private String handleSignupRestaurant(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String address = params.get("address").replace(",", ""); // Remove commas from the address
        String phoneNumber = params.get("phoneNumber");
        String email = params.get("email");
        String businessPhoneNumber = params.get("businessPhoneNumber");
        String cuisine = params.get("cuisine");

        if (!ServerApp.getAvailableCuisines().contains(cuisine)) {
            return createResponse(false, "Invalid cuisine");
        }

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
        String username = params.get("username");
        String password = params.get("password");

        User user = null;

        for (User user_ : ServerApp.allUsers) {
            if (user_.getUserName().equals(username) && user_.checkPassword(hashPassword(password))) {
                user = user_;
            }
        }

        if(user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        // Get user's address for calculating distance
        String maxDistanceStr = params.get("distance");
        String cuisine = params.get("cuisine");

        // Get customer coordinates

        double[] customerCoordinates = user.getCoordinates();
        if (customerCoordinates == null) {
            return createResponse(false, "Unable to determine customer location");
        }

        // Parse the max distance, defaulting to 30km if not provided
        double maxDistance = (maxDistanceStr != null && !maxDistanceStr.isEmpty()) ? Double.parseDouble(maxDistanceStr) : 30.0;
        System.out.println("Max distance: " + maxDistance);

        List<Map<String, Object>> restaurantInfoList = new ArrayList<>();

        for (RestaurantUser restaurant : ServerApp.loggedInRestaurants) {
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
                    restaurantInfo.put("restaurantName", restaurant.getUserName());
                    restaurantInfo.put("address", restaurant.getAddress());
                    restaurantInfo.put("distance", String.format("%.2f", distance));
                    restaurantInfo.put("cuisine", restaurant.getCuisine());
                    restaurantInfo.put("phoneNumber", restaurant.getBusinessPhoneNumber());

                    // Include profile picture URL if it exists
                    if (restaurant.hasProfilePicture()) {
                        String profilePictureUrl = "http://" + ServerApp.SERVER_IP + ":" + ServerApp.IMAGE_SERVER_PORT + "/profile_pictures/" + restaurant.getUserName() + ".jpg";
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

    private String handleGetMenu(Map<String, String> params) throws IOException {
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
                    String itemImagePath = "menu_item_images/" + restaurant.getUserName() + "_" + item.getName() + ".jpg";
                    File itemImageFile = new File(itemImagePath);

                    if (itemImageFile.exists()) {
                        // Assuming you have a method or base URL that constructs the correct URL for accessing images
                        String imageUrl = "http://" + ServerApp.SERVER_IP + ":" + ServerApp.IMAGE_SERVER_PORT + "/" + itemImagePath;
                        itemInfo.put("photoUrl", imageUrl);
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


    private static int orderCounter = 0; // Counter for the number of orders
    private CreditCardAuthenticator creditCardAuthenticator = new CreditCardAuthenticator(); // Initialize the mock authenticator

    private String handlePlaceOrder(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String restaurantName = params.get("restaurantName");
        System.out.println("okay here");
        Object items = params.get("items");
        System.out.println("still here");
        String customerNote = params.get("customerNote");
        String status = "Pending"; // Default status
        boolean isSendHome = params.get("sendHome").equalsIgnoreCase("true");
        String address = params.get("address");

        CustomerUser customer = null;
        if (authenticateUser(params)) {
            for (User user : ServerApp.allUsers) {
                if (user instanceof CustomerUser && user.getUserName().equals(username)) {
                    customer = (CustomerUser) user;
                    break;
                }
            }
        }

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
        for (RestaurantUser loggedInRestaurant : ServerApp.loggedInRestaurants) {
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

        int orderId = ++orderCounter;
        // Properly handling the JSON parsing
        List<Order.Item> itemsList = (ArrayList<Order.Item>) gson.fromJson(gson.toJson(items), new TypeToken<List<Order.Item>>(){}.getType());

        Order order = new Order(orderId, new Date(), itemsList, customer.getUserName(), restaurantName, status, customerNote, address);

        customer.addOrder(order);
        restaurant.addOrder(order);

        ServerApp.saveOrder(order);


        return createResponse(true, "Order placed successfully with ID: " + orderId);
    }

    private String handleUpdateMenu(Map<String, String> params) throws IOException {
        String username = params.get("username");
        String password = params.get("password");
        String restaurantName = params.get("restaurantName");
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
        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
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
                restaurant.getMenu().remove(existingItem);
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
        String username = params.get("username");
        String password = params.get("password");
        String creditCardNumber = params.get("creditCardNumber");
        String expirationDate = params.get("expirationDate");
        String cvv = params.get("cvv");

        CustomerUser customer = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof CustomerUser && user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                customer = (CustomerUser) user;
                break;
            }
        }
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
        String username = params.get("username");
        String password = params.get("password");

        User authenticatedUser = null;
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                authenticatedUser = user;
                break;
            }
        }
        if (authenticatedUser == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        if (authenticatedUser instanceof CustomerUser) {
            return createResponse(true, gson.toJson(((CustomerUser) authenticatedUser).getOrderHistory()));
        } else if (authenticatedUser instanceof RestaurantUser) {
            return createResponse(true, gson.toJson(((RestaurantUser) authenticatedUser).getOrders()));
        }
        return createResponse(false, "User type not recognized");
    }

    private String handleMarkOrderComplete(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        int orderId = Integer.parseInt(params.get("orderId"));

        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

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

        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }
        String menuItemName = params.get("menuItemName");
        restaurant.disableMenuItem(menuItemName);
        return createResponse(true, "Menu items disabled");
    }

    private String handleEnableMenuItems(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        String menuItemName = params.get("menuItemName");
        restaurant.enableMenuItem(menuItemName);
        return createResponse(true, "Menu items enabled");
    }

    private String handleGetCurrentOrders(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        RestaurantUser restaurant = null;
        for (User user : ServerApp.allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                restaurant = (RestaurantUser) user;
                break;
            }
        }
        if (restaurant == null) {
            return createResponse(false, "Authentication failed or restaurant not found");
        }

        return gson.toJson(restaurant.getCurrentOrders());
    }

    private String handleDisconnect(Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        User userToDisconnect = null;
        for (User user : ServerApp.allUsers) {
            if (user.getUserName().equals(username) && user.checkPassword(hashPassword(password))) {
                userToDisconnect = user;
                break;
            }
        }

        if (userToDisconnect == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        if (userToDisconnect instanceof RestaurantUser) {
            ServerApp.loggedInRestaurants.remove(userToDisconnect);
            System.out.println("Restaurant " + userToDisconnect.getUserName() + " has been logged out.");
        }

        return createResponse(true, "Disconnected successfully");
    }

    private String handleGetAvailableCuisines() {
        return createResponse(true, ServerApp.getAvailableCuisines());
    }

    private String handleChangePassword(Map<String, String> params) {
        String username = params.get("username");
        String oldPassword = hashPassword(params.get("oldPassword"));
        String newPassword = params.get("newPassword");

        User user = null;
        for (User u : ServerApp.allUsers) {
            if (u.getUserName().equals(username) && u.checkPassword(oldPassword)) {
                user = u;
                break;
            }
        }

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        user.setHashedPassword(hashPassword(newPassword));
        ServerApp.updateUserInCSV(user);

        return createResponse(true, "Password changed successfully");
    }

    private String handleChangeEmail(Map<String, String> params) {
        String username = params.get("username");
        String password = hashPassword(params.get("password"));
        String newEmail = params.get("newEmail");

        User user = null;
        for (User u : ServerApp.allUsers) {
            if (u.getUserName().equals(username) && u.checkPassword(password)) {
                user = u;
                break;
            }
        }

        if (user == null) {
            return createResponse(false, "Authentication failed or user not found");
        }

        user.setEmail(newEmail);
        ServerApp.updateUserInCSV(user);

        return createResponse(true, "Email changed successfully");
    }

    private String handleProfilePictureUpload(Map<String, String> params) {
        String username = params.get("username");
        String encodedImage = params.get("profilePicture");

        try {
            for (User user : ServerApp.allUsers) {
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

                        ServerApp.updateUserInCSV(user);  // Save updated user data
                        return createResponse(true, "Profile picture uploaded successfully");
                    } else {
                        return createResponse(false, "No profile picture provided");
                    }
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

    private String handleDefault() {
        return createResponse(false, "Invalid request type");
    }
}
