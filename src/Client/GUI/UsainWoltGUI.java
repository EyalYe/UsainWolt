// Group: 6
package Client.GUI;

import Client.model.Restaurant;
import Client.network.ClientApp;
import Server.Models.Order;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static Server.Utilities.CustomDateAdapter.gsonCreator;

public class UsainWoltGUI implements LogoutCallback {
    private JFrame frame;
    private ClientApp clientApp;
    private  JTextField usernameField;
    private JPasswordField passwordField;
    private Timer responsePoller;
    private String[] availableCuisines;
    private Gson gson = gsonCreator();
    private boolean isLoggedIn = false;
    private LoginPanel loginPanel;
    private CustomerGUI customerGUI = null;
    private RestaurantGUI restaurantGUI = null;
    private DeliveryGUI deliveryGUI = null;
    private boolean skipedLogin = false;
    private boolean firstTimeLogin;

    @Override
    public void onLogout() {
        handleLogout();
    }

    @Override
    public void showLoadingScreen() {
        showLoading();
    }

    @Override
    public String[] getCuisines() {
        return availableCuisines;
    }

    // Constructor for initializing UsainWoltGUI
    public UsainWoltGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        this.availableCuisines = new String[0];
        startResponsePolling();
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        firstTimeLogin = true;
        clientApp.addRequest(request);
        initialize();
    }

    // Constructor for initializing UsainWoltGUI with login details
    public UsainWoltGUI(ClientApp clientApp, String username, String password, String userType) {
        this.clientApp = clientApp;
        this.availableCuisines = new String[0];
        usernameField = new JTextField();
        this.usernameField.setText(username);
        passwordField = new JPasswordField();
        this.passwordField.setText(password);
        startResponsePolling();
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        firstTimeLogin = false;
        skipedLogin = true;
        clientApp.addRequest(request);
        initialize_noLogin(userType);
    }

    // Initializes GUI components without login
    private void initialize_noLogin(String userType){
      initialize();
        Map<String, Object> request = new HashMap<>();
        request.put("type", "login");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        request.put("userType", userType);
        clientApp.addRequest(request);
    }

    private JLabel connectionStatusLabel;

    // Updates the connection status label
    private void updateConnectionStatus(String status) {
        SwingUtilities.invokeLater(() -> connectionStatusLabel.setText(status));
    }

    // Initializes the main GUI frame
    private void initialize() {
        // Setup the main frame
        frame = new JFrame("Usain Wolt");
        frame.setSize(1050, 600);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame, "Leaving so soon?", "Close Window?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    if (responsePoller != null) {
                        responsePoller.stop();
                    }
                    if(isLoggedIn) {
                        try {
                            clientApp.disconnect(usernameField.getText(), new String(passwordField.getPassword()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    clientApp.close();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.dispose();
                }
            }
        });

        // Add connection status label
        connectionStatusLabel = new JLabel("Not connected");
        frame.add(connectionStatusLabel, BorderLayout.NORTH);

        // Call the method to generate the login screen
        this.loginPanel = new LoginPanel(frame, clientApp, usernameField, passwordField, this, availableCuisines);
        loginPanel.generateLogin();

        // Set the frame visible at the end
        frame.setVisible(true);
    }

    // Starts polling for server responses in the background
    private void startResponsePolling() {
        SwingWorker<Void, Map<String, Object>> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (true) {
                    Map<String, Object> response = clientApp.getResponse();
                    if (response != null) {
                        // Publish response to the GUI thread
                        publish(response);
                    }

                    // Sleep for a while before the next poll to avoid excessive CPU usage
                    Thread.sleep(100);
                    closeLoading();
                }
            }

            @Override
            protected void process(List<Map<String, Object>> chunks) {
                // This runs on the EDT
                for (Map<String, Object> response : chunks) {
                    closeLoading();  // Close the loading dialog
                    processResponse(response);  // Process the response in the GUI
                }
            }
        };

        // Start the background worker
        worker.execute();
    }

    // Processes server responses and updates GUI accordingly
    private void processResponse(Map<String, Object> response) {
        String requestType = (String) response.get("type");
        if("false".equals(response.get("success"))) {
            JOptionPane.showMessageDialog(frame, "Error: " + response.get("message"));
            if ("You are not on the specified delivery".equals(response.get("message")) && deliveryGUI != null) {
                deliveryGUI.disableDeliveryFinishedButton();
            }
            return;
        }
        if(requestType == null) {
            return;
        }
        switch (requestType) {
            case "handleGetAvailableCuisines":
                availableCuisines = ((String) response.get("message")).split(",");
                if (loginPanel != null) {
                    loginPanel.updateCuisines(availableCuisines);
                }
                break;
            case "handleLogin":
                handleLoginResponse(response);
                break;
            case "handleSignupCustomer":
                handleSignupResponse(response);
                break;
            case "handleSignupRestaurant":
                handleSignupResponse(response);
                break;
            case "handleSignupDelivery":
                handleSignupResponse(response);
                break;
            case "handleGetRestaurants":
                Type restaurantListType = new TypeToken<java.util.List<Restaurant>>(){}.getType();
                List<Restaurant> restaurants = gson.fromJson((String) response.get("message"), restaurantListType);
                if(customerGUI != null)
                    customerGUI.showRestaurants(restaurants);
                break;
            case "handleGetMenu":
                Type menuListType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                if (customerGUI != null)
                    customerGUI.showMenu(gson.fromJson((String) response.get("message"), menuListType));
                if (restaurantGUI != null)
                    restaurantGUI.showMenu(gson.fromJson((String) response.get("message"), menuListType));
                break;
            case "handlePlaceOrder":
                if ("true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Order placed successfully!");
                    if (customerGUI != null)
                        customerGUI.showOrderHistory();
                } else {
                    JOptionPane.showMessageDialog(frame, "Error placing order: " + response.get("message"));
                }
                break;
            case "handleGetOrdersHistory":
                Type orderListType = new TypeToken<List<Order>>(){}.getType();
                List<Order> orders = gson.fromJson((String) response.get("message"), orderListType);
                if (customerGUI != null)
                    customerGUI.showOrderHistoryFrame(orders);
                if (restaurantGUI != null)
                    restaurantGUI.showOrdersFrame(orders);
                break;
            case "update":
                if (restaurantGUI != null)
                    restaurantGUI.refreshOrders();
                break;
            case "handleUpdateParameter":
                if ("true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Parameter updated successfully!");
                    if (restaurantGUI != null)
                        restaurantGUI.showRestaurantSettings();
                } else {
                    JOptionPane.showMessageDialog(frame, "Error updating parameter: " + response.get("message"));
                }
                break;
            case "handleMarkOrderReadyForPickup":
                if (!"true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Error marking order as ready for pickup: " + response.get("message"));
                }
                if (restaurantGUI != null)
                    restaurantGUI.showViewOrders();
                break;
            case "handleDisableMenuItems":
                if (!"true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Error disabling menu items: " + response.get("message"));
                }
                if (restaurantGUI != null)
                    restaurantGUI.showManageMenu();
                break;
            case "handleEnableMenuItems":
                if (!"true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Error enabling menu items: " + response.get("message"));
                }
                if (restaurantGUI != null)
                    restaurantGUI.showManageMenu();
                break;
            case "handleUpdateMenu":
                if (!"true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Error updating menu item: " + response.get("message"));
                }
                if (restaurantGUI != null)
                    restaurantGUI.showManageMenu();
                break;
            case "handleProfilePictureUpload":
                if (!"true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Error uploading profile picture: " + response.get("message"));
                }
                if (restaurantGUI != null)
                    restaurantGUI.showRestaurantSettings();
                break;
            case "handleGetDeliveryOrders":
                Type deliveryOrderListType = new TypeToken<List<Order>>(){}.getType();
                List<Order> deliveryOrders = gson.fromJson((String) response.get("message"), deliveryOrderListType);
                if (deliveryGUI != null)
                    deliveryGUI.showAvailableDeliveriesFrame(deliveryOrders);
                break;
            case "handlePickupOrder":
                if (!"true".equals(response.get("success"))) {
                    JOptionPane.showMessageDialog(frame, "Error updating delivery order status: " + response.get("message"));
                }
                if (deliveryGUI != null){
                    deliveryGUI.enableDeliveryFinishedButton(((String) response.get("message")).replace("Order picked up successfully for delivery to ", ""));
                    deliveryGUI.showAvailableDeliveries();
                }
                break;
            case "handleCheckIfOnDelivery":
                if (deliveryGUI != null){
                    if (((String) response.get("message")).contains("You are on a delivery to ")) {
                        deliveryGUI.enableDeliveryFinishedButton(((String) response.get("message")).replace("You are on a delivery to ", ""));
                    } else {
                        deliveryGUI.disableDeliveryFinishedButton();
                    }
                }
                break;
            case "handleMarkOrderDelivered":
                if (deliveryGUI != null) {
                    deliveryGUI.disableDeliveryFinishedButton();
                    deliveryGUI.showAvailableDeliveries();
                }
                break;
            case "handleGetIncomeData":
                if (deliveryGUI != null) {
                    deliveryGUI.showUserSettingsFrame((String) response.get("message"));
                }
                break;
            case "handleGetUserData":
                if (customerGUI != null) {
                    Type newType = new TypeToken<Map<String, String>>(){}.getType();
                    customerGUI.createUserDataPane(gson.fromJson((String) response.get("message"), newType));
                }
                if (restaurantGUI != null) {
                    Type newType = new TypeToken<Map<String, String>>(){}.getType();
                    restaurantGUI.createRestaurantSettingsFrame(gson.fromJson((String) response.get("message"), newType));
                }
                break;
            case "handleUpdateCreditCard":
                if (customerGUI != null) {
                    if ("true".equals(response.get("success"))) {
                        JOptionPane.showMessageDialog(frame, "Credit card updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error updating credit card: " + response.get("message"));
                    }
                }
                break;
        }
    }

    // Handles login response from the server
    public void handleLoginResponse(Map<String, Object> response) {
        if ("true".equals(response.get("success"))) {
            if(firstTimeLogin)
                JOptionPane.showMessageDialog(frame, "Login successful!");
            isLoggedIn = true;
            if (!skipedLogin) {
                this.usernameField = loginPanel.getUsernameField();
                this.passwordField = loginPanel.getPasswordField();
            }
            switch ((String) response.get("message")) {
                case "Logged in as customer":
                    customerGUI = new CustomerGUI(frame, usernameField, passwordField, clientApp, availableCuisines, this);
                    customerGUI.generateCustomerUI();
                    break;
                case "Logged in as restaurant":
                    clientApp.setRestaurant(true, usernameField.getText(), new String(passwordField.getPassword()));
                    if(restaurantGUI == null) {
                        restaurantGUI = new RestaurantGUI(frame, usernameField, passwordField, clientApp, availableCuisines, this);
                        restaurantGUI.generateRestaurantUI();
                        firstTimeLogin = false;
                    }
                    break;
                case "Logged in as delivery":
                    deliveryGUI = new DeliveryGUI(frame, usernameField, passwordField, clientApp, availableCuisines, this);
                    deliveryGUI.generateDeliveryUI();
                    break;
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Login failed: " + response.get("message"));
        }
    }

    // Handles signup response from the server
    public void handleSignupResponse(Map<String, Object> response) {
        if ("true".equals(response.get("success"))) {
            JOptionPane.showMessageDialog(frame, "Signup successful!");
            loginPanel.generateLogin();
        } else {
            JOptionPane.showMessageDialog(frame, "Signup failed: " + response.get("message"));
        }
    }

    // Handles logout
    public void handleLogout() {
        Map<String,Object> request = new HashMap<>();
        request.put("type", "disconnect");
        request.put("username", usernameField.getText());
        request.put("password", new String(passwordField.getPassword()));
        clientApp.addRequest(request);
        loginPanel.generateLogin();
        customerGUI = null;
        restaurantGUI = null;
        deliveryGUI = null;
    }

    // Method to add form fields for text fields and password fields
    public static JTextField addFormField(JPanel panel, String labelText, int yPos) {
        return addFormField(panel, labelText, yPos, new JTextField(20), false);
    }

    public static JPasswordField addFormField(JPanel panel, String labelText, int yPos, boolean isPasswordField) {
        return (JPasswordField) addFormField(panel, labelText, yPos, new JPasswordField(20), true);
    }

    public static JTextField addFormField(JPanel panel, String labelText, int yPos, JTextField textField) {
        return addFormField(panel, labelText, yPos, textField, false);
    }

    public static JTextField addFormField(JPanel panel, String labelText, int yPos, JTextField textField, boolean isPasswordField) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = yPos;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = yPos;
        panel.add(textField, gbc);

        return textField;
    }

    // Method to add form fields for JComboBox
    public static void addComboBoxField(JPanel panel, String labelText, int yPos, JComboBox<String> comboBox) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = yPos;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = yPos;
        panel.add(comboBox, gbc);
    }

    // -------------------------------------Loading screen--------------------------------------
    private JDialog loadingDialog;

    // Method to show the loading dialog
    void showLoading() {
        if (loadingDialog == null) {
            // Create a new JDialog with a loading message
            loadingDialog = new JDialog(frame, "Loading", true);
            loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            loadingDialog.setSize(200, 100);
            loadingDialog.setLayout(new BorderLayout());

            JLabel loadingLabel = new JLabel("Loading, please wait...", SwingConstants.CENTER);
            loadingDialog.add(loadingLabel, BorderLayout.CENTER);

            // loadingLabel.setIcon(new ImageIcon("path/to/loading.gif"));

            // Center the dialog on the parent frame
            loadingDialog.setLocationRelativeTo(frame);
        }

        // Show the loading dialog
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
    }

    // Method to close the loading dialog
    private void closeLoading() {
        if (loadingDialog != null) {
            // Hide and dispose of the loading dialog
            SwingUtilities.invokeLater(() -> {
                loadingDialog.setVisible(false);
                loadingDialog.dispose();
                // loadingDialog = null; // Reset the reference for future use
            });
        }
    }

}
