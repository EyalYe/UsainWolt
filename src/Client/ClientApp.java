package Client;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientApp {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    public ClientApp(String serverAddress, int port) throws Exception {
        // Establish a connection to the server
        clientSocket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
        gson = new Gson();
    }

    // Helper method to send a request and return the response
    private Map<String, String> sendRequest(Map<String, String> request) throws Exception {
        String jsonRequest = gson.toJson(request);
        out.println(jsonRequest);
        String jsonResponse = in.readLine();

        // Check if the response is a JSON object
        if (jsonResponse.startsWith("{")) {
            return gson.fromJson(jsonResponse, Map.class);
        } else {
            // Handle the case where the response is just a plain string
            System.out.println("Unexpected server response: " + jsonResponse);
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("message", "Unexpected server response: " + jsonResponse);
            return response;
        }
    }


    // Method to login a user
    public Map<String, String> login(String username, String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "login");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    // Method to sign up a customer
    public Map<String, String> signupCustomer(String username, String password, String address, String phoneNumber, String email) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "signupCustomer");
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        return sendRequest(request);
    }

    // Method to sign up a restaurant
    public Map<String, String> signupRestaurant(String username, String password, String address, String phoneNumber, String email, String businessPhoneNumber) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "signupRestaurant");
        request.put("username", username);
        request.put("password", password);
        request.put("address", address);
        request.put("phoneNumber", phoneNumber);
        request.put("email", email);
        request.put("businessPhoneNumber", businessPhoneNumber);
        return sendRequest(request);
    }

    // Method to place an order
    public Map<String, String> placeOrder(String username, String password, String restaurantName, String items, String customerNote, String creditCardNumber, String expirationDate, String cvv) throws Exception {
        Map<String, String> request = new HashMap<>();
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

    // Method to update the restaurant menu
    public Map<String, String> updateMenu(String username, String password, String restaurantName, String itemName, double price) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "updateMenu");
        request.put("username", username);
        request.put("password", password);
        request.put("restaurantName", restaurantName);
        request.put("itemName", itemName);
        request.put("price", String.valueOf(price));
        return sendRequest(request);
    }

    // Method to update a customer's credit card information
    public Map<String, String> updateCreditCard(String username, String password, String creditCardNumber, String expirationDate, String cvv) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "updateCreditCard");
        request.put("username", username);
        request.put("password", password);
        request.put("creditCardNumber", creditCardNumber);
        request.put("expirationDate", expirationDate);
        request.put("cvv", cvv);
        return sendRequest(request);
    }

    // Method to get a customer's or restaurant's order history
    public Map<String, String> getOrdersHistory(String username, String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "getOrdersHistory");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    // Method to mark an order as complete
    public Map<String, String> markOrderComplete(String username, String password, int orderId) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "markOrderComplete");
        request.put("username", username);
        request.put("password", password);
        request.put("orderId", String.valueOf(orderId));
        return sendRequest(request);
    }

    // Method to disconnect a user
    public Map<String, String> disconnect(String username, String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "disconnect");
        request.put("username", username);
        request.put("password", password);
        return sendRequest(request);
    }

    public Map<String, String> getAvailableCuisines() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        return sendRequest(request);
    }

    // Close the connection to the server
    public void closeConnection() throws Exception {
        clientSocket.close();
    }

    public static void main(String[] args) {
        try {
            ClientApp clientApp = new ClientApp("localhost", 8080);

            // Example of logging in a user
            Map<String, String> response = clientApp.login("john_doe", "password123");
            System.out.println("Login Response: " + response);

            clientApp.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
