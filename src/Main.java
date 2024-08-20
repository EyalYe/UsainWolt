import Client.network.ClientApp;
import Server.ServerMain;
import Client.UsainWoltMain;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 12345;
    public static final int RESTAURANTS_INSTANCES = 3;
    public static final int CUSTOMERS_INSTANCES = 3;
    public static final int DELIVERIES_INSTANCES = 3;

    public static void main(String[] args) {

        Thread serverThread = new Thread(() -> {
            System.out.println("Thread " + Thread.currentThread().getName() + " running server");
            ServerMain.main(args);
        });
        serverThread.start();
        for (int i = 0; i < RESTAURANTS_INSTANCES; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                System.out.println("Thread " + Thread.currentThread().getName() + " running restaurant user " + finalI);
                UsainWoltMain.skipLogin("restaurant" + (new Random().nextInt(25) + 1), "password", "restaurant");
            });
            thread.start();
        }
        for (int i = 0; i < CUSTOMERS_INSTANCES; i++) {
            int finalI1 = i;
            Thread thread = new Thread(() -> {
                System.out.println("Thread " + Thread.currentThread().getName() + " running customer user " + finalI1);
                UsainWoltMain.skipLogin("customer" + (new Random().nextInt(25) + 1), "password", "customer");
            });
            thread.start();
        }
        for (int i = 0; i < DELIVERIES_INSTANCES; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                System.out.println("Thread " + Thread.currentThread().getName() + " running delivery user " + finalI);
                UsainWoltMain.skipLogin("delivery" + (new Random().nextInt(4) + 1), "password", "delivery");
            });
            thread.start();
        }
        try {
            // Create an instance of ClientApp
            ClientApp clientApp = new ClientApp("localhost", 12345);

            // 25 restaurant usernames, all with the same password ("password")
            for (int i = 0; i < 25; i++) {
                String restaurantUsername = "restaurant" + (i + 1);
                String restaurantPassword = "password";  // Consistent password

                // Log in to the restaurant account
                Map<String, Object> loginResponse = clientApp.loginAsync(restaurantUsername, restaurantPassword);
                System.out.println("Login Response for " + restaurantUsername + ": " + loginResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static  void oldMain(String[] args) {
        try {
            // Create an instance of ClientApp
            ClientApp clientApp = new ClientApp("localhost", 12345);

            String[] streetNames = {"Hertzel", "Ben Gurion", "Dizengoff", "Rothschild", "Allenby", "King George", "HaYarkon", "HaShalom", "HaArbaa", "HaMaccabi"};
            String[] cities = {"Tel Aviv", "Jerusalem", "Haifa", "Rishon LeZion", "Petah Tikva", "Ashdod", "Netanya", "Beer Sheva", "Holon", "Bnei Brak"};
            String[] fictionalRestaurantNames = {
                    "The Golden Spoon", "Urban Bites", "Spice Junction", "Bistro Verde", "The Hungry Owl",
                    "Savory Street", "Blue Horizon Grill", "Rustic Roots Café", "Citrus & Thyme", "Ember & Smoke",
                    "The Cozy Plate", "The Velvet Fork", "Starlight Diner", "Terra Cotta Kitchen", "Luna's Bistro",
                    "The Sizzle Spot", "The Green Olive", "Cloud 9 Eatery", "Ocean Breeze Café", "Firewood Grill",
                    "Harvest Table", "Midnight Snack Shack", "Scarlet Maple", "Driftwood Tavern", "The Copper Pot"
            };
            String[] foodTypes = {"American", "Chinese", "Italian", "Japanese", "Mexican", "Thai", "Israeli", "Indian"};
            String[] itemNames = {"Pizza", "Special Burger", "Pasta", "Sushi Roll", "Tacos", "Salad", "Fried Chicken", "Steak",
                    "Ice Cream", "Fish and Chips", "Pad Thai", "Burrito", "Falafel", "Soup", "Ramen", "Donuts",
                    "Lasagna", "BBQ Ribs", "Dim Sum", "Spring Rolls", "Waffles", "Crepes", "Shawarma", "Nachos",
                    "Hot Dog", "Bruschetta", "Curry", "Risotto", "Peking Duck", "Dumplings", "Pancakes", "Spaghetti",
                    "French Fries", "Churros", "Tiramisu", "Smoothie"};

            Random random = new Random();

            // Create a CSV writer
            FileWriter csvWriter = new FileWriter("users.csv");
            csvWriter.append("Username,Password,Role,Location,RestaurantName\n");

            // Create 25 customers and 25 restaurants
            for (int i = 0; i < 25; i++) {
                String customerUsername = "customer" + (i + 1);
                String customerPassword = "password";
                String address = streetNames[random.nextInt(streetNames.length)] + " St, " + cities[random.nextInt(cities.length)];
                String phoneNumber = "050" + (1000000 + random.nextInt(8999999));
                String email = customerUsername + "@example.com";

                // Sign up customer
                Map<String, Object> signupResponse = clientApp.signupCustomerAsync(customerUsername, customerPassword, address, phoneNumber, email);
                System.out.println("Customer Signup Response: " + signupResponse);

                // Write customer details to CSV
                csvWriter.append(customerUsername + ",password,customer," + address + ",\n");
            }

            for (int i = 0; i < 25; i++) {
                String restaurantUsername = "restaurant" + (i + 1);
                String restaurantPassword = "password";
                String address = streetNames[random.nextInt(streetNames.length)] + " St, " + cities[random.nextInt(cities.length)];
                String phoneNumber = "050" + (1000000 + random.nextInt(8999999));
                String email = restaurantUsername + "@example.com";
                String businessPhoneNumber = "03" + (5000000 + random.nextInt(999999));
                String cuisine = foodTypes[random.nextInt(foodTypes.length)]; // Randomized cuisine

                // Sign up restaurant
                Map<String, Object> signupResponse = clientApp.signupRestaurantAsync(restaurantUsername, restaurantPassword, address, phoneNumber, email, businessPhoneNumber, cuisine);
                System.out.println("Restaurant Signup Response: " + signupResponse);

                // Assign a random fictional restaurant name
                String fictionalName = fictionalRestaurantNames[random.nextInt(fictionalRestaurantNames.length)];
                System.out.println("Restaurant " + restaurantUsername + " will be called: " + fictionalName);

                // Add 6 meaningful menu items to the restaurant
                for (int j = 0; j < 6; j++) {
                    String itemName = itemNames[random.nextInt(itemNames.length)];
                    double price = 10.0 + j;
                    String description = "Delicious " + itemName;
                    File itemImage = new File("temp/item" + (random.nextInt(36) + 1) + ".jpg");

                    // Add each item to the menu
                    Map<String, Object> updateMenuResponse = clientApp.updateMenuAsync(restaurantUsername, restaurantPassword, fictionalName, itemName, price, description, itemImage, "add");
                    System.out.println("Add Menu Item Response: " + updateMenuResponse);
                }

                // Update profile picture for the restaurant
                File profilePicture = new File("temp/profile_picture_" + (i + 1) + ".jpg");
                Map<String, Object> updateProfilePictureResponse = clientApp.updateProfilePictureAsync(restaurantUsername, restaurantPassword, profilePicture);
                System.out.println("Update Profile Picture Response: " + updateProfilePictureResponse);

                // Write restaurant details to CSV
                csvWriter.append(restaurantUsername + ",password,restaurant," + address + "," + fictionalName + "\n");
            }

            for (int i = 0; i < 4; i++){
                String customerUsername = "delivery" + (i + 1);
                String customerPassword = "password";
                String address = streetNames[random.nextInt(streetNames.length)] + " St, " + cities[random.nextInt(cities.length)];
                String phoneNumber = "050" + (1000000 + random.nextInt(8999999));
                String email = customerUsername + "@example.com";

                // Sign up delivery
                Map<String, Object> signupResponse = clientApp.signupDeliveryAsync(customerUsername, customerPassword, address, phoneNumber, email, "A1234");
                System.out.println("Delivery Signup Response: " + signupResponse);

                // Write delivery details to CSV
                csvWriter.append(customerUsername + ",password,delivery," + address + ",\n");
            }

            csvWriter.flush();
            csvWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
