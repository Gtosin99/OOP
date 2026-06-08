package com.mycompany.santelims.controllers;

import com.mycompany.santelims.dao.RoleDao;
import com.mycompany.santelims.dao.UserDao;
import com.mycompany.santelims.models.Role;
import com.mycompany.santelims.models.User;
import com.mycompany.santelims.services.AuditService;
import com.mycompany.santelims.utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UserManagementController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;

    private final UserDao userDao = new UserDao();
    private final RoleDao roleDao = new RoleDao();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getRole() != null
                    ? data.getValue().getRole().getRoleName()
                    : ""
            )
        );

        List<Role> roles = roleDao.getAllRoles();
        for (Role r : roles) {
            roleComboBox.getItems().add(r.getRoleName());
        }

        loadUsers();
    }

    private void loadUsers() {
        List<User> users = userDao.getAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    @FXML
    public void handleAddUser() {
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String roleName = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || roleName == null) {
            showMessage("All fields are required.", "red");
            return;
        }

        // Hash password with BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Role role = new Role();
        role.setRoleName(roleName);

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setMustChangePassword(true); // force password change on first login
        user.setRole(role);

        userDao.addUser(user);

        // Audit log
        String byEmail = Session.getCurrentUser().getEmail();
        AuditService.logUserCreated(byEmail, email, roleName);

        showMessage("User added successfully.", "green");
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        loadUsers();
    }

    @FXML
    public void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a user to delete.", "red");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + selected.getFullName() + "?");
        alert.setContentText("This action cannot be undone.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String byEmail = Session.getCurrentUser().getEmail();
                AuditService.logUserDeleted(byEmail, selected.getEmail());
                userDao.deleteUser(selected.getId());
                showMessage("User deleted.", "green");
                loadUsers();
            }
        });
    }

    private void showMessage(String text, String color) {
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
        messageLabel.setText(text);
    }
}