# UsainWolt

## Introduction
**UsainWolt** is a Java-based clone of the Wolt food delivery application. It allows customers to browse nearby restaurants, view menus, place orders, and track order history. Restaurants can update menus, manage orders, and track revenue. The project uses object-oriented design principles and a client-server architecture, with distinct packages for client and server functionalities. It incorporates exception handling, containers, dynamic operations, and user-friendly interfaces using `JOptionPane`.

## How to run the project
1. Clone the repository.
2. Open the project in an IDE (IntelliJ IDEA recommended).
3. Rename the `.envtemp` file (in `src/Server`) to `.env` and add the provided API keys.
4. To run the example test cases, execute `src/Main`.
5. To run separately:
    - Run the `src/Server/ServerMain.java` file for the server.
    - Run the `src/Client/UsainWoltMain.java` file for the client.
6. Interact with the system using the GUI.

***List of users is found in the `users.csv` file, with username, password, and role.***

## Troubleshooting
### Dependencies:
- Gson

### Ports:
- 8080 for the image server.
- 12345 for the main server.

Ensure API keys are correct, dependencies are installed, and the specified ports are available.

## Features
### Client
#### Customer:
- Browse nearby restaurants.
- View menus and place orders.
- Track order history.
- Update personal information (password, email, phone, address, payment method).

#### Restaurant:
- Update and manage menus (add, remove, enable/disable items).
- Manage and track orders.
- Track revenue.
- Update personal information.

#### Delivery Person:
- View and accept nearby orders.
- Track order status.
- Update personal information.
- Track revenue.

#### Admin:
- Admin functionality is planned for future development.

### Server
- Handle multiple client connections.
- Manage backup and restore of data.
- Manage orders and users.
- Send order notifications to clients.
- Track revenue and handle API integration.
- Implement password hashing for security.
- Serve images for restaurants and items.

## Future Features
- Admin interface for user and restaurant management.
- Integration of live order tracking for customers.
- Improved revenue tracking with analytics.
- Push notifications for real-time updates.

## How the Project Meets the Code Guidelines
1. **Collection of Classes**:
   The project contains more than five classes including `CustomerUser`, `RestaurantUser`, `ServerApp`, `Menu`, and `Order`.

2. **Use of Containers**:
   Containers like `List` and `Map` are used to store restaurants, orders, and menu items dynamically.

3. **Dynamic System**:
   The system supports addition (restaurants, menu items, orders), deletion (menu items, restaurants), searching (find restaurants by name), and sorting (sort restaurants or orders based on custom criteria). For example, a method could be implemented to sort restaurants by their ratings.

4. **Regular Class, Abstract Class, Interface**:
    - Regular class: `ServerApp` handles server-side logic.
    - Abstract class: `User` is an abstract class with `CustomerUser` , `RestaurantUser`, `DeliveryUser`, and `AdminUser` as subclasses.
    - Interface: 'LogoutCallback' is an interface used to implement logout functionality.

5. **Inheritance and Polymorphism**:
   Inheritance is demonstrated in `CustomerUser`, `RestaurantUser`, `DeliveryUser` and `AdminUser` , all inheriting from `User`, allowing the system to use polymorphism by referencing users generically as `User` objects.

6. **User-Friendly**:
    The project uses 'JFrame' and 'JOptionPane' to create a user-friendly interface. The GUI is intuitive and easy to navigate, with clear instructions and feedback messages. The system also provides error messages for incorrect input.
7. **Exception Handling**:
   Exception handling is implemented for edge cases such as duplicate restaurant names, incorrect login credentials, and invalid input data. For example:
   ```java
   if (restaurantExists(name)) throw new IllegalArgumentException("Restaurant already exists.");
   ```

8. **Well-Organized Code**:
   The code is organized into clear packages and classes, with each class following the single responsibility principle. Methods are well-documented with clear function names that indicate their purpose. The project also contains meaningful variable names and comments explaining complex logic.




