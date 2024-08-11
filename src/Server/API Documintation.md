# UsainWolt API Documentation

## Overview

This document provides an overview of how to interact with the UsainWolt server API. The API allows users to perform various actions such as logging in, signing up, placing orders, updating menus, and more.

### Base URL

All requests should be sent to the base URL of the server:

```
http://<server_address>:<port>/
```

Replace `<server_address>` and `<port>` with the actual address and port of the server.

---

## Authentication

### Login

**Endpoint:** `/login`

**Method:** `POST`

**Description:** Authenticates a user with their username and password.

**Parameters:**
- `username` (string): The username of the user.
- `password` (string): The user's password.

**Response:**
- Success: `{"success": "true", "message": "Login successful"}`
- Failure: `{"success": "false", "message": "Invalid username or password"}`

---

### Signup (Customer)

**Endpoint:** `/signupCustomer`

**Method:** `POST`

**Description:** Registers a new customer user.

**Parameters:**
- `username` (string): Desired username.
- `password` (string): Desired password.
- `address` (string): The customer's address.
- `phoneNumber` (string): The customer's phone number.
- `email` (string): The customer's email address.

**Response:**
- Success: `{"success": "true", "message": "Customer signup successful"}`
- Failure: `{"success": "false", "message": "Invalid address"}`

### Signup (Restaurant)

**Endpoint:** `/signupRestaurant`

**Method:** `POST`

**Description:** Registers a new restaurant user.

**Parameters:**
- `username` (string): Desired username.
- `password` (string): Desired password.
- `address` (string): The restaurant's address.
- `phoneNumber` (string): The restaurant's phone number.
- `email` (string): The restaurant's email address.
- `businessPhoneNumber` (string): The restaurant's business phone number.

**Response:**
- Success: `{"success": "true", "message": "Restaurant signup successful"}`
- Failure: `{"success": "false", "message": "Invalid address"}`

---

## Order Management

### Place Order

**Endpoint:** `/placeOrder`

**Method:** `POST`

**Description:** Places an order for a customer.

**Parameters:**
- `username` (string): The customer's username.
- `password` (string): The customer's password.
- `restaurantName` (string): The name of the restaurant.
- `items` A semicolon-separated string of items, where each item is formatted as `"itemName:price"`, e.g., `"Pizza:12.99;Soda:2.50"`.
- `customerNote` (string): Optional note from the customer.
- `creditCardNumber` (string): Customer's credit card number.
- `expirationDate` (string): Credit card expiration date.
- `cvv` (string): Credit card CVV code.

**Response:**
- Success: `{"success": "true", "message": "Order placed successfully with ID: <order_id>"}`
- Failure: `{"success": "false", "message": "Credit card authentication failed"}`

---

### Get Order History

**Endpoint:** `/getOrdersHistory`

**Method:** `POST`

**Description:** Retrieves the order history for a customer or restaurant.

**Parameters:**
- `username` (string): The username of the user.
- `password` (string): The user's password.

**Response:**
- Success: JSON array of orders.
- Failure: `{"success": "false", "message": "Authentication failed or user not found"}`

---

## Menu Management

### Update Menu

**Endpoint:** `/updateMenu`

**Method:** `POST`

**Description:** Updates the menu of a restaurant.

**Parameters:**
- `username` (string): The restaurant's username.
- `password` (string): The restaurant's password.
- `restaurantName` (string): The name of the restaurant.
- `itemName` (string): The name of the menu item.
- `price` (double): The price of the menu item.

**Response:**
- Success: `{"success": "true", "message": "Menu updated successfully"}`
- Failure: `{"success": "false", "message": "Authentication failed or restaurant not found"}`

---

### Enable/Disable Menu Items

**Endpoint:** `/enableMenuItems` or `/disableMenuItems`

**Method:** `POST`

**Description:** Enables or disables all menu items of a restaurant.

**Parameters:**
- `username` (string): The restaurant's username.
- `password` (string): The restaurant's password.

**Response:**
- Success: `{"success": "true", "message": "Menu items enabled/disabled"}`
- Failure: `{"success": "false", "message": "Authentication failed or restaurant not found"}`

---

## Account Management

### Update Credit Card

**Endpoint:** `/updateCreditCard`

**Method:** `POST`

**Description:** Updates the credit card information for a customer.

**Parameters:**
- `username` (string): The customer's username.
- `password` (string): The customer's password.
- `creditCardNumber` (string): New credit card number.
- `expirationDate` (string): New credit card expiration date.
- `cvv` (string): New credit card CVV code.

**Response:**
- Success: `{"success": "true", "message": "Credit card updated successfully"}`
- Failure: `{"success": "false", "message": "Credit card authentication failed"}`

---

### Mark Order as Complete

**Endpoint:** `/markOrderComplete`

**Method:** `POST`

**Description:** Marks an order as complete.

**Parameters:**
- `username` (string): The restaurant's username.
- `password` (string): The restaurant's password.
- `orderId` (int): The ID of the order to mark as complete.

**Response:**
- Success: `{"success": "true", "message": "Order marked as complete"}`
- Failure: `{"success": "false", "message": "Authentication failed or restaurant not found"}`

---

#### Get Available Cuisines

**Endpoint:** `/getAvailableCuisines`

**Method:** `POST`

**Description:** Retrieves a list of available cuisines.

**Parameters:**
- None

**Response:**
- Success: `{"success": "true", "cuisines": ["Cuisine1", "Cuisine2", ...]}`
- Failure: `{"success": "false", "message": "Failed to retrieve cuisines"}`


---

### Disconnect

**Endpoint:** `/disconnect`

**Method:** `POST`

**Description:** Disconnects a user from the server.

**Parameters:**
- `username` (string): The username of the user.
- `password` (string): The user's password.

**Response:**
- Success: `{"success": "true", "message": "Disconnected successfully"}`

If the user is a restaurant, they will also be removed from the list of logged-in restaurants.

---

## Notes

- **All requests** must be made using the `POST` method.
- **JSON format** should be used for both request parameters and responses.
- The server will return appropriate error messages for invalid or failed requests.

---

This document outlines the basic usage of the UsainWolt server API. For any further questions or clarifications, please consult the development team or refer to additional documentation.
