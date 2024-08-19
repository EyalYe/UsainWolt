package Client.GUI;

import Client.network.ClientApp;

import javax.swing.*;

public interface LogoutCallback {
    void onLogout();
    void showLoadingScreen();

    String[] getCuisines();
}
