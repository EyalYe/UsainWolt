package Client.GUI;

import Client.network.ClientApp;

import javax.swing.*;
import java.awt.*;

public class AdminGUI {
    private final JFrame frame;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final ClientApp clientApp;
    private final String[] availableCuisines;
    private final LogoutCallback logoutCallback;

    AdminGUI(JFrame frame, JTextField usernameField, JPasswordField passwordField, ClientApp clientApp, String[] availableCuisines , LogoutCallback logoutCallback) {
        this.frame = frame;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.clientApp = clientApp;
        this.availableCuisines = availableCuisines;
        this.logoutCallback = logoutCallback;
    }
    // -------------------------------------- UI for Admin --------------------------------------
    void generateAdminUI() {
        // Clear the existing components from the frame
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Create a panel for the sidebar with buttons
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(4, 1, 10, 10));
        sidebar.setPreferredSize(new Dimension(200, frame.getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the sidebar

        // Create the buttons
        JButton manageUsersButton = new JButton("Manage Users");
        JButton viewStatisticsButton = new JButton("View Statistics");
        JButton configureSettingsButton = new JButton("Configure Settings");
        JButton logoutButton = new JButton("Logout");

        // Add action listeners for buttons
        manageUsersButton.addActionListener(e -> showManageUsers());
        viewStatisticsButton.addActionListener(e -> showViewStatistics());
        configureSettingsButton.addActionListener(e -> showConfigureSettings());
        logoutButton.addActionListener(e -> handleLogout());

        // Add buttons to the sidebar panel
        sidebar.add(manageUsersButton);
        sidebar.add(viewStatisticsButton);
        sidebar.add(configureSettingsButton);
        sidebar.add(logoutButton);

        // Create a main content panel where the content will change based on button clicks
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the main content

        // Add a welcome label to the main content panel
        JLabel welcomeLabel = new JLabel("Welcome to Usain Wolt Admin Panel!", JLabel.CENTER);
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

    private void showManageUsers() {
        // Implement the UI and logic to manage users
        // This could include a list of all users with options to edit or delete user accounts
    }

    private void showViewStatistics() {
        // Implement the UI and logic to show platform statistics
        // This might display data such as the number of active users, orders processed, revenue generated, etc.
    }

    private void showConfigureSettings() {
        // Implement the UI and logic to configure system-wide settings
        // This could include options to change global settings, update the platform's appearance, etc.
    }

}
