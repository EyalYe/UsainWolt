### UsainWolt API Documentation

This API allows clients to interact with the UsainWolt server to manage users, restaurants, orders, and delivery functionalities. The API supports customer, restaurant, and delivery signups, order placements, menu updates, and image handling. All communication is done through JSON over a socket connection.

---

### General Request and Response Format

**Request:**
- All requests are sent as JSON objects.
- Each request must include a `type` field that indicates the type of action being requested.

**Response:**
- Responses are JSON objects.
- Each response will include a `success` field (`true` or `false`) and a `message` field containing additional details or error information.
- Some responses may include additional fields based on the request.

---

### API Endpoints

#### 1. **Login**
- **Type**: `login`
- **Description**: Authenticates a user with their username and password.
- **Request**:
  ```json
  {
    "type": "login",
    "username": "user_name",
    "password": "user_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Logged in as <role>"
  }
  ```

---

#### 2. **Signup Customer**
- **Type**: `signupCustomer`
- **Description**: Registers a new customer.
- **Request**:
  ```json
  {
    "type": "signupCustomer",
    "username": "user_name",
    "password": "user_password",
    "email": "customer_email",
    "address": "customer_address",
    "phoneNumber": "customer_phone_number"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Customer signup successful"
  }
  ```

---

#### 3. **Signup Restaurant**
- **Type**: `signupRestaurant`
- **Description**: Registers a new restaurant.
- **Request**:
  ```json
  {
    "type": "signupRestaurant",
    "username": "restaurant_name",
    "password": "restaurant_password",
    "email": "restaurant_email",
    "address": "restaurant_address",
    "phoneNumber": "restaurant_phone_number",
    "businessPhoneNumber": "business_phone_number",
    "cuisine": "cuisine_type"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Restaurant signup successful"
  }
  ```

---

#### 4. **Signup Delivery**
- **Type**: `signupDelivery`
- **Description**: Registers a new delivery person.
- **Request**:
  ```json
  {
    "type": "signupDelivery",
    "username": "delivery_username",
    "password": "delivery_password",
    "email": "delivery_email",
    "address": "delivery_address",
    "phoneNumber": "delivery_phone_number",
    "token": "authorization_token"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Delivery signup successful"
  }
  ```

---

#### 5. **Get Restaurants**
- **Type**: `getRestaurants`
- **Description**: Retrieves a list of restaurants filtered by distance and cuisine.
- **Request**:
  ```json
  {
    "type": "getRestaurants",
    "username": "customer_username",
    "password": "customer_password",
    "distance": "max_distance_in_km",
    "cuisine": "optional_cuisine_filter"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": [
      {
        "restaurantName": "restaurant_name",
        "address": "restaurant_address",
        "distance": "distance_in_km",
        "cuisine": "cuisine_type",
        "profilePictureUrl": "URL_or_null"
      }
    ]
  }
  ```

---

#### 6. **Get Menu**
- **Type**: `getMenu`
- **Description**: Retrieves the menu for a specific restaurant.
- **Request**:
  ```json
  {
    "type": "getMenu",
    "restaurantName": "restaurant_name"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": [
      {
        "name": "menu_item_name",
        "description": "menu_item_description",
        "price": "menu_item_price",
        "available": "true_or_false",
        "photoUrl": "URL_or_null"
      }
    ]
  }
  ```

---

#### 7. **Place Order**
- **Type**: `placeOrder`
- **Description**: Places a new order for a customer.
- **Request**:
  ```json
  {
    "type": "placeOrder",
    "username": "customer_username",
    "password": "customer_password",
    "restaurantName": "restaurant_name",
    "items": [
      {
        "name": "item_name",
        "price": "item_price",
        "quantity": "item_quantity"
      }
    ],
    "customerNote": "optional_note",
    "sendHome": "true_or_false",
    "useSavedCard": "true_or_false",  
    "creditCardNumber": "card_number_if_not_saved",
    "expirationDate": "MM/YY",
    "cvv": "cvv_code"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Order placed successfully with ID: order_id"
  }
  ```

---

