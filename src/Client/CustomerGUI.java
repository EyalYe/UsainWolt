package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class CustomerGUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ClientApp clientApp;

    // Declare signupFrame as an instance variable
    private JFrame signupFrame;

    public CustomerGUI(ClientApp clientApp) {
        this.clientApp = clientApp;
        initialize();
    }

    private void initialize() {
        // Frame setup
        frame = new JFrame("Customer Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel setup
        JPanel panel = new JPanel();
        frame.add(panel, BorderLayout.CENTER);
        placeComponents(panel);

        // Display the frame
        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        usernameField = new JTextField(20);
        usernameField.setBounds(100, 20, 165, 25);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(100, 50, 165, 25);
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 80, 150, 25);
        panel.add(loginButton);

        JButton signupButton = new JButton("Sign Up");
        signupButton.setBounds(180, 80, 150, 25);
        panel.add(signupButton);

        // Add action listeners for buttons
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSignupForm();
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            Map<String, String> response = clientApp.login(username, password);
            if ("true".equals(response.get("success"))) {
                JOptionPane.showMessageDialog(frame, "Login successful!");
                openMainMenu();
            } else {
                JOptionPane.showMessageDialog(frame, "Login failed: " + response.get("message"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void openSignupForm() {
        // Initialize signupFrame here
        signupFrame = new JFrame("Customer Signup");
        signupFrame.setSize(400, 400);
        signupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        signupFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        signupFrame.add(panel, BorderLayout.CENTER);
        placeSignupComponents(panel);

        signupFrame.setVisible(true);
    }

    private void placeSignupComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField signupUsernameField = new JTextField(20);
        signupUsernameField.setBounds(100, 20, 165, 25);
        panel.add(signupUsernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField signupPasswordField = new JPasswordField(20);
        signupPasswordField.setBounds(100, 50, 165, 25);
        panel.add(signupPasswordField);

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(10, 80, 80, 25);
        panel.add(addressLabel);

        JTextField addressField = new JTextField(20);
        addressField.setBounds(100, 80, 165, 25);
        panel.add(addressField);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(10, 110, 80, 25);
        panel.add(phoneLabel);

        JTextField phoneField = new JTextField(20);
        phoneField.setBounds(100, 110, 165, 25);
        panel.add(phoneField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(10, 140, 80, 25);
        panel.add(emailLabel);

        JTextField emailField = new JTextField(20);
        emailField.setBounds(100, 140, 165, 25);
        panel.add(emailField);

        JButton signupButton = new JButton("Sign Up");
        signupButton.setBounds(10, 170, 150, 25);
        panel.add(signupButton);

        // Add action listener for signup button
        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = signupUsernameField.getText();
                String password = new String(signupPasswordField.getPassword());
                String address = addressField.getText();
                String phone = phoneField.getText();
                String email = emailField.getText();

                try {
                    Map<String, String> response = clientApp.signupCustomer(username, password, address, phone, email);
                    if ("true".equals(response.get("success"))) {
                        JOptionPane.showMessageDialog(panel, "Signup successful! Please log in.");
                        signupFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(panel, "Signup failed: " + response.get("message"));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                }
            }
        });
    }

    private void openMainMenu() {
        JFrame mainMenuFrame = new JFrame("Customer Main Menu");
        mainMenuFrame.setSize(400, 400);
        mainMenuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainMenuFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        mainMenuFrame.add(panel, BorderLayout.CENTER);
        panel.setLayout(new GridLayout(4, 1));

        JButton viewRestaurantsButton = new JButton("View Restaurants");
        JButton viewMenuButton = new JButton("View Menu");
        JButton placeOrderButton = new JButton("Place Order");
        JButton orderHistoryButton = new JButton("Order History");

        panel.add(viewRestaurantsButton);
        panel.add(viewMenuButton);
        panel.add(placeOrderButton);
        panel.add(orderHistoryButton);

        // Add action listeners to buttons (implement these methods)
        viewRestaurantsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewRestaurants();
            }
        });

        viewMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewMenu();
            }
        });

        placeOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });

        orderHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewOrderHistory();
            }
        });

        mainMenuFrame.setVisible(true);
    }

    private void viewRestaurants() {
        // Code to view restaurants
        JOptionPane.showMessageDialog(frame, "List of restaurants would be shown here.");
    }

    private void viewMenu() {
        // Code to view menu of a selected restaurant
        JOptionPane.showMessageDialog(frame, "Restaurant menu would be shown here.");
    }

    private void placeOrder() {
        // Code to place an order
        JOptionPane.showMessageDialog(frame, "Order placement form would be shown here.");
    }

    private void viewOrderHistory() {
        // Code to view order history
        JOptionPane.showMessageDialog(frame, "Order history would be shown here.");
    }

    public static void main(String[] args) {
        try {
            ClientApp clientApp = new ClientApp("localhost", 12345);
            new CustomerGUI(clientApp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
