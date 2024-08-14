package Server.Models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RestaurantUser extends User {
    private List<Order> orders;
    private List<Order.Item> menu;
    private String businessPhoneNumber;
    private String cuisine;
    private double revenue;
    private boolean hasProfilePicture;
    private String profilePictureUrl;
    private String restaurantName;


    // Constructor for RestaurantUser
    public RestaurantUser(String userName, String hashedPassword, String address, String phoneNumber, String email, String businessPhoneNumber, String cuisine, double revenue) {
        super(userName, hashedPassword, address, phoneNumber, email);
        this.orders = new ArrayList<>();
        this.menu = new ArrayList<>();
        this.businessPhoneNumber = businessPhoneNumber;
        this.cuisine = cuisine;
        this.revenue = revenue;
        this.hasProfilePicture = false;
        this.profilePictureUrl = "";
        this.restaurantName = this.getUserName().replaceAll("([A-Z])", " $1").trim();

    }

    // Constructor to create RestaurantUser from CSV line
    public RestaurantUser(String csvLine) {
        super(csvLine);
        int index = 8;
        String[] fields = csvLine.split(",");
        this.businessPhoneNumber = fields[index++];
        this.cuisine = fields[index++];
        try {
            this.revenue = Double.parseDouble(fields[index++]);
        } catch (NumberFormatException e) {
            this.revenue = 0.0;
        }
        this.hasProfilePicture = Boolean.parseBoolean(fields[index++]);

        try {
            File profilePicture = new File("profile_pictures/" + this.getUserName() + ".jpg");
            if (!profilePicture.exists()) {
                this.hasProfilePicture = false;
            } else {
                this.profilePictureUrl = "profile_pictures/" + this.getUserName() + ".jpg";
                this.hasProfilePicture = true;
            }
        } catch (Exception e) {
            this.hasProfilePicture = false;
        }
        this.restaurantName = fields[index];

        this.orders = new ArrayList<>();
        this.menu = new ArrayList<>();

    }


    // Getters and Setters

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setOrders(List<Order> orders) {
        this.orders = new ArrayList<>(orders);
    }

    public List<Order.Item> getMenu() {
        return new ArrayList<>(menu);
    }

    public void setMenu(List<Order.Item> menu) {
        this.menu = new ArrayList<>(menu);
    }

    public String getBusinessPhoneNumber() {
        return businessPhoneNumber;
    }

    public void setBusinessPhoneNumber(String businessPhoneNumber) {
        this.businessPhoneNumber = businessPhoneNumber;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public boolean hasProfilePicture() {
        return hasProfilePicture;
    }

    public void setProfilePicture(byte[] hasProfilePicture) {
        this.hasProfilePicture = true;
        this.profilePictureUrl = "profile_pictures/" + this.getUserName() + ".jpg";
    }

    @Override
    public void performUserSpecificAction() {
        // Implementation for restaurant-specific action
        System.out.println("Performing restaurant-specific action...");
    }

    // Additional methods specific to RestaurantUser

    public void addMenuItem(String itemName, double price) {
        if (itemName == null || itemName.isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        Order.Item item = new Order.Item(itemName, price);
        menu.add(item);
    }

    public void addMenuItem(Order.Item item) {
        menu.add(item);
    }

    public void removeMenuItem(String itemName) {
        menu.removeIf(item -> item.getName().equals(itemName));
    }

    public void disableMenuItem(String menuItemName) {
        for (Order.Item item : menu) {
            if (item.getName().equals(menuItemName)) {
                item.setAvailable(false);
                break;
            }
        }
    }

    public void enableMenuItem(String menuItemName) {
        for (Order.Item item : menu) {
            if (item.getName().equals(menuItemName)) {
                item.setAvailable(true);
                break;
            }
        }
    }

    public void disableAllMenuItems() {
        for (Order.Item item : menu) {
            item.setAvailable(false);
        }
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(int orderId) {
        orders.removeIf(order -> order.getOrderId() == orderId);
    }

    public List<Order> getCurrentOrders() {
        return orders;
    }

    @Override
    public String toString() {
        return "Restaurant," + super.toString() + "," + businessPhoneNumber + "," + cuisine + "," + revenue + "," + hasProfilePicture + "," + restaurantName;
    }

    public void addRevenue(double totalPrice) {
        revenue += totalPrice;
    }

    public void setRestaurantName(String value) {
        this.restaurantName = value;
    }

    public Object getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantActualName(String value) {
        this.restaurantName = value;
    }
}
