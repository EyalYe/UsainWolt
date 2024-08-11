package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ServerApp {
    private static final String USERS_FILE = "users.csv";
    private static final String MENUS_FILE = "menus.csv";
    private static final String ORDERS_FILE = "orders.csv";

    public static List<User> allUsers = new ArrayList<>();
    public static List<RestaurantUser> loggedInRestaurants = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Create the files if they don't exist
            createFileIfNotExists(USERS_FILE);
            createFileIfNotExists(MENUS_FILE);
            createFileIfNotExists(ORDERS_FILE);

            // Load data from CSV files
            loadUsersFromCSV();
            loadMenusFromCSV();
            loadOrdersFromCSV();

            // Set up server socket
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server is listening on port 12345");

            // Server loop to handle clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // Create a new thread to handle the client
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFileIfNotExists(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
            System.out.println(fileName + " created.");
        }
    }

    private static void loadUsersFromCSV() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            String userType = parts[0];
            String username = parts[1];
            String hashedPassword = parts[2];
            String address = parts[3];
            String phoneNumber = parts[4];
            String email = parts[5];

            if ("Customer".equals(userType)) {
                CustomerUser customer = new CustomerUser(username, hashedPassword, address, phoneNumber, email);
                allUsers.add(customer);
            } else if ("Restaurant".equals(userType)) {
                String businessPhoneNumber = parts[6];
                String cuisine = parts[7];
                double revenue = Double.parseDouble(parts[8]);
                RestaurantUser restaurant = new RestaurantUser(username, hashedPassword, address, phoneNumber, email, businessPhoneNumber, cuisine, revenue);
                allUsers.add(restaurant);
            }
        }
        reader.close();
    }

    private static void loadMenusFromCSV() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(MENUS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            String restaurantUsername = parts[0];
            String itemName = parts[1];
            double price = Double.parseDouble(parts[2]);

            for (User user : allUsers) {
                if (user instanceof RestaurantUser && user.getUserName().equals(restaurantUsername)) {
                    ((RestaurantUser) user).addMenuItem(itemName, price);
                    break;
                }
            }
        }
        reader.close();
    }

    private static void loadOrdersFromCSV() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(ORDERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            int orderId = Integer.parseInt(parts[0]);
            String orderDate = parts[1];
            String itemsString = parts[2];
            double totalPrice = Double.parseDouble(parts[3]);
            String customerName = parts[4];
            String restaurantName = parts[5];
            String status = parts[6];
            String customerNote = parts[7];

            List<Order.Item> items = parseItems(itemsString);
            Order order = new Order(orderId, Timestamp.valueOf(orderDate), items, customerName, restaurantName, status, customerNote);

            for (User user : allUsers) {
                if (user.getUserName().equals(customerName)) {
                    if (user instanceof CustomerUser) {
                        ((CustomerUser) user).addOrder(order);
                    } else if (user instanceof RestaurantUser) {
                        ((RestaurantUser) user).addOrder(order);
                    }
                }
            }
        }
        reader.close();
    }

    static List<Order.Item> parseItems(String itemsString) {
        List<Order.Item> items = new ArrayList<>();
        String[] itemArray = itemsString.split(";");
        for (String item : itemArray) {
            String[] parts = item.split(":");
            String itemName = parts[0];
            double price = Double.parseDouble(parts[1]);
            items.add(new Order.Item(itemName, price));
        }
        return items;
    }

    public static void addUser(User user) throws IOException {
        allUsers.add(user);
        BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true));
        if (user instanceof CustomerUser) {
            writer.write("Customer," + user.getUserName() + "," + user.getHashedPassword() + "," +
                    user.getAddress() + "," + user.getPhoneNumber() + "," + user.getEmail() + "\n");
        } else if (user instanceof RestaurantUser) {
            writer.write("Restaurant," + user.getUserName() + "," + user.getHashedPassword() + "," +
                    user.getAddress() + "," + user.getPhoneNumber() + "," + user.getEmail() + "," +
                    ((RestaurantUser) user).getBusinessPhoneNumber() + "," + ((RestaurantUser) user).getCuisine() + "\n");
            saveMenu((RestaurantUser) user);
        }
        writer.close();
    }

    static void saveMenu(RestaurantUser restaurant) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(MENUS_FILE, true));
        for (Order.Item item : restaurant.getMenu()) {
            writer.write(restaurant.getUserName() + "," + item.getName() + "," + item.getPrice() + "\n");
        }
        writer.close();
    }

    public static void saveOrder(Order order) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ORDERS_FILE, true));
        writer.write(order.getOrderId() + "," + order.getOrderDate() + "," + formatItems(order.getItems()) + "," +
                order.getTotalPrice() + "," + order.getCustomerName() + "," + order.getRestaurantName() + "," +
                order.getStatus() + "," + order.getCustomerNote() + "\n");
        writer.close();
    }

    private static String formatItems(List<Order.Item> items) {
        StringBuilder sb = new StringBuilder();
        for (Order.Item item : items) {
            sb.append(item.getName()).append(":").append(item.getPrice()).append(";");
        }
        return sb.toString();
    }

    public static void addLoggedInRestaurant(RestaurantUser restaurant) {
        loggedInRestaurants.add(restaurant);
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                // Handle request here
                // For example: parse JSON, process request, and send response
                out.println("Response: " + inputLine); // Echo back the input for demonstration
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
}
