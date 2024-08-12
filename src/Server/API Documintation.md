### UsainWolt API Documentation

This API allows clients to interact with the UsainWolt server to manage users, orders, and restaurant-related functionalities. The API supports customer and restaurant signups, order placements, menu updates, and image handling. All communication is done through JSON over a socket connection.

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
    "message": "Login successful"
  }
  ```

#### 2. **Signup Customer**
- **Type**: `signupCustomer`
- **Description**: Registers a new customer.
- **Request**:
  ```json
  {
    "type": "signupCustomer",
    "username": "user_name",
    "password": "user_password",
    "address": "customer_address",
    "phoneNumber": "customer_phone_number",
    "email": "customer_email"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Customer signup successful"
  }
  ```

#### 3. **Signup Restaurant**
- **Type**: `signupRestaurant`
- **Description**: Registers a new restaurant.
- **Request**:
  ```json
  {
    "type": "signupRestaurant",
    "username": "restaurant_name",
    "password": "restaurant_password",
    "address": "restaurant_address",
    "phoneNumber": "restaurant_phone_number",
    "email": "restaurant_email",
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

#### 4. **Get Restaurants**
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
        "profilePicture": "Base64_encoded_image_string_or_null"
      }
    ]
  }
  ```

#### 5. **Get Menu**
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
        "itemName": "menu_item_name",
        "description": "menu_item_description",
        "price": "menu_item_price",
        "image": "Base64_encoded_image_string_or_null"
      }
    ]
  }
  ```

#### 6. **Place Order**
- **Type**: `placeOrder`
- **Description**: Places a new order for a customer.
- **Request**:
  ```json
  {
    "type": "placeOrder",
    "username": "customer_username",
    "password": "customer_password",
    "restaurantName": "restaurant_name",
    "items": "item1;item2;item3",  // Items separated by semicolons
    "customerNote": "optional_note",
    "useSavedCard": "true_or_false",  // Whether to use a saved credit card
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

#### 7. **Update Menu**
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
    "description": "item_description"
  }
  ```
  - **Image**: The image data should be sent as a binary stream immediately after the JSON request.
- **Response**:
  ```json
  {
    "success": true,
    "message": "Menu item added/updated successfully"
  }
  ```

#### 8. **Get Orders History**
- **Type**: `getOrdersHistory`
- **Description**: Retrieves the order history for a customer or restaurant.
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
        "orderId": order_id,
        "orderDate": "order_date",
        "items": ["item1", "item2"],
        "status": "order_status",
        "customerNote": "optional_note"
      }
    ]
  }
  ```

#### 9. **Change Password**
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

#### 10. **Change Email**
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

#### 11. **Get Image**
- **Type**: `getImage`
- **Description**: Retrieves an image from the server given a path.
- **Request**:
  ```json
  {
    "type": "getImage",
    "imagePath": "relative_image_path"
  }
  ```
- **Response**:
  ```json
  {
    "success": true,
    "message": "Image retrieved successfully",
    "imageData": "Base64_encoded_image_string"
  }
  ```

#### 12. **Upload Profile Picture**
- **Type**: `uploadProfilePicture`
- **Description**: Uploads a profile picture for a restaurant.
- **Request**:
  ```json
  {
    "type": "uploadProfilePicture",
    "username": "restaurant_username"
  }
  ```
  - **Image**: The image data should be sent as a binary stream immediately after the JSON request.
- **Response**:
  ```json
  {
    "success": true,
    "message": "Profile picture uploaded successfully"
  }
  ```

#### 13. **Disconnect**
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

#### 14. **Get Available Cuisines**
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
    "message": "American, Chinese, Italian, Japanese, Mexican, Thai, Israeli, Indian"
  }
  ```

---

### Image Handling

- Images are uploaded and retrieved using specific endpoints.
- Image data is encoded in Base64 when being sent as part of JSON responses.
- Image paths are relative to the server's image directories, such as `profile_pictures/` or `menu

_item_images/`.

---

### Error Handling

- All requests must include valid parameters; otherwise, the server will return an error response.
- Example error response:
  ```json
  {
    "success": false,
    "message": "Error description"
  }
  ```

### Notes
- Ensure that binary data (like images) is correctly handled when sending and receiving requests.
- Always validate user credentials before processing requests that require authentication.

This documentation provides an overview of the API methods and their expected inputs/outputs. Use this as a guide to integrate the UsainWolt server into client applications effectively.