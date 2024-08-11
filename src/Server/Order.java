package Server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Order {
    private int orderId;
    private Date orderDate;
    private List<Item> items;
    private double totalPrice;
    private String customerName;
    private String restaurantName;
    private String status; // e.g., "Pending", "In Progress", "Delivered"
    private String customerNote;

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    // Inner class to represent an item with its price
    public static class Item {
        private String name;
        private double price;
        private boolean available;

        public Item(String name, double price) {
            this.name = name;
            this.price = price;
            this.available = true;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return name + " ($" + price + ")";
        }

        public void setAvailable(boolean b) {
            this.available = b;
        }
    }

    public Order(int orderId, Date orderDate, List<Item> items, String customerName, String restaurantName, String status, String customerNote) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.items = new ArrayList<>(items);
        this.totalPrice = calculateTotalPrice();
        this.customerName = customerName;
        this.restaurantName = restaurantName;
        this.status = status;
        this.customerNote = customerNote;
    }

    // Getters and Setters

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<Item> items) {
        this.items = new ArrayList<>(items);
        this.totalPrice = calculateTotalPrice();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Additional methods

    public void addItem(String itemName, double price) {
        if (itemName == null || itemName.isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        Item item = new Item(itemName, price);
        items.add(item);
        totalPrice += price;
    }

    public void removeItem(String itemName) {
        Item itemToRemove = null;
        for (Item item : items) {
            if (item.getName().equals(itemName)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            items.remove(itemToRemove);
            totalPrice -= itemToRemove.getPrice();
        }
    }

    public void printOrder() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Order Date: " + orderDate);
        System.out.println("Customer: " + customerName);
        System.out.println("Restaurant: " + restaurantName);
        System.out.println("Items: " + items);
        System.out.println("Total Price: $" + totalPrice);
        System.out.println("Status: " + status);
        System.out.println("Customer Note: " + customerNote);
    }

    public List<Item> searchItems(String keyword) {
        return items.stream()
                .filter(item -> item.getName().contains(keyword))
                .collect(Collectors.toList());
    }

    public void sortItems() {
        items.sort((item1, item2) -> item1.getName().compareTo(item2.getName()));
    }

    private double calculateTotalPrice() {
        return items.stream().mapToDouble(Item::getPrice).sum();
    }

    // Main method for testing purposes
    public static void main(String[] args) {
        List<Item> initialItems = List.of(new Item("Pizza", 12.99), new Item("Burger", 8.99), new Item("Salad", 5.99));
        Order order = new Order(1, new Date(), initialItems, "John Doe", "Fast Food Inc.", "Pending", "Extra ketchup");

        // Print initial order
        order.printOrder();

        // Add item
        order.addItem("Soda", 2.50);
        order.printOrder();

        // Remove item
        order.removeItem("Burger");
        order.printOrder();

        // Search items
        System.out.println("Search results: " + order.searchItems("Sa"));

        // Sort items
        order.sortItems();
        order.printOrder();
    }
}

