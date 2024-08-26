## API Documentation for `ClientHandler` Server

This documentation outlines the API endpoints provided by the `ClientHandler` class. Each endpoint is accessed via a JSON-formatted request, and the response is returned in JSON format as well.

### Table of Contents

1. [Login](#login)
2. [Sign Up](#sign-up)
  - Customer
  - Restaurant
  - Delivery
3. [Get Restaurants](#get-restaurants)
4. [Get Menu](#get-menu)
5. [Place Order](#place-order)
6. [Update Menu](#update-menu)
7. [Update Credit Card](#update-credit-card)
8. [Get Orders History](#get-orders-history)
9. [Mark Order Ready For Pickup](#mark-order-ready-for-pickup)
10. [Disable Menu Item](#disable-menu-item)
11. [Enable Menu Item](#enable-menu-item)
12. [Get Current Orders](#get-current-orders)
13. [Disconnect](#disconnect)
14. [Upload Profile Picture](#upload-profile-picture)
15. [Get Image](#get-image)
16. [Change Password](#change-password)
17. [Change Email](#change-email)
18. [Delete Account](#delete-account)
19. [Get Delivery Orders](#get-delivery-orders)
20. [Pickup Order](#pickup-order)
21. [Check If On Delivery](#check-if-on-delivery)
22. [Mark Order Delivered](#mark-order-delivered)
23. [Get User Data](#get-user-data)
24. [Get Income Data](#get-income-data)
25. [Change Parameter](#change-parameter)
26. [Get Available Cuisines](#get-available-cuisines)

---

### 1. **Login**

**Request Type:** `login`

**Description:** Authenticates a user based on username and password.

**Request:**

```json
{
  "type": "login",
  "username": "example_user",
  "password": "example_password"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Logged in as customer"
}
```

**Use Cases:**

- Log in as a customer, restaurant, or delivery user.

---

### 2. **Sign Up**

**Request Type:** `signupCustomer`, `signupRestaurant`, `signupDelivery`

**Description:** Registers a new user in the system.

**Request:**

*For Customer:*
```json
{
  "type": "signupCustomer",
  "username": "customer1",
  "email": "customer1@example.com",
  "address": "123 Main St",
  "phoneNumber": "1234567890",
  "password": "securepassword"
}
```

*For Restaurant:*
```json
{
  "type": "signupRestaurant",
  "username": "restaurant1",
  "email": "restaurant1@example.com",
  "address": "456 Elm St",
  "phoneNumber": "0987654321",
  "password": "securepassword",
  "businessPhoneNumber": "1231231234",
  "cuisine": "Italian"
}
```

*For Delivery:*
```json
{
  "type": "signupDelivery",
  "username": "delivery1",
  "email": "delivery1@example.com",
  "address": "789 Oak St",
  "phoneNumber": "4567891234",
  "password": "securepassword",
  "token": "validToken"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Customer signup successful"
}
```

**Use Cases:**

- Sign up as a customer, restaurant, or delivery user.
- Validation of input data such as email, phone number, address, and password length.

---

### 3. **Get Restaurants**

**Request Type:** `getRestaurants`

**Description:** Retrieves a list of restaurants within a specified distance and optionally filters by cuisine.

**Request:**

```json
{
  "type": "getRestaurants",
  "username": "customer1",
  "password": "securepassword",
  "distance": "10",
  "cuisine": "Italian",
  "sendHome": "true"
}
```

**Response:**

```json
{
  "success": true,
  "message": "[{...restaurant details...}]"
}
```

**Use Cases:**

- Retrieve nearby restaurants based on distance.
- Filter restaurants by specific cuisine.

---

### 4. **Get Menu**

**Request Type:** `getMenu`

**Description:** Retrieves the menu of a specified restaurant.

**Request:**

```json
{
  "type": "getMenu",
  "restaurantName": "restaurant1"
}
```

**Response:**

```json
{
  "success": true,
  "message": "[{...menu items...}]"
}
```

**Use Cases:**

- View the menu of a specific restaurant.

---

### 5. **Place Order**

**Request Type:** `placeOrder`

**Description:** Places a new order for a customer.

**Request:**

```json
{
  "type": "placeOrder",
  "username": "customer1",
  "password": "securepassword",
  "restaurantName": "restaurant1",
  "items": "[{...item details...}]",
  "customerNote": "Please add extra napkins.",
  "sendHome": "true",
  "useSavedCard": "true"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Order placed successfully with ID: 123"
}
```

**Use Cases:**

- Place an order for delivery to the customer's home or a specified address.
- Use a saved credit card or provide new payment details.

---

### 6. **Update Menu**

**Request Type:** `updateMenu`

**Description:** Adds, updates, or removes a menu item for a restaurant.

**Request:**

*Add/Update:*
```json
{
  "type": "updateMenu",
  "username": "restaurant1",
  "password": "securepassword",
  "itemName": "Pizza Margherita",
  "price": "9.99",
  "description": "Classic Italian pizza",
  "image": "base64EncodedImageString",
  "isAvailable": "true"
}
```

*Remove:*
```json
{
  "type": "updateMenu",
  "username": "restaurant1",
  "password": "securepassword",
  "itemName": "Pizza Margherita",
  "action": "remove"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Menu item added/updated successfully"
}
```

**Use Cases:**

- Add or update a menu item, including uploading an image.
- Remove an existing menu item.

---

### 7. **Update Credit Card**

**Request Type:** `updateCreditCard`

**Description:** Updates the credit card information for a customer.

**Request:**

```json
{
  "type": "updateCreditCard",
  "username": "customer1",
  "password": "securepassword",
  "creditCardNumber": "4111111111111111",
  "expirationDate": "12/25",
  "cvv": "123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Credit card updated successfully"
}
```

**Use Cases:**

- Update a customer's saved credit card information.

---

### 8. **Get Orders History**

**Request Type:** `getOrdersHistory`

**Description:** Retrieves the order history for a user.

**Request:**

```json
{
  "type": "getOrdersHistory",
  "username": "customer1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "[{...order history details...}]"
}
```

**Use Cases:**

- View past orders for customers, restaurants, or delivery users.

---

### 9. **Mark Order Ready For Pickup**

**Request Type:** `markOrderReadyForPickup`

**Description:** Marks an order as ready for pickup by a delivery user.

**Request:**

```json
{
  "type": "markOrderReadyForPickup",
  "username": "restaurant1",
  "password": "securepassword",
  "order": "{...order details...}"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Order status updated successfully"
}
```

**Use Cases:**

- Update the status of an order to "Ready For Pickup."

---

### 10. **Disable Menu Item**

**Request Type:** `disableMenuItem`

**Description:** Disables a menu item for a restaurant.

**Request:**

```json
{
  "type": "disableMenuItem",
  "username": "restaurant1",
  "password": "securepassword",
  "menuItemName": "Pizza Margherita"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Menu items disabled"
}
```

**Use Cases:**

- Temporarily disable a menu item from being ordered.

---

### 11. **Enable Menu Item**

**Request Type:** `enableMenuItem`

**Description:** Enables a previously disabled menu item.

**Request:**

```json
{
  "type": "enableMenuItem",
  "username": "restaurant1",
  "password": "securepassword",
  "menuItemName": "Pizza Margherita"
}
```

**Response:**

```json
{
  "success": true,
  "message":

 "Menu items enabled"
}
```

**Use Cases:**

- Make a previously disabled menu item available again.

---

### 12. **Get Current Orders**

**Request Type:** `getCurrentOrders`

**Description:** Retrieves the current orders for a restaurant.

**Request:**

```json
{
  "type": "getCurrentOrders",
  "username": "restaurant1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "[{...order details...}]"
}
```

**Use Cases:**

- View all orders that are currently active for a restaurant.

---

### 13. **Disconnect**

**Request Type:** `disconnect`

**Description:** Disconnects a user from the server.

**Request:**

```json
{
  "type": "disconnect",
  "username": "restaurant1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Disconnected successfully"
}
```

**Use Cases:**

- Log out a restaurant or delivery user from the system.

---

### 14. **Upload Profile Picture**

**Request Type:** `uploadProfilePicture`

**Description:** Uploads a profile picture for a restaurant user.

**Request:**

```json
{
  "type": "uploadProfilePicture",
  "username": "restaurant1",
  "password": "securepassword",
  "profilePicture": "base64EncodedImageString"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Profile picture uploaded successfully"
}
```

**Use Cases:**

- Upload or update a restaurant's profile picture.

---

### 15. **Get Image**

**Request Type:** `getImage`

**Description:** Retrieves an image file from the server.

**Request:**

```json
{
  "type": "getImage",
  "imagePath": "profile_pictures/restaurant1.jpg"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Image retrieved successfully",
  "imageData": "base64EncodedImageString"
}
```

**Use Cases:**

- Retrieve a profile picture or menu item image.

---

### 16. **Change Password**

**Request Type:** `changePassword`

**Description:** Changes the password of a user.

**Request:**

```json
{
  "type": "changePassword",
  "username": "customer1",
  "oldPassword": "oldpassword",
  "newPassword": "newpassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

**Use Cases:**

- Change the password for any user (customer, restaurant, delivery).

---

### 17. **Change Email**

**Request Type:** `changeEmail`

**Description:** Changes the email address of a user.

**Request:**

```json
{
  "type": "changeEmail",
  "username": "customer1",
  "password": "securepassword",
  "newEmail": "newemail@example.com"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Email changed successfully"
}
```

**Use Cases:**

- Update the email address for any user.

---

### 18. **Delete Account**

**Request Type:** `deleteAccount`

**Description:** Deletes a user's account from the system.

**Request:**

```json
{
  "type": "deleteAccount",
  "username": "customer1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Account deleted successfully"
}
```

**Use Cases:**

- Remove a customer, restaurant, or delivery account.

---

### 19. **Get Delivery Orders**

**Request Type:** `getDeliveryOrders`

**Description:** Retrieves orders available for delivery within a specified distance.

**Request:**

```json
{
  "type": "getDeliveryOrders",
  "username": "delivery1",
  "password": "securepassword",
  "address": "789 Oak St",
  "distance": "10"
}
```

**Response:**

```json
{
  "success": true,
  "message": "[{...order details...}]"
}
```

**Use Cases:**

- View delivery orders based on the distance from the delivery user's current location.

---

### 20. **Pickup Order**

**Request Type:** `pickupOrder`

**Description:** Marks an order as picked up by a delivery user.

**Request:**

```json
{
  "type": "pickupOrder",
  "username": "delivery1",
  "password": "securepassword",
  "orderId": "123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Order picked up successfully for delivery to 123 Main St"
}
```

**Use Cases:**

- Update the status of an order to "Picked Up" for delivery.

---

### 21. **Check If On Delivery**

**Request Type:** `checkIfOnDelivery`

**Description:** Checks if a delivery user is currently on a delivery.

**Request:**

```json
{
  "type": "checkIfOnDelivery",
  "username": "delivery1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "You are on a delivery to 123 Main St"
}
```

**Use Cases:**

- Verify if the delivery user is on an active delivery.

---

### 22. **Mark Order Delivered**

**Request Type:** `markOrderDelivered`

**Description:** Marks an order as delivered and updates the delivery user's income.

**Request:**

```json
{
  "type": "markOrderDelivered",
  "username": "delivery1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Order marked as delivered"
}
```

**Use Cases:**

- Update the status of an order to "Delivered" and increase the delivery user's income.

---

### 23. **Get User Data**

**Request Type:** `getUserData`

**Description:** Retrieves the profile information of a user.

**Request:**

```json
{
  "type": "getUserData",
  "username": "customer1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "{...user data...}"
}
```

**Use Cases:**

- View the profile information of a customer or restaurant.

---

### 24. **Get Income Data**

**Request Type:** `getIncomeData`

**Description:** Retrieves the income data for a delivery user.

**Request:**

```json
{
  "type": "getIncomeData",
  "username": "delivery1",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "success": true,
  "message": "{...income data...}"
}
```

**Use Cases:**

- View the total income for a delivery user.

---

### 25. **Change Parameter**

**Request Type:** `changeParameter`

**Description:** Updates a specific profile parameter for a user.

**Request:**

```json
{
  "type": "changeParameter",
  "username": "restaurant1",
  "password": "securepassword",
  "parameter": "address",
  "newValue": "789 Oak St"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Parameter updated successfully"
}
```

**Use Cases:**

- Change a user’s address, phone number, email, or other parameters.

---

### 26. **Get Available Cuisines**

**Request Type:** `getAvailableCuisines`

**Description:** Retrieves a list of all available cuisines on the platform.

**Request:**

```json
{
  "type": "getAvailableCuisines"
}
```

**Response:**

```json
{
  "success": true,
  "message": "[\"Italian\", \"Chinese\", \"Mexican\"]"
}
```

**Use Cases:**

- View the list of cuisines offered by restaurants on the platform.