package lk.ijse.mental_health_therapy.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.bo.BOFactory;
import lk.ijse.mental_health_therapy.bo.custom.PatientBO;
import lk.ijse.mental_health_therapy.dto.PatientDTO;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PatientController implements Initializable {

    PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOFactory.BO_Types.PATIENT);

    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private ComboBox<String> cmbGender;
    @FXML private ComboBox<String> cmbTherapyProgram;
    @FXML private TableColumn<PatientDTO, Integer> colId;
    @FXML private TableColumn<PatientDTO, String> colName;
    @FXML private TableColumn<PatientDTO, LocalDate> colDob;
    @FXML private TableColumn<PatientDTO, String> colEmail;
    @FXML private TableColumn<PatientDTO, String> colGender;
    @FXML private TableColumn<PatientDTO, String> colPhone;
    @FXML private TableColumn<PatientDTO, String> colProgram;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private Label lblEmailError;
    @FXML private Label lblNameError;
    @FXML private Label lblPhoneError;
    @FXML private Label lblStatus;
    @FXML private TableView<PatientDTO> tblPatients;
    @FXML private TextArea txtAddress;
    @FXML private TextField txtEmail;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtSearch;

    private List<PatientDTO> allPatients;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();
        setupTableColumns();
        loadAllPatients();
        clearErrors();
    }

    private void setupComboBoxes() {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        cmbTherapyProgram.setItems(FXCollections.observableArrayList(
                "Cognitive Behavioral Therapy",
                "Dialectical Behavior Therapy",
                "Mindfulness-Based Therapy",
                "Psychodynamic Therapy",
                "Exposure Therapy"
        ));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colDob.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colProgram.setCellValueFactory(new PropertyValueFactory<>("therapyProgress"));
    }

    private void loadAllPatients() {
        try {
            allPatients = patientBO.findAllPatients();
            tblPatients.setItems(FXCollections.observableArrayList(allPatients));
        } catch (Exception e) {
            showStatus("Failed to load patients: " + e.getMessage(), true);
        }
    }

    @FXML
    void savePatient(ActionEvent event) {


        if (tblPatients.getSelectionModel().getSelectedItem() != null) {
            showStatus("Patient already exists. Use Update button.", true);
            return;
        }

        if (!validateForm()) return;

        btnSave.setDisable(true);

        try {
            PatientDTO dto = buildDTOFromForm(0);

            boolean saved = patientBO.savePatient(dto);

            if (saved) {
                showStatus("Patient saved successfully.", false);
                clearForm(null);
                loadAllPatients();
            } else {
                showStatus("Failed to save patient.", true);
            }

        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);

        } finally {
            btnSave.setDisable(false);
        }
    }

    @FXML
    void updatePatient(ActionEvent event) {
        PatientDTO selected = tblPatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a patient from the table first.", true);
            return;
        }
        if (!validateForm()) return;

        try {
            PatientDTO dto = buildDTOFromForm(selected.getId());
            boolean updated = patientBO.updatePatient(dto);

            if (updated) {
                showStatus("Patient updated successfully.", false);
                clearForm(null);
                loadAllPatients();
            } else {
                showStatus("Failed to update patient.", true);
            }
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), true);
        }
    }

    @FXML
    void deletePatient(ActionEvent event) {
        PatientDTO selected = tblPatients.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a patient to delete.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete patient \"" + selected.getFullName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm Deletion");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean deleted = patientBO.deletePatient(selected.getId());
                    if (deleted) {
                        showStatus("Patient deleted.", false);
                        clearForm(null);
                        loadAllPatients();
                    } else {
                        showStatus("Could not delete patient.", true);
                    }
                } catch (Exception e) {
                    showStatus("Error: " + e.getMessage(), true);
                }
            }
        });
    }

    @FXML
    void onTableRowClick(MouseEvent event) {
        PatientDTO selected = tblPatients.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        txtFullName.setText(selected.getFullName());
        dpDateOfBirth.setValue(selected.getDateOfBirth());
        cmbGender.setValue(selected.getGender());
        txtAddress.setText(selected.getAddress());
        txtPhone.setText(selected.getContactNumber());
        txtEmail.setText(selected.getEmail());
        cmbTherapyProgram.setValue(selected.getTherapyProgress());
        clearErrors();
        lblStatus.setText("");
    }

    @FXML
    void searchPatient(KeyEvent event) {
        String query = txtSearch.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            tblPatients.setItems(FXCollections.observableArrayList(allPatients));
            return;
        }

        List<PatientDTO> filtered = allPatients.stream()
                .filter(p ->
                        (p.getFullName() != null && p.getFullName().toLowerCase().contains(query)) ||
                                (p.getEmail()    != null && p.getEmail().toLowerCase().contains(query))    ||
                                (p.getContactNumber() != null && p.getContactNumber().contains(query))     ||
                                String.valueOf(p.getId()).contains(query)
                )
                .collect(Collectors.toList());

        tblPatients.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    void clearForm(ActionEvent event) {
        txtFullName.clear();
        dpDateOfBirth.setValue(null);
        cmbGender.setValue(null);
        txtAddress.clear();
        txtPhone.clear();
        txtEmail.clear();
        cmbTherapyProgram.setValue(null);
        tblPatients.getSelectionModel().clearSelection();
        clearErrors();
        lblStatus.setText("");
    }


    private PatientDTO buildDTOFromForm(int id) {
        return new PatientDTO(
                id,
                txtFullName.getText().trim(),
                dpDateOfBirth.getValue(),
                cmbGender.getValue(),
                txtAddress.getText().trim(),
                txtPhone.getText().trim(),
                txtEmail.getText().trim(),
                cmbTherapyProgram.getValue()
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
        } else if (!phone.matches("[\\d\\s+\\-]{7,15}")) {
            lblPhoneError.setText("Enter a valid phone number.");
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
    @FXML
    void Back(ActionEvent event) {
        if ("RECEPTIONIST".equals(Launcher.currentRole)) {
            Launcher.setRoot("ReceptionistDashboard");
        } else {
            Launcher.setRoot("admindashboard");
        }
    }


    private void showStatus(String message, boolean isError) {
        lblStatus.setText(message);
        lblStatus.setStyle(isError
                ? "-fx-text-fill: #e53935;"
                : "-fx-text-fill: #43a047;");
    }
}