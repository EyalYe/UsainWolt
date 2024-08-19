package Server.App;

import Server.Models.*;
import Server.Utilities.GeoLocationService;
import Server.Utilities.ImageServer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerApp {
    public static final String USERS_PATH = "server_logs/users";
    private static final String[] RESTAURANT_CUISINES = {"All", "American", "Chinese", "Italian", "Japanese", "Mexican", "Thai", "Israeli", "Indian"};
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 12345;
    public static final int IMAGE_SERVER_PORT = 8080;
    public static final double DELIVERY_FEE = 5.0;
    public static List<Order> pending = new ArrayList<>();
    public static List<Order> readyForPickupOrders = new ArrayList<>();
    public static Gson gson = new Gson();

    public static List<User> allUsers = new ArrayList<>();
    public static List<RestaurantUser> loggedInRestaurants = new ArrayList<>();

    public static void createFileIfNotExists(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
            System.out.println(fileName + " created.");
        }
    }

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
                        for (Order order : customerUser.getOrderHistory()) {
                             if (order.getStatus().equals("Ready For Pickup")) {
                                readyForPickupOrders.add(order);
                            }
                        }
                    } else if (name.contains("DeliveryUser")) {
                        DeliveryUser deliveryUser = gson.fromJson(jsonUser.toString(), DeliveryUser.class);
                        allUsers.add(deliveryUser);
                        if(deliveryUser.getCurrentOrder() != null) {
                            if (deliveryUser.getCurrentOrder().getStatus().equals("Pending")) {
                                pending.add(deliveryUser.getCurrentOrder());
                            } else if (deliveryUser.getCurrentOrder().getStatus().equals("Ready For Pickup")) {
                                readyForPickupOrders.add(deliveryUser.getCurrentOrder());
                            }
                        }
                    } else if (name.contains("AdminUser")) {
                        AdminUser adminUser = gson.fromJson(jsonUser.toString(), AdminUser.class);
                        allUsers.add(adminUser);
                    }
                    else {
                        System.out.println("Unknown user type: " + name);
                    }
                }
            }
        } catch (IOException e) {
           e.printStackTrace();
       }
       for (Order order : pending) {
            String restaurantName = order.getRestaurantName();
            for (User user : allUsers) {
                if (user instanceof RestaurantUser && user.getUserName().equals(restaurantName)) {
                    ((RestaurantUser) user).addOrder(order);
                    updateUser(user);
                    break;
                }
            }
        }
    }

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

    public static void addUser(User user) throws IOException {
       if (checkIfUserExists(user.getUserName())) {
              throw new IllegalArgumentException("User already exists");
         }
          allUsers.add(user);
          updateUser(user);
    }

    public static boolean checkIfUserExists(String userName) {
        for (User user : allUsers) {
            if (user.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

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

    static void saveMenu(RestaurantUser restaurant) throws IOException {
        File directory = new File("menu_data");
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }
        String jsonMenu = gson.toJson(restaurant.getMenu());

        File menuFile = new File(directory, restaurant.getUserName() + ".json");

        // Writing the restaurant's menu to its dedicated file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(menuFile))) {
            writer.write(jsonMenu);
        }
    }

    public static void addLoggedInRestaurant(RestaurantUser restaurant) {
        loggedInRestaurants.add(restaurant);
    }

    public static String getAvailableCuisines() {
        return String.join(",", RESTAURANT_CUISINES);
    }



    public static List<Order.Item> parseItems(String items) {
        List<Order.Item> itemList = new ArrayList<>();
        String[] itemStrings = items.split(";");
        for (String itemString : itemStrings) {
            itemList.add(new Order.Item(itemString));
        }
        return itemList;
    }

    // Order handling
    public static boolean saveOrder(Order order) throws IOException {
        return updateOrder(order);
    }

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
                pending.add(order);
                return pending.contains(order);
            case "Ready For Pickup":
                pending.remove(order);
                updateAllUsers();
                readyForPickupOrders.add(order);
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

    public static void removeUser(User user) {
        allUsers.remove(user);
        File userFile = new File("server_logs/users/" + user.getUserName() + "." + user.getClass().getSimpleName() + ".json");
        if (userFile.exists()) {
            userFile.delete();
        }
    }

    public static Order[] getReadyForPickupOrders() {
        return readyForPickupOrders.toArray(new Order[0]);
    }

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
}