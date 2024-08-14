package Client.GUI;

import Client.network.ClientApp;

import javax.swing.*;
import java.awt.*;

public class RestaurantGUI {
    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final ClientApp clientApp;
    private final String[] availableCuisines;
    private final LogoutCallback logoutCallback;

    public RestaurantGUI(JFrame frame, JTextField usernameField, JPasswordField passwordField, ClientApp clientApp, String[] availableCuisines , LogoutCallback logoutCallback) {
        this.frame = frame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.clientApp = clientApp;
        this.availableCuisines = availableCuisines;
        this.logoutCallback = logoutCallback;
    }

    void generateRestaurantUI() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Create a panel for the sidebar with buttons
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the sidebar

        // Create the buttons
        JButton viewOrdersButton = new JButton("View Orders");
        JButton manageMenuButton = new JButton("Manage Menu");
        JButton updateSettingsButton = new JButton("Update Settings");
        JButton logoutButton = new JButton("Logout");

        // Add action listeners for buttons
        viewOrdersButton.addActionListener(e -> showViewOrders());
        manageMenuButton.addActionListener(e -> showManageMenu());
        updateSettingsButton.addActionListener(e -> showRestaurantSettings());
        logoutButton.addActionListener(e -> handleLogout());

        // Add buttons to the sidebar panel
        sidebar.add(viewOrdersButton);
        sidebar.add(manageMenuButton);
        sidebar.add(updateSettingsButton);
        sidebar.add(logoutButton);

        // Create a main content panel where the content will change based on button clicks
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the main content

        // Add a welcome label to the main content panel
        JLabel welcomeLabel = new JLabel("Welcome to Usain Wolt Restaurant Panel!", JLabel.CENTER);
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
        // Implement the logic to logout the restaurant
        logoutCallback.onLogout();
    }

    private void showViewOrders() {
        // Implement the UI and logic to show the restaurant's orders
    }

    private void showManageMenu() {
        // Implement the UI and logic to manage the restaurant's menu
    }

    private void showRestaurantSettings() {
        // Implement the UI and logic to update the restaurant's settings
    }

}
