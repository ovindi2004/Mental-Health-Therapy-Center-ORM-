package lk.ijse.mental_health_therapy.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lk.ijse.mental_health_therapy.bo.BOFactory;
import lk.ijse.mental_health_therapy.bo.custom.UserBO;

public class ChangePasswordController {

    @FXML private TextField     txtNewUsername;
    @FXML private PasswordField pwdCurrentPassword;
    @FXML private PasswordField pwdNewPassword;
    @FXML private TextField     txtNewPasswordVisible;
    @FXML private PasswordField pwdConfirmPassword;
    @FXML private Button        btnToggleNew;
    @FXML private Label         lblUsernameError;
    @FXML private Label         lblCurrentPwdError;
    @FXML private Label         lblNewPwdError;
    @FXML private Label         lblConfirmPwdError;
    @FXML private Label         lblSuccess;

    private final UserBO userBO =
            (UserBO) BOFactory.getInstance().getBO(BOFactory.BO_Types.USER);

    private boolean newPasswordVisible = false;


    private static String loggedInUsername;
    private static String loggedInRole;

    public static void setLoggedInUser(String username, String role) {
        loggedInUsername = username;
        loggedInRole     = role;
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=]).{8,}$";

    @FXML
    public void initialize() {
        txtNewPasswordVisible.textProperty()
                .bindBidirectional(pwdNewPassword.textProperty());
    }

    @FXML
    private void toggleNewPassword() {
        newPasswordVisible = !newPasswordVisible;
        if (newPasswordVisible) {
            txtNewPasswordVisible.setVisible(true);
            txtNewPasswordVisible.setManaged(true);
            pwdNewPassword.setVisible(false);
            pwdNewPassword.setManaged(false);
            btnToggleNew.setText("🙈");
        } else {
            pwdNewPassword.setVisible(true);
            pwdNewPassword.setManaged(true);
            txtNewPasswordVisible.setVisible(false);
            txtNewPasswordVisible.setManaged(false);
            btnToggleNew.setText("👁");
        }
    }

    @FXML
    private void updateCredentials() {
        // Session check
        if (loggedInUsername == null) {
            lblSuccess.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
            lblSuccess.setText(" Session error. Please login again.");
            return;
        }

        clearErrors();

        String newUsername = txtNewUsername.getText().trim();
        String currentPwd  = pwdCurrentPassword.getText();
        String newPwd      = pwdNewPassword.getText();
        String confirmPwd  = pwdConfirmPassword.getText();
        boolean hasError   = false;

        // Username validation
        if (newUsername.isEmpty()) {
            newUsername = loggedInUsername;
        } else if (newUsername.length() < 3) {
            lblUsernameError.setText("Username must be at least 3 characters.");
            hasError = true;
        } else if (userBO.isUsernameTaken(newUsername, loggedInUsername)) {
            lblUsernameError.setText("Username is already taken.");
            hasError = true;
        }

        // Current password validation
        if (currentPwd.isEmpty()) {
            lblCurrentPwdError.setText("Current password is required.");
            hasError = true;
        } else if (!userBO.verifyCurrentPassword(loggedInUsername, currentPwd)) {
            lblCurrentPwdError.setText("Current password is incorrect.");
            hasError = true;
        }

        // New password validation
        if (newPwd.isEmpty()) {
            lblNewPwdError.setText("New password is required.");
            hasError = true;
        } else if (!newPwd.matches(PASSWORD_REGEX)) {
            lblNewPwdError.setText("Password doesn't meet the requirements.");
            hasError = true;
        }

        // Confirm password validation
        if (!confirmPwd.equals(newPwd)) {
            lblConfirmPwdError.setText("Passwords do not match.");
            hasError = true;
        }

        if (hasError) return;

        boolean success = userBO.updateCredentials(loggedInUsername, newUsername, newPwd);

        if (success) {
            loggedInUsername = newUsername;
            lblSuccess.setText(" Credentials updated successfully!");
            clearFields();
        } else {
            lblSuccess.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
            lblSuccess.setText(" Update failed. Please try again.");
        }
    }

    public static String getLoggedInRole() {
        return loggedInRole;
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) txtNewUsername.getScene().getWindow();
        stage.close();
    }

    private void clearErrors() {
        lblUsernameError.setText("");
        lblCurrentPwdError.setText("");
        lblNewPwdError.setText("");
        lblConfirmPwdError.setText("");
        lblSuccess.setText("");
        lblSuccess.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
    }

    private void clearFields() {
        txtNewUsername.clear();
        pwdCurrentPassword.clear();
        pwdNewPassword.clear();
        txtNewPasswordVisible.clear();
        pwdConfirmPassword.clear();
    }
}
