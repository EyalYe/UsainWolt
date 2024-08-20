package Client;

import Client.GUI.UsainWoltGUI;
import Client.network.ClientApp;

import javax.swing.*;

public class UsainWoltMain {
    public final static String SERVER_IP = "192.168.1.12";
    public final static int SERVER_PORT = 12345;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClientApp clientApp = new ClientApp(SERVER_IP, SERVER_PORT);
                Thread clientThread = new Thread(clientApp);
                clientThread.start();

                new UsainWoltGUI(clientApp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void skipLogin(String username, String password, String type) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClientApp clientApp = new ClientApp(SERVER_IP, SERVER_PORT);
                Thread clientThread = new Thread(clientApp);
                clientThread.start();

                new UsainWoltGUI(clientApp, username, password, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
