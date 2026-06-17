package lk.ijse.mental_health_therapy.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.entity.User;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField pwdPassword;
    @FXML private TextField     txtPasswordVisible;
    @FXML private Button        btnShowPassword;
    @FXML private Button        btnLogin;
    @FXML private Label         lblError;
    @FXML private RadioButton   rbAdmin;
    @FXML private RadioButton   rbReceptionist;
    @FXML private ToggleGroup roleGroup;

    private boolean passwordVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtPasswordVisible.textProperty().bindBidirectional(pwdPassword.textProperty());
        txtUsername.textProperty().addListener((obs, o, n) -> clearError());
        pwdPassword.textProperty().addListener((obs, o, n) -> clearError());
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            pwdPassword.setVisible(false);
            pwdPassword.setManaged(false);
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
            btnShowPassword.setText("🙈");
        } else {
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            pwdPassword.setVisible(true);
            pwdPassword.setManaged(true);
            pwdPassword.requestFocus();
            pwdPassword.positionCaret(pwdPassword.getText().length());
            btnShowPassword.setText("👁");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = pwdPassword.getText();

        if (username.isEmpty()) {
            showError("Please enter your username.");
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Please enter your password.");
            pwdPassword.requestFocus();
            return;
        }

        boolean isAdmin        = rbAdmin.isSelected();
        boolean isReceptionist = rbReceptionist.isSelected();

        if (!isAdmin && !isReceptionist) {
            showError("Please select a role before signing in.");
            return;
        }

        //  user verify
        User user = findUserInDB(username);

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {

            showError("Invalid credentials. Please try again.");
            return;
        }

        // Role mismatch check — selected role
        String dbRole = user.getRole();

        if (isAdmin && !"ADMIN".equalsIgnoreCase(dbRole)) {
            showError("This account does not have Admin access.");
            return;
        }
        if (isReceptionist && !"RECEPTIONIST".equalsIgnoreCase(dbRole)) {
            showError("This account does not have Receptionist access.");
            return;
        }

        // Session set
        Launcher.currentRole = dbRole.toUpperCase();
        ChangePasswordController.setLoggedInUser(username, Launcher.currentRole);

        // Navigate
        if ("ADMIN".equals(Launcher.currentRole)) {
            Launcher.setRoot("admindashboard");
        } else {
            Launcher.setRoot("ReceptionistDashboard");
        }
    }

    // User entity
    private User findUserInDB(String username) {
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            return session.get(User.class, username);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String message) { lblError.setText(message); }
    private void clearError()              { lblError.setText(""); }
}