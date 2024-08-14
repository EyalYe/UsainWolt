package Client.GUI;

import Client.network.ClientApp;

import javax.swing.*;
import java.awt.*;

public class DeliveryGUI {
    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final ClientApp clientApp;
    private final String[] availableCuisines;
    private final LogoutCallback logoutCallback;

    DeliveryGUI(JFrame frame, JTextField usernameField, JPasswordField passwordField, ClientApp clientApp, String[] availableCuisines , LogoutCallback logoutCallback) {
        this.frame = frame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.clientApp = clientApp;
        this.availableCuisines = availableCuisines;
        this.logoutCallback = logoutCallback;
    }

    // -------------------------------------- UI for Delivery --------------------------------------
    void generateDeliveryUI() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Create a panel for the sidebar with buttons
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(3, 1, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the sidebar

        // Create the buttons
        JButton viewDeliveriesButton = new JButton("View Deliveries");
        JButton deliveryHistoryButton = new JButton("Delivery History");
        JButton userSettingsButton = new JButton("User Settings");
        JButton logoutButton = new JButton("Logout");

        // Add action listeners for buttons
        viewDeliveriesButton.addActionListener(e -> showAvailableDeliveries());
        deliveryHistoryButton.addActionListener(e -> showDeliveryHistory());
        userSettingsButton.addActionListener(e -> showUserSettings());
        logoutButton.addActionListener(e -> handleLogout());

        // Add buttons to the sidebar panel
        sidebar.add(viewDeliveriesButton);
        sidebar.add(deliveryHistoryButton);
        sidebar.add(userSettingsButton);
        sidebar.add(logoutButton);

        // Create a main content panel where the content will change based on button clicks
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the main content

        // Add a welcome label to the main content panel
        JLabel welcomeLabel = new JLabel("Welcome to Usain Wolt Delivery Panel!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainContentPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Add the sidebar and main content panels to the frame
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainContentPanel, BorderLayout.CENTER);

        // Refresh the frame to display the new UI
        frame.revalidate();
        frame.repaint();
    }

    private void handleLogout() {
        logoutCallback.onLogout();
    }

    private void showUserSettings() {
    }

    private void showAvailableDeliveries() {
        // Implement the UI and logic to show available deliveries
        // This might include a list of delivery tasks that the user can accept or reject
    }

    private void showDeliveryHistory() {
        // Implement the UI and logic to show the delivery history of the delivery person
        // This could display past deliveries with details like date, time, restaurant, customer, etc.
    }

}
