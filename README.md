# UsainWolt

## Introduction
**UsainWolt** is a Java-based clone of the Wolt food delivery application. The system allows customers to browse nearby restaurants, view menus, place orders, and track their order history. For restaurants, it provides functionality to update menus, manage orders, and track revenue. The project is built using object-oriented design principles and follows a client-server architecture, with separate packages for client and server functionalities.

## Project Requirements Checklist

### Completed Requirements
- [x] **Object-Oriented Design**:
  - Implemented with multiple classes, including abstract classes and interfaces, demonstrating inheritance and polymorphism.
- [x] **Collection Usage**:
  - Utilized collections such as `List` to manage users, orders, and menus.
- [x] **Dynamic System**:
  - The system supports dynamic actions like adding, removing, and searching through menus and orders.
- [x] **User Interface**:
  - A GUI for customers has been developed using `JOptionPane` and Swing components.
- [x] **Error Handling**:
  - Implemented error handling using exceptions for edge cases and invalid inputs.
- [x] **Code Quality**:
  - The code is well-structured and documented, adhering to good coding standards.

### Partially Completed or Planned Requirements
- [ ] **Customer Preferences**:
  - Implementing a map to store customer preferences for cuisine and distance. (In Progress)
- [ ] **Distance Calculation**:
  - Implementing the calculation of distance between customers and restaurants using latitude and longitude. (Planned)
- [ ] **Credit Card Validation**:
  - A mock credit card validation system is partially implemented. (In Progress)
- [ ] **Restaurant GUI**:
  - GUI for restaurant management is planned but not yet implemented. (Planned)
- [ ] **Admin Interface**:
  - A GUI for an admin to manage users and restaurants is planned. (Planned)

## Project Requirements from Assignment Document

### Met Requirements
- [x] **Minimum 5 Classes**:
  - Implemented with multiple classes including `User`, `CustomerUser`, `RestaurantUser`, `Order`, and `ClientHandler`.
- [x] **Use of Collections**:
  - `List` is used to manage users, orders, and menu items dynamically.
- [x] **Dynamic System Operations**:
  - The system supports dynamic operations such as adding, removing, searching, sorting, and updating records.
- [x] **Inheritance and Polymorphism**:
  - The project uses inheritance (e.g., `CustomerUser` and `RestaurantUser` extend `User`) and polymorphism.
- [x] **User-Friendly Interface**:
  - A GUI for customers is developed using Swing components, following a user-friendly design.
- [x] **Exception Handling**:
  - Exception handling is implemented to manage errors and edge cases, ensuring robustness.

### Unmet or Partially Met Requirements
- [ ] **Full Feature Set in GUI**:
  - GUI for restaurants and an admin interface are planned but not yet implemented.
- [ ] **Advanced Location Features**:
  - Distance calculation and location-based filtering are planned but not yet implemented.
- [ ] **Credit Card Validation**:
  - A mock system for credit card validation is under development.

## File Structure

```plaintext
UsainWolt/
│
├── README.md                   # This file
├── src/
│   ├── Client/
│   │   ├── ClientApp.java       # Handles communication between client GUI and server.
│   │   ├── CustomerGUI.java     # GUI for customers to log in, sign up, and manage orders.
│   │   ├── (RestaurantGUI.java) # Planned GUI for restaurant management. (In Construction)
│   │   └── (AdminApp.java)      # Planned GUI for admin functionalities. (In Construction)
│   │
│   ├── Server/
│   │   ├── ServerApp.java       # Main server application that handles client connections.
│   │   ├── ClientHandler.java   # Manages individual client requests in separate threads.
│   │   ├── User.java            # Abstract class representing general users.
│   │   ├── CustomerUser.java    # Extends `User` to represent customers.
│   │   ├── RestaurantUser.java  # Extends `User` to represent restaurants.
│   │   ├── Order.java           # Represents orders including items and statuses.
│   │   ├── LocationService.java # Handles location-based services like distance calculation. (Planned)
│   │
└── resources/                   # Contains dynamically generated CSV files (not included in the repository).
    ├── users.csv                # Stores user information.
    ├── menus.csv                # Stores menu items for restaurants.
    └── orders.csv               # Stores order information.
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