#### 8. **Update Menu**
- **Type**: `updateMenu`
- **Description**: Updates or adds a menu item for a restaurant.
- **Request**:
  ```json
  {
    "type": "updateMenu",
    "username": "restaurant_username",
    "password": "restaurant_password",
    "restaurantName": "restaurant_name",
    "itemName": "item_name",
    "price": "item_price",
    "description": "item_description",
    "isAvailable": "true_or_false"
  }
  ```
  - **Image**: Optional; can be sent as a binary stream after the request.
- **Response**:
  ```json
  {
    "success": true,
    "message": "Menu item added/updated successfully"
  }
  ```

---

#### 9. **Update Credit Card**
- **Type**: `updateCreditCard`
- **Description**: Updates the customer's credit card information.
- **Request**:
  ```json
  {
    "type": "updateCreditCard",
    "username": "customer_username",
    "password": "customer_password",
    "creditCardNumber": "card_number",
    "expirationDate": "MM/YY",
    "cvv": "cvv_code"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Credit card updated successfully"
  }
  ```

---

#### 10. **Get Orders History**
- **Type**: `getOrdersHistory`
- **Description**: Retrieves the order history for a customer, restaurant, or delivery person.
- **Request**:
  ```json
  {
    "type": "getOrdersHistory",
    "username": "user_name",
    "password": "user_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": [
      {
        "orderId": "order_id",
        "orderDate": "order_date",
        "items": [
          {
            "name": "item_name",
            "quantity": "item_quantity",
            "price": "item_price"
          }
        ],
        "status": "order_status",
        "customerNote": "optional_note"
      }
    ]
  }
  ```

---

#### 11. **Mark Order Ready for Pickup**
- **Type**: `markOrderReadyForPickup`
- **Description**: Marks an order as ready for pickup by the delivery person.
- **Request**:
  ```json
  {
    "type": "markOrderReadyForPickup",
    "username": "restaurant_username",
    "password": "restaurant_password",
    "order": {
      "orderId": "order_id"
    }
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Order marked as ready for pickup"
  }
  ```

---

#### 12. **Disable Menu Item**
- **Type**: `disableMenuItem`
- **Description**: Disables a menu item in the restaurant's menu.
- **Request**:
  ```json
  {
    "type": "disableMenuItem",
    "username": "restaurant_username",
    "password": "restaurant_password",
    "menuItemName": "item_name"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Menu item disabled"
  }
  ```

---

#### 13. **Enable Menu Item**
- **Type**: `enableMenuItem`
- **Description**: Enables a previously disabled menu item.
- **Request**:
  ```json
  {
    "type": "enableMenuItem",
    "username": "restaurant_username",
    "password": "restaurant_password",
    "menuItemName": "item_name"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Menu item enabled"
  }


  ```

---

#### 14. **Get Current Orders**
- **Type**: `getCurrentOrders`
- **Description**: Retrieves the current active orders for a restaurant.
- **Request**:
  ```json
  {
    "type": "getCurrentOrders",
    "username": "restaurant_username",
    "password": "restaurant_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": [
      {
        "orderId": "order_id",
        "items": [
          {
            "name": "item_name",
            "quantity": "item_quantity"
          }
        ],
        "status": "order_status"
      }
    ]
  }
  ```

---

#### 15. **Get Available Cuisines**
- **Type**: `getAvailableCuisines`
- **Description**: Retrieves the list of available cuisines.
- **Request**:
  ```json
  {
    "type": "getAvailableCuisines"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": ["American", "Chinese", "Italian", "Mexican"]
  }
  ```

---

#### 16. **Change Password**
- **Type**: `changePassword`
- **Description**: Changes the password for a user.
- **Request**:
  ```json
  {
    "type": "changePassword",
    "username": "user_name",
    "oldPassword": "old_password",
    "newPassword": "new_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Password changed successfully"
  }
  ```

---

#### 17. **Change Email**
- **Type**: `changeEmail`
- **Description**: Changes the email address for a user.
- **Request**:
  ```json
  {
    "type": "changeEmail",
    "username": "user_name",
    "password": "user_password",
    "newEmail": "new_email_address"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Email changed successfully"
  }
  ```

---

#### 18. **Disconnect**
- **Type**: `disconnect`
- **Description**: Disconnects a user from the server.
- **Request**:
  ```json
  {
    "type": "disconnect",
    "username": "user_name",
    "password": "user_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Disconnected successfully"
  }
  ```

