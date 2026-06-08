package com.sante.lims.controllers;

import com.sante.lims.dao.UserDao;
import com.sante.lims.models.User;
import com.sante.lims.util.SceneNavigator;
import com.sante.lims.utils.PasswordUtil;
import com.sante.lims.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UserDao userDao = new UserDao();

    @FXML
    public void handleChangePassword() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            SceneNavigator.switchScene("/fxml/login.fxml", "Sante LIMS - Login");
            return;
        }
        String password = newPasswordField.getText();
        if (password == null || password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmPasswordField.getText())) {
            messageLabel.setText("Passwords do not match.");
            return;
        }
        try {
            userDao.updatePassword(user.getId(), PasswordUtil.hashPassword(password));
            user.setForcePasswordChange(false);
            switch (user.getRole()) {
                case "SUPER_ADMIN" -> SceneNavigator.switchScene("/fxml/admin-dashboard.fxml", "Sante LIMS - Super Admin");
                case "LAB_ATTENDANT" -> SceneNavigator.switchScene("/fxml/attendant-dashboard.fxml", "Sante LIMS - Lab Attendant");
                default -> SceneNavigator.switchScene("/fxml/customer-dashboard.fxml", "Sante LIMS - Customer Dashboard");
            }
        } catch (Exception ex) {
            messageLabel.setText("Unable to update password: " + ex.getMessage());
        }
    }
}
