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
    private static final String[] RESTAURANT_CUISINES = {"American", "Chinese", "Italian", "Japanese", "Mexican", "Thai", "Israeli", "Indian","All"};
    public static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    public static final int IMAGE_SERVER_PORT = 8080;

    public static List<User> allUsers = new ArrayList<>();
    public static List<RestaurantUser> loggedInRestaurants = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Start the image server in a new thread
            new Thread(() -> {
                try {
                    ImageServer.startServer(IMAGE_SERVER_PORT); // Start the image server
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Create the files if they don't exist
            createFileIfNotExists(USERS_FILE);
            createFileIfNotExists(MENUS_FILE);
            createFileIfNotExists(ORDERS_FILE);

            // Load data from CSV files
            loadUsersFromCSV();
            loadMenusFromCSV();
            loadOrdersFromCSV();

            // Set up server socket
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server is listening on port 12345");

            // Server loop to handle clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // Create a new thread to handle the client using ClientHandler
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
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
            if (line.startsWith("Customer")) {
                allUsers.add(new CustomerUser(line));
            } else if (line.startsWith("Restaurant")) {
                allUsers.add(new RestaurantUser(line));
            }
        }
        reader.close();
    }

    private static void loadMenusFromCSV() throws IOException {
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
                    restaurantUser.addMenuItem(new Order.Item(line));
                }
            }
        }
    }

    private static void loadOrdersFromCSV() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(ORDERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            Order order = new Order(line);
            for (User user : allUsers) {
                if (user.getUserName().equals(order.getCustomerName())) {
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

    public static void addUser(User user) throws IOException {
        allUsers.add(user);
        BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true));
        writer.write(user.toString() + "\n");
        writer.close();

        if (user instanceof RestaurantUser) {
            saveMenu((RestaurantUser) user);
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



    public static void saveOrder(Order order) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ORDERS_FILE, true));
        writer.write(order.toString() + "\n");
        writer.close();
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
}