---

#### 19. **Upload Profile Picture**
- **Type**: `uploadProfilePicture`
- **Description**: Uploads a profile picture for a user.
- **Request**:
  ```json
  {
    "type": "uploadProfilePicture",
    "username": "user_name",
    "password": "user_password",
    "profilePicture": "<base64_encoded_image>"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Profile picture uploaded successfully"
  }
  ```

---

#### 20. **Get Image**
- **Type**: `getImage`
- **Description**: Retrieves an image from the server.
- **Request**:
  ```json
  {
    "type": "getImage",
    "imagePath": "path_to_image"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Image retrieved successfully",
    "imageData": "<base64_encoded_image>"
  }
  ```

---

#### 21. **Change Parameter**
- **Type**: `changeParameter`
- **Description**: Updates a specific parameter (e.g., address, phone number, etc.) for the user.
- **Request**:
  ```json
  {
    "type": "changeParameter",
    "username": "user_name",
    "password": "user_password",
    "parameter": "parameter_name",
    "newValue": "new_value"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Parameter updated successfully"
  }
  ```

---

#### 22. **Delete Account**
- **Type**: `deleteAccount`
- **Description**: Deletes a user's account.
- **Request**:
  ```json
  {
    "type": "deleteAccount",
    "username": "user_name",
    "password": "user_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Account deleted successfully"
  }
  ```

---

#### 23. **Get Delivery Orders**
- **Type**: `getDeliveryOrders`
- **Description**: Retrieves available delivery orders for a delivery person within a certain distance.
- **Request**:
  ```json
  {
    "type": "getDeliveryOrders",
    "username": "delivery_username",
    "password": "delivery_password",
    "address": "current_location",
    "distance": "max_distance_in_km"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": [
      {
        "orderId": "order_id",
        "customerName": "customer_name",
        "items": [
          {
            "name": "item_name",
            "quantity": "item_quantity"
          }
        ],
        "status": "order_status"
      }
    ]
  }
  ```

---

#### 24. **Pickup Order**
- **Type**: `pickupOrder`
- **Description**: Marks an order as picked up by the delivery person.
- **Request**:
  ```json
  {
    "type": "pickupOrder",
    "username": "delivery_username",
    "password": "delivery_password",
    "orderId": "order_id"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Order picked up successfully"
  }
  ```

---

#### 25. **Check If On Delivery**
- **Type**: `checkIfOnDelivery`
- **Description**: Checks if the delivery person is currently on a delivery.
- **Request**:
  ```json
  {
    "type": "checkIfOnDelivery",
    "username": "delivery_username",
    "password": "delivery_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "You are on a delivery"
  }
  ```

---

#### 26. **Mark Order Delivered**
- **Type**: `markOrderDelivered`
- **Description**: Marks an order as delivered by the delivery person.
- **Request**:
  ```json
  {
    "type": "markOrderDelivered",
    "username": "delivery_username",
    "password": "delivery_password",
    "orderId": "order_id"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Order marked as delivered"
  }
  ```

---

#### 27. **Get Income Data**
- **Type**: `getIncomeData`
- **Description**: Retrieves the income data for a delivery person.
- **Request**:
  ```json
  {
    "type": "getIncomeData",
    "username": "delivery_username",
    "password": "delivery_password"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": {
      "income": "total_income"
    }
  }
  ```

---

#### 28. **Invalid Request (Default)**
- **Type**: Any invalid `type`
- **Description**: Handles invalid request types.
- **Response**:
  ```json
  {
    "success": false,
    "message": "Invalid request type"
  }
  ```

---

### Image Handling

- Images are uploaded and retrieved using specific endpoints like `uploadProfilePicture` and `getImage`.
- Image data is encoded in Base64 format in JSON responses.

### Error Handling

- All requests must include valid parameters, or the server will return an error.
- Example error response:
  ```json
  {
    "success": false,
    "message": "Error description"
  }
  ```

### Notes
- Ensure that images (binary data) are handled correctly when sending or receiving requests.
- Validate user credentials before processing requests that require authentication.

This documentation provides an overview of the UsainWolt API methods and their expected inputs/outputs.