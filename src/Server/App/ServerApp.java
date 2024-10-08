package Server.App;

import Server.Models.*;
import Server.Utilities.CustomDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;




// Group: 6
public class ServerApp {
    // Constants and configuration settings
    public static final String USERS_PATH = "server_logs/users";
    private static final String[] RESTAURANT_CUISINES = {"All", "American", "Chinese", "Italian", "Japanese", "Mexican", "Thai", "Israeli", "Indian"};
    public static final double DELIVERY_FEE = 5.0;
    public static List<Order> pending = new ArrayList<>();
    public static List<Order> readyForPickupOrders = new ArrayList<>();
    public static final String DATE_FORMAT = "MMM dd, yyyy, hh:mm:ss a";  // Matches 'Aug 18, 2024, 10:27:06 PM'
    public static Gson gson = gsonCreator();

    public static List<User> allUsers = new CopyOnWriteArrayList<>();
    public static List<Map<RestaurantUser,Socket>> loggedInRestaurants = new CopyOnWriteArrayList<>();

    // Creates a file if it doesn't exist
    public static void createFileIfNotExists(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
            System.out.println(fileName + " created.");
        }
    }

    // Cleans up logged-in restaurants list by removing restaurants with closed sockets
    public static void cleanUpLoggedInRestaurants() {
        loggedInRestaurants.removeIf(entry -> {
            RestaurantUser restaurant = entry.keySet().iterator().next();
            Socket socket = entry.get(restaurant);
            return socket.isClosed();
        });
    }

    // Configures and returns a Gson instance with custom date formatting
    public static Gson gsonCreator() {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new CustomDateAdapter())  // Use the custom adapter
                .create();
    }

    // Loads users from JSON files and initializes the server with them
    public static void loadUsersFromJSON() throws IOException {
        File usersDirectory = new File("server_logs/users");
       try {
            if (!usersDirectory.exists()) {
                usersDirectory.mkdirs();
            }
            File[] userFiles = usersDirectory.listFiles((dir, name) -> name.endsWith(".json"));
            if (userFiles == null) {
                return;
            }
            for (File userFile : userFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                    StringBuilder jsonUser = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonUser.append(line);
                    }
                    String name = userFile.getName();
                    if (name.contains("RestaurantUser")) {
                        RestaurantUser restaurantUser = gson.fromJson(jsonUser.toString(), RestaurantUser.class);
                        allUsers.add(restaurantUser);
                        for (Order order : restaurantUser.getOrders()) {
                            if (order.getStatus().equals("Pending")) {
                                pending.add(order);
                            } else if (order.getStatus().equals("Ready For Pickup")) {
                                readyForPickupOrders.add(order);
                            }
                        }
                    } else if (name.contains("CustomerUser")) {
                        CustomerUser customerUser = gson.fromJson(jsonUser.toString(), CustomerUser.class);
                        allUsers.add(customerUser);
                    } else if (name.contains("DeliveryUser")) {
                        DeliveryUser deliveryUser = gson.fromJson(jsonUser.toString(), DeliveryUser.class);
                        allUsers.add(deliveryUser);
                    }
                    else {
                        System.out.println("Unknown user type: " + name);
                    }
                }
            }
        } catch (IOException e) {
           e.printStackTrace();
       }
    }

    // Loads menus from JSON files into corresponding restaurant users
    public static void loadMenusFromJSON() throws IOException {
        File menuDirectory = new File("menu_data");
        if (!menuDirectory.exists() || !menuDirectory.isDirectory()) {
            System.out.println("No menu directory found. Skipping menu loading.");
            return;
        }

        // Loop through each file in the menu_data directory
        File[] menuFiles = menuDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        if (menuFiles == null) {
            System.out.println("No menu files found.");
            return;
        }

        for (File menuFile : menuFiles) {
            String restaurantUsername = menuFile.getName().replace(".json", "");

            // Find the corresponding RestaurantUser
            RestaurantUser restaurantUser = null;
            for (User user : allUsers) {
                if (user instanceof RestaurantUser && user.getUserName().equals(restaurantUsername)) {
                    restaurantUser = (RestaurantUser) user;
                    break;
                }
            }

            if (restaurantUser == null) {
                System.out.println("Restaurant user " + restaurantUsername + " not found. Skipping file " + menuFile.getName());
                continue;
            }

            // Read and load the menu items from the file
            try (BufferedReader reader = new BufferedReader(new FileReader(menuFile))) {
                StringBuilder jsonMenu = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonMenu.append(line);
                }
                Type menuType = new TypeToken<List<Order.Item>>() {}.getType();
                restaurantUser.setMenu(gson.fromJson(jsonMenu.toString(), menuType));
            }
        }
    }

    // Adds a new user if they don't already exist and saves their data
    public static void addUser(User user) throws IOException {
       if (checkIfUserExists(user.getUserName())) {
              throw new IllegalArgumentException("User already exists");
         }
          allUsers.add(user);
          updateUser(user);
    }

    // Checks if a user with a given username already exists
    public static boolean checkIfUserExists(String userName) {
        for (User user : allUsers) {
            if (user.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    // Updates the JSON file for a given user
    public static void updateUser(User user) throws IOException {
       File usersDirectory = new File("server_logs/users");
        if (!usersDirectory.exists()) {
            usersDirectory.mkdirs();
        }
        File userFile = new File(usersDirectory, user.getUserName() + "." + String.valueOf(user.getClass().getSimpleName()) + ".json");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
            writer.write(gson.toJson(user));
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Saves a restaurants menu to a JSON file
    static void saveMenu(RestaurantUser restaurant) throws IOException {
        File directory = new File("menu_data");
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }
        String jsonMenu = gson.toJson(restaurant.getMenu());

        File menuFile = new File(directory, restaurant.getUserName() + ".json");

        // Writing the restaurants menu to its dedicated file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(menuFile))) {
            writer.write(jsonMenu);
        }
    }

    // Adds a logged-in restaurant and its socket to the list
    public static void addLoggedInRestaurant(RestaurantUser restaurant, Socket socket) {
        if (isLogged(restaurant)) {
            loggedInRestaurants.removeIf(user -> user.containsKey(restaurant));
        }
        loggedInRestaurants.add(Map.of(restaurant, socket));
    }

    // Returns a comma-separated string of all available cuisines
    public static String getAvailableCuisines() {
        return String.join(",", RESTAURANT_CUISINES);
    }



    // Saves an order and updates its status
    public static boolean saveOrder(Order order) throws IOException {
        return updateOrder(order);
    }

    // Updates an orders status and moves it between lists accordingly
    static boolean updateOrder(Order order) throws IOException {
        String status = order.getStatus();
        System.out.println("Updating order " + order.getOrderId() + " to status " + status);
        String restaurantName = order.getRestaurantName();
        String customerName = order.getCustomerName();
        String deliveryName = order.getDeliveryPerson();
        RestaurantUser restaurant = null;
        CustomerUser customer = null;
        DeliveryUser delivery = null;
        for (User user : allUsers) {
            if (user instanceof RestaurantUser && user.getUserName().equals(restaurantName)) {
                restaurant = (RestaurantUser) user;
            } else if (user instanceof CustomerUser && user.getUserName().equals(customerName)) {
                customer = (CustomerUser) user;
            } else if (user instanceof DeliveryUser && user.getUserName().equals(deliveryName)) {
                delivery = (DeliveryUser) user;
            }
        }
        if (restaurant == null || customer == null) {
            return false;
        }
        if (delivery != null) {
            delivery.setCurrentOrder(order);
        }
        customer.removeOrder(order.getOrderId());
        customer.addOrder(order);
        switch (status) {
            case "Pending":
                if (!pending.contains(order)) {
                    pending.add(order);
                }
                updateAllUsers();
                return pending.contains(order);
            case "Ready For Pickup":
                pending.remove(order);
                if (!readyForPickupOrders.contains(order)) {
                    readyForPickupOrders.add(order);
                }
                updateAllUsers();
                return readyForPickupOrders.contains(order);
            case "Picked Up":
                readyForPickupOrders.remove(order);
                updateAllUsers();
                return !readyForPickupOrders.contains(order);
            case "Delivered":
                readyForPickupOrders.remove(order);
                if (delivery != null)
                    delivery.setCurrentOrder(null);
                updateAllUsers();
                return !readyForPickupOrders.contains(order);
            case "Cancelled":
                readyForPickupOrders.remove(order);
                pending.remove(order);
                if (delivery != null)
                    delivery.setCurrentOrder(null);
                updateAllUsers();
                return !readyForPickupOrders.contains(order) && !pending.contains(order);
            default:
                return false;
        }
    }

    // Updates JSON files for all users
    public static void updateAllUsers() throws IOException {
        for (User user : allUsers) {
            updateUser(user);
        }
    }

    private static boolean addLineNotExists(File file, Order order) {
        boolean exists = false;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                Order currentOrder = new Order(currentLine);
                if (currentOrder.getOrderId() == order.getOrderId()) {
                    exists = true;
                    break;
                }
            }
            reader.close();
            if (!exists) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                writer.write(order.toString() + "\n");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return !exists;
    }

    private static boolean removeLineIfExists(File file, Order order) {
        boolean exists = false;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine;
            List<String> lines = new ArrayList<>();
            while ((currentLine = reader.readLine()) != null) {
                Order currentOrder = new Order(currentLine);
                if (!(currentOrder.getOrderId() == order.getOrderId())) {
                    lines.add(currentLine);
                } else {
                    exists = true;
                }
            }
            reader.close();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return !exists;
    }

    // Removes a user from the server and deletes their JSON file
    public static void removeUser(User user) {
        allUsers.remove(user);
        File userFile = new File("server_logs/users/" + user.getUserName() + "." + user.getClass().getSimpleName() + ".json");
        if (userFile.exists()) {
            userFile.delete();
        }
    }

    // Retrieves orders that are ready for pickup
    public static Order[] getReadyForPickupOrders() {
        return readyForPickupOrders.toArray(new Order[0]);
    }

    // Finds and returns an order by its ID
    public static Order getOrderById(int orderId) {
        for (Order order : readyForPickupOrders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        for (Order order : pending) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    // Checks if a restaurant user is currently logged in
    public static boolean isLogged(RestaurantUser restaurantUser) {
        for (Map<RestaurantUser, Socket> user : loggedInRestaurants) {
            if (user.containsKey(restaurantUser)) {
                return true;
            }
        }
        return false;
    }

    // Returns a list of all logged-in restaurant users
    public static List<RestaurantUser> getLoggedInRestaurants() {
        List<RestaurantUser> restaurants = new ArrayList<>();
        for (Map<RestaurantUser, Socket> user : loggedInRestaurants) {
            restaurants.add(user.keySet().iterator().next());
        }
        return restaurants;
    }

    // Retrieves the socket for a logged-in restaurant user
    public static Socket getRestaurantSocket(RestaurantUser restaurant) {
        for (Map<RestaurantUser, Socket> user : loggedInRestaurants) {
            if (user.containsKey(restaurant)) {
                return user.get(restaurant); // Return the associated socket
            }
        }
        return null; // Return null if not found
    }

    // Sends a message to a restaurant user through their socket
    public static void pushUpdateToRestaurant(RestaurantUser restaurant, String message) {
        Socket socket = getRestaurantSocket(restaurant);
        if (socket != null) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                Map<String,String> response = new java.util.HashMap<>();
                response.put("type", "update");
                response.put("success", "true");
                response.put("message", message);
                writer.println(gson.toJson(response));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Logs out a restaurant user by removing them from the logged-in list
    public static void logoutRestaurant(RestaurantUser userToDisconnect) {
        loggedInRestaurants.removeIf(user -> user.containsKey(userToDisconnect));
    }
}