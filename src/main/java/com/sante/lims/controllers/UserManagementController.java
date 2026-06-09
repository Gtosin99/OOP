package com.sante.lims.controllers;

import com.sante.lims.dao.UserDao;
import com.sante.lims.models.User;
import com.sante.lims.services.AuditLogService;
import com.sante.lims.utils.PasswordUtil;
import com.sante.lims.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class UserManagementController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colForceChange;

    private final UserDao userDao = new UserDao();
    private final AuditLogService auditLogService = new AuditLogService();

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("LAB_ATTENDANT", "CUSTOMER"));
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        colForceChange.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isForcePasswordChange() ? "Yes" : "No"));
        loadUsers();
    }

    @FXML
    public void handleAddUser() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        if (fullName.isBlank() || email.isBlank() || password == null || password.isBlank() || role == null) {
            messageLabel.setText("All fields are required.");
            return;
        }
        try {
            userDao.addStaffCreatedUser(fullName, email, PasswordUtil.hashPassword(password), role);
            User current = SessionManager.getCurrentUser();
            auditLogService.logAction(current == null ? null : current.getId(), "USER_CREATED",
                    "Created " + role + " account for " + email);
            clearForm();
            loadUsers();
            messageLabel.setText("User added. First login will require a password change.");
        } catch (Exception ex) {
            messageLabel.setText("Unable to add user: " + ex.getMessage());
        }
    }

    @FXML
    public void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a user to delete.");
            return;
        }
        User current = SessionManager.getCurrentUser();
        if (current != null && current.getId() == selected.getId()) {
            messageLabel.setText("You cannot delete your own account while signed in.");
            return;
        }
        if ("SUPER_ADMIN".equals(selected.getRole())) {
            messageLabel.setText("Super admin accounts cannot be deleted from this screen.");
            return;
        }
        try {
            auditLogService.logAction(current == null ? null : current.getId(), "USER_DELETED",
                    "Deleted user account " + selected.getEmail());
            userDao.deleteUser(selected.getId());
            loadUsers();
            messageLabel.setText("User deleted.");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Unable to delete user:\n" + ex.getMessage()).showAndWait();
        }
    }

    private void loadUsers() {
        try {
            userTable.setItems(FXCollections.observableArrayList(userDao.getAllUsers()));
        } catch (Exception ex) {
            messageLabel.setText("Unable to load users: " + ex.getMessage());
        }
    }

    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
    }
}
