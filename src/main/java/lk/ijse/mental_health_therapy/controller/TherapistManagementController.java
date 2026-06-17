package lk.ijse.mental_health_therapy.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.bo.BOFactory;
import lk.ijse.mental_health_therapy.bo.custom.TherapistBO;
import lk.ijse.mental_health_therapy.dto.TherapistDTO;


import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TherapistManagementController implements Initializable {

    TherapistBO therapistBO = (TherapistBO) BOFactory.getInstance().getBO(BOFactory.BO_Types.THERAPIST);

    @FXML private TextField txtFullName;
    @FXML private TextField txtSpecialization;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtSearch;

    @FXML private ComboBox<String> cmbProgram;
    @FXML private ComboBox<String> cmbAvailability;

    @FXML private TableView<TherapistDTO>           tblTherapists;
    @FXML private TableColumn<TherapistDTO, Integer> colId;
    @FXML private TableColumn<TherapistDTO, String>  colName;
    @FXML private TableColumn<TherapistDTO, String>  colSpec;
    @FXML private TableColumn<TherapistDTO, String>  colPhone;
    @FXML private TableColumn<TherapistDTO, String>  colEmail;
    @FXML private TableColumn<TherapistDTO, String>  colAvail;


    @FXML private Label lblNameError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblEmailError;
    @FXML private Label lblStatus;


    @FXML private Button btnSave;


    private List<TherapistDTO> allTherapists;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();
        setupTableColumns();
        loadAllTherapists();
        clearErrors();
    }

    private void setupComboBoxes() {
        cmbAvailability.setItems(FXCollections.observableArrayList(
                "Available", "On Leave", "Fully Booked"
        ));
        cmbProgram.setItems(FXCollections.observableArrayList(
                "Anxiety Management", "Depression Support",
                "Stress Relief", "Trauma Recovery", "Mindfulness"
        ));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAvail.setCellValueFactory(new PropertyValueFactory<>("availability"));
    }

    private void loadAllTherapists() {
        try {
            allTherapists = therapistBO.findAllTherapists();
            tblTherapists.setItems(FXCollections.observableArrayList(allTherapists));
        } catch (Exception e) {
            showStatus("Failed to load therapists: " + e.getMessage(), true);
        }
    }


    @FXML
    void saveTherapist(ActionEvent event) {
        if (!validateForm()) return;

        try {
            TherapistDTO dto = buildDTOFromForm(0); // 0 → auto-generated ID
            boolean saved = therapistBO.saveTherapist(dto);

            if (saved) {
                showStatus("Therapist saved successfully.", false);
                clearForm(null);
                loadAllTherapists();
            } else {
                showStatus("Failed to save therapist.", true);
            }
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    void updateTherapist(ActionEvent event) {
        TherapistDTO selected = tblTherapists.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a therapist from the table first.", true);
            return;
        }
        if (!validateForm()) return;

        try {
            TherapistDTO dto = buildDTOFromForm(selected.getId());
            boolean updated = therapistBO.updateTherapist(dto);

            if (updated) {
                showStatus("Therapist updated successfully.", false);
                clearForm(null);
                loadAllTherapists();
            } else {
                showStatus("Failed to update therapist.", true);
            }
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    void deleteTherapist(ActionEvent event) {
        TherapistDTO selected = tblTherapists.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a therapist to delete.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete therapist \"" + selected.getFullName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Deletion");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean deleted = therapistBO.deleteTherapist(selected.getId());
                    if (deleted) {
                        showStatus("Therapist deleted.", false);
                        clearForm(null);
                        loadAllTherapists();
                    } else {
                        showStatus("Could not delete therapist.", true);
                    }
                } catch (Exception e) {
                    showStatus("Error: " + e.getMessage(), true);
                }
            }
        });
    }


    @FXML
    void onTableRowClick(MouseEvent event) {
        TherapistDTO selected = tblTherapists.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        txtFullName.setText(selected.getFullName());
        txtSpecialization.setText(selected.getSpecialization());
        txtPhone.setText(selected.getPhone());
        txtEmail.setText(selected.getEmail());
        cmbProgram.setValue(selected.getAssignedProgram());
        cmbAvailability.setValue(selected.getAvailability());

        clearErrors();
        lblStatus.setText("");
    }


    @FXML
    void searchTherapist(KeyEvent event) {
        String query = txtSearch.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            tblTherapists.setItems(FXCollections.observableArrayList(allTherapists));
            return;
        }

        List<TherapistDTO> filtered = allTherapists.stream()
                .filter(t ->
                        (t.getFullName()       != null && t.getFullName().toLowerCase().contains(query))       ||
                                (t.getSpecialization() != null && t.getSpecialization().toLowerCase().contains(query)) ||
                                (t.getEmail()          != null && t.getEmail().toLowerCase().contains(query))          ||
                                String.valueOf(t.getId()).contains(query)
                )
                .collect(Collectors.toList());

        tblTherapists.setItems(FXCollections.observableArrayList(filtered));
    }



    @FXML
    void clearForm(ActionEvent event) {
        txtFullName.clear();
        txtSpecialization.clear();
        txtPhone.clear();
        txtEmail.clear();
        cmbProgram.setValue(null);
        cmbAvailability.setValue(null);
        tblTherapists.getSelectionModel().clearSelection();
        clearErrors();
        lblStatus.setText("");
    }



    @FXML
    void goBack(ActionEvent event) {
        Launcher.setRoot("admindashboard");   // Admin only
    }



    private TherapistDTO buildDTOFromForm(int id) {
        return new TherapistDTO(
                id,
                txtFullName.getText().trim(),
                txtSpecialization.getText().trim(),
                txtPhone.getText().trim(),
                txtEmail.getText().trim(),
                cmbProgram.getValue(),
                cmbAvailability.getValue()
        );
    }

    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        if (txtFullName.getText().trim().isEmpty()) {
            lblNameError.setText("Full name is required.");
            valid = false;
        }

        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            lblPhoneError.setText("Phone number is required.");
            valid = false;
        } else if (!phone.matches("0\\d{9}")) {
            lblPhoneError.setText("Enter a valid 10-digit phone number.");
            valid = false;
        }

        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            lblEmailError.setText("Email is required.");
            valid = false;
        } else if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            lblEmailError.setText("Enter a valid email address.");
            valid = false;
        }

        return valid;
    }


    private void clearErrors() {
        lblNameError.setText("");
        lblPhoneError.setText("");
        lblEmailError.setText("");
    }

    private void showStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setStyle(isError
                ? "-fx-text-fill: #e53935;"
                : "-fx-text-fill: #43a047;");
    }
}