package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class RestaurantUser extends User {
    private List<Order> orders;
    private List<Order.Item> menu;
    private String businessPhoneNumber;
    private String cuisine;
    private double revenue;

    public RestaurantUser(String userName, String hashedPassword, String address, String phoneNumber, String email, String businessPhoneNumber, String cuisine, double revenue) {
        super(userName, hashedPassword, address, phoneNumber, email);
        this.orders = new ArrayList<>();
        this.menu = new ArrayList<>();
        this.businessPhoneNumber = businessPhoneNumber;
        this.cuisine = cuisine;
        this.revenue = revenue;
    }

    // Getters and Setters

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
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

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getBusinessPhoneNumber() {
        return businessPhoneNumber;
    }

    public void setBusinessPhoneNumber(String businessPhoneNumber) {
        this.businessPhoneNumber = businessPhoneNumber;
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

    public void removeMenuItem(String itemName) {
        menu.removeIf(item -> item.getName().equals(itemName));
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(int orderId) {
        orders.removeIf(order -> order.getOrderId() == orderId);
    }

    // Main method for testing purposes
    public static void main(String[] args) {
        RestaurantUser restaurant = new RestaurantUser("restaurant_owner", "hashed_password", "456 Elm St", "555-5678", "restaurant@example.com", "555-5678", "Fast Food", 1000.0);
        restaurant.addMenuItem("Burger", 8.99);
        restaurant.addMenuItem("Salad", 5.99);

        // Create an order and add it to the restaurant's orders
        Order order = new Order(1, new Date(), List.of(new Order.Item("Burger", 8.99)), "John Doe", "Fast Food Inc.", "Pending", "Extra ketchup");
        restaurant.addOrder(order);

        restaurant.performUserSpecificAction();
        System.out.println("Menu: " + restaurant.getMenu());
        System.out.println("Orders: " + restaurant.getOrders());
    }

    public void disableAllMenuItems() {
        for (Order.Item item : menu) {
            item.setAvailable(false);
        }
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

    public List<Order> getCurrentOrders() {
        return orders;
    }
}
