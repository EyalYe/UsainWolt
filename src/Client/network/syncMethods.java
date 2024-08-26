// Group: 6
package Client.network;

import Client.model.Restaurant;
import Server.Models.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import static Server.Utilities.CustomDateAdapter.gsonCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class syncMethods {
    public static Gson gson = gsonCreator();

    // Method to send a request to the server and get a response
    public  Map<String, Object> sendRequest(Map<String, Object> request) throws Exception {
        return null;
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

    // Method to change password
    public Map<String, Object> changePassword(String username, String oldPassword, String newPassword) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changePassword");
        request.put("username", username);
        request.put("oldPassword", oldPassword);
        request.put("newPassword", newPassword);
        return sendRequest(request);
    }

    // Method to change an email
    public Map<String, Object> changeEmail(String username, String password, String newEmail) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", "changeEmail");
        request.put("username", username);
        request.put("password", password);
        request.put("newEmail", newEmail);
        return sendRequest(request);
    }

    // Method to update a profile picture
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


    // Method to get menu
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

    // Method to sign up a delivery
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
}
