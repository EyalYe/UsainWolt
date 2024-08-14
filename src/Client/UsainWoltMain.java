package Client;

import Client.GUI.UsainWoltGUI;
import Client.network.ClientApp;

import javax.swing.*;

public class UsainWoltMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ClientApp clientApp = new ClientApp("localhost", 12345);
                Thread clientThread = new Thread(clientApp);
                clientThread.start();

                new UsainWoltGUI(clientApp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
