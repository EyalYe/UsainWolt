package Server.App;

import Server.Models.*;
import Server.Utilities.ImageServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerApp {
    public static final String USERS_FILE = "server_logs/users.csv";
    private static final String[] RESTAURANT_CUISINES = {"All", "American", "Chinese", "Italian", "Japanese", "Mexican", "Thai", "Israeli", "Indian"};
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 12345;
    public static final int IMAGE_SERVER_PORT = 8080;

    public static List<User> allUsers = new ArrayList<>();
    public static List<RestaurantUser> loggedInRestaurants = new ArrayList<>();

    public static void createFileIfNotExists(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
            System.out.println(fileName + " created.");
        }
    }

    public static void loadUsersFromCSV() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Customer")) {
                allUsers.add(new CustomerUser(line));
            } else if (line.startsWith("Restaurant")) {
                allUsers.add(new RestaurantUser(line));
            } else if (line.startsWith("Delivery")) {
                allUsers.add(new DeliveryUser(line));
            } else if (line.startsWith("Admin")) {
                allUsers.add(new AdminUser(line));
            }
        }
        reader.close();
    }

    public static void loadMenusFromCSV() throws IOException {
        File menuDirectory = new File("menu_data");
        if (!menuDirectory.exists() || !menuDirectory.isDirectory()) {
            System.out.println("No menu directory found. Skipping menu loading.");
            return;
        }

        // Loop through each file in the menu_data directory
        File[] menuFiles = menuDirectory.listFiles((dir, name) -> name.endsWith(".csv"));
        if (menuFiles == null) {
            System.out.println("No menu files found.");
            return;
        }

        for (File menuFile : menuFiles) {
            String restaurantUsername = menuFile.getName().replace(".csv", ""); // Extract restaurant username from the filename

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
                String line;
                while ((line = reader.readLine()) != null) {
                    // Each line is expected to represent an item in the menu
                    String[] parts = line.split(";");
                    restaurantUser.addMenuItem(new Order.Item(parts[0], Double.parseDouble(parts[1]), SERVER_IP+":"+IMAGE_SERVER_PORT+"/menu_item_images/" + restaurantUsername + "_" + parts[0] + ".jpg", parts[4],  true));
                }
            }
        }
    }

    public static void addUser(User user) throws IOException {
       if (checkIfUserExists(user.getUserName())) {
              throw new IllegalArgumentException("User already exists");
         }
          allUsers.add(user);
          updateUser(user, true);
    }

    public static boolean checkIfUserExists(String userName) {
        for (User user : allUsers) {
            if (user.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public static void updateUser(User user, boolean isNew) throws IOException {
        if (isNew) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
                writer.write(user.toString() + "\n");
            }
        } else {
            updateUserInCSV(user);
        }
    }

    static void saveMenu(RestaurantUser restaurant) throws IOException {
        File directory = new File("menu_data");
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }

        File menuFile = new File(directory, restaurant.getUserName() + ".csv");

        // Writing the restaurant's menu to its dedicated file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(menuFile))) {
            for (Order.Item item : restaurant.getMenu()) {
                writer.write(item.toString() + "\n");
            }
        }
    }

    public static void addLoggedInRestaurant(RestaurantUser restaurant) {
        loggedInRestaurants.add(restaurant);
    }

    public static String getAvailableCuisines() {
        return String.join(",", RESTAURANT_CUISINES);
    }

    public static void updateUserInCSV(User user) {
        try {
            // Read all existing users into memory
            List<String> lines = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[1].equals(user.getUserName())) {
                    lines.add(user.toString()); // Replace the line with the updated user
                } else {
                    lines.add(line); // Keep the line unchanged
                }
            }
            reader.close();

            // Write the updated list back to the CSV file
            BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE));
            for (String updatedLine : lines) {
                writer.write(updatedLine + "\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return updateOrderInCSV(order);
    }

    static boolean updateOrderInCSV(Order order) {
        String status = order.getStatus();
        String restaurantPath = "restaurant_orders/" + order.getRestaurantName() + ".csv";
        String customerPath = "customer_orders/" + order.getCustomerName() + ".csv";
        String deliveredPath = "delivery_orders/" + order.getDeliveryPerson() + ".csv";
        String readyForPickupPath = "delivery_orders/Ready For Pickup.csv";
        File restaurantFile = new File(restaurantPath);
        File deliveredFile = new File(deliveredPath);
        File customerFile = new File(customerPath);
        File readyForPickupFile = new File(readyForPickupPath);
        switch (status) {
            case "Pending":
                addLineNotExists(restaurantFile, order);
                return addLineNotExists(customerFile, order);
            case "Ready For Pickup":
                removeLineIfExists(restaurantFile, order);
                removeLineIfExists(customerFile, order);
                addLineNotExists(customerFile, order);
                return addLineNotExists(readyForPickupFile, order);
            case "Delivered":
                removeLineIfExists(restaurantFile, order);
                removeLineIfExists(customerFile, order);
                removeLineIfExists(deliveredFile, order);
                return addLineNotExists(customerFile, order);
            case "Cancelled":
                removeLineIfExists(restaurantFile, order);
                removeLineIfExists(customerFile, order);
                removeLineIfExists(deliveredFile, order);
                return addLineNotExists(customerFile, order);
            default:
                return false;
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

    public static Object getOrdersFromCSV(String directory, String username) {
        List<Order> orders = new ArrayList<>();
        File file = new File(directory + "/" + username + ".csv");
        if (!file.exists()) {
            return orders;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                orders.add(new Order(line));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public static void updateUsersCSV() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE));
            for (User user : allUsers) {
                writer.write(user.toString() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}