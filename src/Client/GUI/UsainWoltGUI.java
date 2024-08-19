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

public class UsainWoltGUI implements LogoutCallback {
    private JFrame frame;
    private ClientApp clientApp;
    private  JTextField usernameField;
    private JPasswordField passwordField;
    private Timer responsePoller;
    private String[] availableCuisines;
    private Gson gson = new Gson();
    private boolean isLoggedIn = false;
    private LoginPanel loginPanel;
    private CustomerGUI customerGUI;
    private RestaurantGUI restaurantGUI;
    private DeliveryGUI deliveryGUI;
    private AdminGUI adminGUI;

    @Override
    public void onLogout() {
        handleLogout();
    }

    @Override
    public void showLoadingScreen() {
        showLoading();
    }

    public UsainWoltGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        this.availableCuisines = new String[0];
        initialize();
        startResponsePolling();
        Map<String, Object> request = new HashMap<>();
        request.put("type", "getAvailableCuisines");
        clientApp.addRequest(request);
    }

    private JLabel connectionStatusLabel;

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
        this.loginPanel = new LoginPanel(frame, clientApp, usernameField, passwordField, this);
        loginPanel.generateLogin();

        // Set the frame visible at the end
        frame.setVisible(true);
    }

    private void updateConnectionStatus(String status) {
        SwingUtilities.invokeLater(() -> connectionStatusLabel.setText(status));
    }

    private void startResponsePolling() {
        responsePoller = new Timer(100, e -> {
            Map<String, Object> response = clientApp.getResponse();
            if (response != null) {
                processResponse(response);  // Process the response
            }
        });
        responsePoller.start();  // Start the timer to poll responses
    }

    private void processResponse(Map<String, Object> response) {
        String requestType = (String) response.get("type");
        closeLoading();
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
            case "handleGetAvailableCuisines":
                availableCuisines = ((String) response.get("message")).split(",");
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
            case "handleUpdateMenuItem":
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
        }
    }

    public void handleLoginResponse(Map<String, Object> response) {
        if ("true".equals(response.get("success"))) {
            JOptionPane.showMessageDialog(frame, "Login successful!");
            isLoggedIn = true;
            this.usernameField = loginPanel.getUsernameField();
            this.passwordField = loginPanel.getPasswordField();
            switch ((String) response.get("message")) {
                case "Logged in as customer":
                    customerGUI = new CustomerGUI(frame, usernameField, passwordField, clientApp, availableCuisines, this);
                    customerGUI.generateCustomerUI();
                    break;
                case "Logged in as restaurant":
                    restaurantGUI = new RestaurantGUI(frame, usernameField, passwordField, clientApp, availableCuisines, this);
                    restaurantGUI.generateRestaurantUI();
                    break;
                case "Logged in as delivery":
                    deliveryGUI = new DeliveryGUI(frame, usernameField, passwordField, clientApp, availableCuisines, this);
                    deliveryGUI.generateDeliveryUI();
                    break;
                case "Logged in as admin":
                    adminGUI = new AdminGUI(frame, usernameField, passwordField, clientApp, availableCuisines, this);
                    adminGUI.
                    generateAdminUI();
                    break;
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Login failed: " + response.get("message"));
        }
    }

    public void handleSignupResponse(Map<String, Object> response) {
        if ("true".equals(response.get("success"))) {
            JOptionPane.showMessageDialog(frame, "Signup successful!");
            loginPanel.generateLogin();
        } else {
            JOptionPane.showMessageDialog(frame, "Signup failed: " + response.get("message"));
        }
    }

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
        adminGUI = null;
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
