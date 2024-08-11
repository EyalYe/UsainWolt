# UsainWolt

## Introduction
**UsainWolt** is a Java-based clone of the Wolt food delivery application. It allows customers to browse restaurants, view menus, place orders, and track their order history. Restaurants can update their menus, manage orders, and track their revenue. The project is built using Java and utilizes a client-server architecture, where the server manages user data, orders, and restaurant information, and the client interacts with the server through a graphical user interface (GUI).

## Project Checklist

### Completed Features
- [x] **User Authentication**:
    - Implemented login and signup features for both customers and restaurants.
- [x] **Customer GUI**:
    - Developed the GUI for customer login, signup, and main menu navigation.
- [x] **Server Handling**:
    - Created the server that listens for client connections and processes requests using the `ClientHandler` class.
- [x] **Order Placement**:
    - Customers can place orders through the GUI.
- [x] **Menu Management**:
    - Restaurants can update their menus.
- [x] **Order History**:
    - Customers can view their past orders.
- [x] **Restaurant Management**:
    - Restaurants can track current and past orders.
- [x] **Data Persistence**:
    - Users, menus, and orders are persisted in CSV files.
- [x] **Disconnect Handling**:
    - When a user exits the application, a disconnect request is sent to the server.

### Features in Progress or Planned
- [ ] **Customer Preferences (In Progress)**:
    - Map to store customer preferences for cuisine and distance.
- [ ] **Restaurant Location and Distance Calculation** (Planned):
    - Calculate the distance between the customer and restaurants using latitude and longitude.
- [ ] **Credit Card Validation** (In Progress):
    - Implement a mock credit card validation system.
- [ ] **Admin Interface (Planned)**:
    - A GUI for an admin to manage users and restaurants.

## Project Requirements Checklist

### Met Requirements
- [x] **User Authentication**:
    - Implemented secure login and signup for customers and restaurants.
- [x] **GUI for Customers**:
    - Created a user-friendly interface for customers to interact with the application.
- [x] **Order Management**:
    - Customers can browse restaurants, view menus, and place orders.
- [x] **Menu Management**:
    - Restaurants can update their menus through the GUI.
- [x] **Data Persistence**:
    - User data, menus, and orders are stored in CSV files for persistence.
- [x] **Client-Server Architecture**:
    - The application uses a client-server architecture, allowing multiple clients to connect to the server.
- [x] **Logout/Disconnect Handling**:
    - Users can log out, and the server is notified when a user disconnects.

### Unmet or Partially Met Requirements
- [ ] **Distance Calculation for Nearby Restaurants** (Planned):
    - Implementing distance-based filtering for restaurant search.
- [ ] **Customer Preferences System** (In Progress):
    - Adding a system for customers to save their cuisine and distance preferences.
- [ ] **Full Admin Functionality** (Planned):
    - Adding an admin interface to manage users and restaurants.

## File Structure

```plaintext
UsainWolt/
│
├── README.md                   # This file
├── src/
│   ├── Client/
│   │   ├── ClientApp.java       # The client-side application logic for interacting with the server.
│   │   ├── CustomerGUI.java     # GUI for customers to log in, sign up, and manage orders.
│   │   └── (RestaurantGUI.java) # GUI for restaurants to manage their menus and orders. (In Construction)
│   │
│   ├── Server/
│   │   ├── ServerApp.java       # The main server application that manages client connections.
│   │   ├── ClientHandler.java   # Handles individual client requests in a separate thread.
│   │   ├── User.java            # Abstract class representing a general user.
│   │   ├── CustomerUser.java    # Extends User to represent customers and their specific actions.
│   │   ├── RestaurantUser.java  # Extends User to represent restaurants and their specific actions.
│   │   ├── Order.java           # Represents orders, including order items and status.
│   │   ├── LocationService.java # Handles location-based services like distance calculation. (In Construction)
│   │   └── (AdminApp.java)      # GUI for admin functionalities. (In Construction)
│   │
│   └── resources/
│       ├── users.csv            # CSV file for storing user information.
│       ├── menus.csv            # CSV file for storing restaurant menus.
│       └── orders.csv           # CSV file for storing order information.
│
└── pom.xml                      # Maven configuration file if you're using Maven for project management.
```

## Explanation of Each File

### `ClientApp.java`
Handles the communication between the client GUI and the server, sending requests, and processing responses.

### `CustomerGUI.java`
Provides the graphical user interface for customers to log in, sign up, view restaurants, view menus, place orders, and view order history.

### `(RestaurantGUI.java) (In Construction)`
Will provide the graphical user interface for restaurant owners to manage their restaurant's menu and orders.

### `ServerApp.java`
The main server application that initializes the server, listens for incoming client connections, and manages the overall flow of the application.

### `ClientHandler.java`
A class that runs on a separate thread for each client, responsible for handling client requests and sending back responses.

### `User.java`
An abstract class that represents a general user of the system, containing common attributes and methods.

### `CustomerUser.java`
Extends the `User` class to represent a customer, including additional attributes like order history and preferences.

### `RestaurantUser.java`
Extends the `User` class to represent a restaurant, including additional attributes like menu and business phone number.

### `Order.java`
Represents an order placed by a customer, including order ID, items, total price, customer name, restaurant name, status, and customer notes.

### `LocationService.java (In Construction)`
Planned to handle location-based functionalities like calculating distances between customers and restaurants.

### `(AdminApp.java) (In Construction)`
Will provide the GUI for an admin to manage the users and restaurants within the system.

### `users.csv`
Stores user information such as username, password, address, and user type (customer or restaurant).

### `menus.csv`
Stores the menu items for each restaurant, including item names and prices.

### `orders.csv`
Stores information about each order, including order ID, customer name, restaurant name, items, and order status.
