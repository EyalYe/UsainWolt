package Server.Models;

public class AdminUser extends User {
    public AdminUser(String userName, String hashedPassword, String address, String phoneNumber, String email) {
        super(userName, hashedPassword, address, phoneNumber, email);
    }

    public AdminUser(String csvLine) {
        super(csvLine);
    }

    @Override
    public void performUserSpecificAction() {
        // Admins don't have any specific actions
    }

}
