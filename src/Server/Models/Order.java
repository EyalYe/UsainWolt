// Group: 6
package Server.Models;

import Server.Utilities.GeoLocationService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Order {
    private int orderId; // Unique identifier for the order
    private Date orderDate; // Date when the order was placed
    private List<Item> items; // List of items in the order
    private double totalPrice; // Total price of the order
    private String customerName; // Name of the customer who placed the order
    private String restaurantName; // Name of the restaurant fulfilling the order
    private String status; // Current status of the order (e.g., "Pending", "In Progress", "Delivered")
    private String customerNote; // Additional note from the customer
    private String address; // Delivery address
    private String restaurantAddress; // Address of the restaurant
    private String deliveryPerson; // Name of the delivery person (if assigned)
    private double distance; // Distance between restaurant and delivery address
    private double[] location = new double[2]; // Geographical location [latitude, longitude]

    // Constructor for creating an order with all details
    public Order(int orderId, Date orderDate, List<Item> items, String customerName, String restaurantName, String status, String customerNote, String address, String restaurantAddress) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.items = new ArrayList<>(items);
        this.totalPrice = calculateTotalPrice();
        this.customerName = customerName;
        this.restaurantName = restaurantName;
        this.status = status;
        this.customerNote = safeString(customerNote);
        this.address = stripAddress(address);
        this.deliveryPerson = null;
        this.restaurantAddress = restaurantAddress;
        GeoLocationService geoLocationService = new GeoLocationService();
        try{
        this.location = geoLocationService.getCoordinates(restaurantAddress);
        } catch (Exception e){
            this.location = new double[]{0,0};
        }
    }


    // Constructor for creating an order from a string representation
    public Order(String orderString) {
        String[] parts = orderString.split(",");
        this.orderId = Integer.parseInt(parts[0]);
        this.orderDate = new Date(Long.parseLong(parts[1]));  // Assuming date is stored as a long timestamp
        this.customerName = parts[2];
        this.restaurantName = parts[3];
        this.status = parts[4];
        this.customerNote = parts[5];
        this.address = parts[6];
        this.deliveryPerson = parts[7];
        this.items = new ArrayList<>();

        // Parse items from the string and add to the list
        for (int i = 8; i < parts.length; i++) {
            this.items.add(new Item(parts[i]));
        }

        this.totalPrice = calculateTotalPrice();
    }

    // Constructor with only orderId
    public Order(int orderId){
        this.orderId = orderId;
    }

    // Getters and Setters

    public String getRestaurantAddress(){
        return restaurantAddress;
    }

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

    public void setDistance(double distance){
        this.distance = distance;
    }

    public double getDistance(){
        return distance;
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

    public String getCustomerNote() {
        return customerNote;
    }

    public void setAddress(String address){
        this.address = stripAddress(address);
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public String getDeliveryPerson() {
        return deliveryPerson;
    }

    public void setDeliveryPerson(String deliveryPerson) {
        this.deliveryPerson = deliveryPerson;
    }

    public boolean isDelivered() {
        return status.equals("Delivered");
    }

    public String getAddress() {
        return address;
    }

    public double[] getLocation(){
        return location;
    }

    public void setTotal(double total){
        this.totalPrice = total;
    }

    // Inner class to represent an item within an order
    public static class Item {
        private String name;
        private double price;
        private boolean available;
        private String photoUrl;
        private String description;
        private int quantity;

        // Constructor for an item with basic details
        public Item(String name, double price) {
            this.name = name;
            this.price = price;
            this.available = true;
            this.photoUrl = null;
        }

        // Constructor with all item fields
        public Item(String name, double price, String photoUrl, String description, boolean available) {
            this.name = name;
            this.price = price;
            this.photoUrl = photoUrl;
            this.description = safeString(description);
            this.available = available;
        }

        // Constructor for creating an item from a string representation
        public Item(String itemString) {
            String[] parts = itemString.split(";");
            this.name = parts[0];
            this.price = Double.parseDouble(parts[1]);
            this.available = Boolean.parseBoolean(parts[2]);
            this.photoUrl = parts[3];
            this.description = parts[4];
            this.quantity = parts.length > 5 ? Integer.parseInt(parts[5]) : 0;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }


        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return name + ";" + price + ";" + available + ";" + photoUrl + ";" + description + ";" + quantity;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    // Additional methods for managing the order

    // Adds a new item to the order and updates the total price
    public void addItem(String itemName, double price) {
        if (itemName == null || itemName.isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        Item item = new Item(itemName, price);
        items.add(item);
        totalPrice += price;
    }

    // Removes an item from the order by name and updates the total price
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

    // Prints the order details to the console
    public void printOrder() {
        System.out.println("Order ID: " + orderId);
        System.out.println("Order Date: " + orderDate);
        System.out.println("Customer: " + customerName);
        System.out.println("Restaurant: " + restaurantName);
        System.out.println("Items: " + items);
        System.out.println("Total Price: ₪" + totalPrice);
        System.out.println("Status: " + status);
        System.out.println("Customer Note: " + customerNote);
    }

    // Searches for items in the order based on a keyword
    public List<Item> searchItems(String keyword) {
        return items.stream()
                .filter(item -> item.getName().contains(keyword))
                .collect(Collectors.toList());
    }

    // Sorts the items in the order by their name
    public void sortItems() {
        items.sort((item1, item2) -> item1.getName().compareTo(item2.getName()));
    }

    // Calculates the total price of the order based on item prices
    private double calculateTotalPrice() {
        return items.stream().mapToDouble(Item::getPrice).sum();
    }

    @Override
    public String toString() {
        // Return a string representation of the order
        StringBuilder sb = new StringBuilder();
        sb.append(orderId).append(",")
                .append(orderDate.getTime()).append(",")
                .append(customerName).append(",")
                .append(restaurantName).append(",")
                .append(status).append(",")
                .append(customerNote).append(",")
                .append(address).append(",")
                .append(deliveryPerson);


        for (Item item : items) {
            sb.append(",").append(item.toString());
        }

        return sb.toString();
    }

    public static String safeString(String str) {
        return str;
    }

    public String stripAddress(String address) {
        return address;
    }

}
