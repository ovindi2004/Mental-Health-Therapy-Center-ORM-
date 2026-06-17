package lk.ijse.mental_health_therapy.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lk.ijse.mental_health_therapy.Config.FactoryConfiguration;
import lk.ijse.mental_health_therapy.Launcher;
import lk.ijse.mental_health_therapy.bo.BOFactory;
import lk.ijse.mental_health_therapy.bo.custom.TherapyProgramBO;
import lk.ijse.mental_health_therapy.dto.TherapyProgramDTO;
import lk.ijse.mental_health_therapy.entity.Therapist;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TherapyProgramManagementController implements Initializable {


    @FXML
    private TextField txtProgramId;
    @FXML
    private TextField txtProgramName;
    @FXML
    private TextField txtDuration;
    @FXML
    private TextField txtFee;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> cmbTherapist;

    @FXML
    private Label lblIdError;
    @FXML
    private Label lblNameError;
    @FXML
    private Label lblFeeError;
    @FXML
    private Label lblStatus;

    @FXML
    private TableView<TherapyProgramDTO> tblPrograms;
    @FXML
    private TableColumn<TherapyProgramDTO, String> colId;
    @FXML
    private TableColumn<TherapyProgramDTO, String> colName;
    @FXML
    private TableColumn<TherapyProgramDTO, String> colDuration;
    @FXML
    private TableColumn<TherapyProgramDTO, Double> colFee;
    @FXML
    private TableColumn<TherapyProgramDTO, String> colTherapist;
    @FXML
    private TableColumn<TherapyProgramDTO, String> colDesc;



    TherapyProgramBO therapyProgramBO = (TherapyProgramBO) BOFactory.getInstance().getBO(BOFactory.BO_Types.THERAPY_PROGRAM);

    // therapist display name → therapist id
    private final Map<String, Integer> therapistMap = new LinkedHashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadTherapists();
        refreshTable();
    }


    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("programId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colFee.setCellValueFactory(new PropertyValueFactory<>("fee"));
        colTherapist.setCellValueFactory(new PropertyValueFactory<>("therapistName"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
    }


    private void loadTherapists() {
        therapistMap.clear();
        Session session = FactoryConfiguration.getInstance().getSession();
        try {
            List<Therapist> list = session.createQuery(
                    "FROM Therapist ORDER BY fullName", Therapist.class).getResultList();
            ObservableList<String> names = FXCollections.observableArrayList();
            names.add("-- None --");
            therapistMap.put("-- None --", 0);
            for (Therapist t : list) {
                String display = t.getFullName() + " (" + t.getSpecialization() + ")";
                therapistMap.put(display, t.getId());
                names.add(display);
            }
            cmbTherapist.setItems(names);
            cmbTherapist.getSelectionModel().selectFirst();
        } catch (Exception e) {
            showError("Failed to load therapists: " + e.getMessage());
        } finally {
            session.close();
        }
    }


    @FXML
    void saveProgram(ActionEvent event) {
        clearErrors();
        if (!validateForm()) return;

        TherapyProgramDTO dto = buildDTO();
        try {
            boolean saved = therapyProgramBO.saveProgram(dto);
            if (saved) {
                showSuccess("✔ Program \"" + dto.getProgramId() + "\" saved successfully.");
                clearForm(null);
                refreshTable();
            } else {
                showError("Save failed. Please try again.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }


    @FXML
    void updateProgram(ActionEvent event) {
        clearErrors();
        if (txtProgramId.getText().isBlank()) {
            lblIdError.setText("Select a row from the table first.");
            return;
        }
        if (!validateForm()) return;

        TherapyProgramDTO dto = buildDTO();
        try {
            boolean updated = therapyProgramBO.updateProgram(dto);
            if (updated) {
                showSuccess("✔ Program \"" + dto.getProgramId() + "\" updated successfully.");
                clearForm(null);
                refreshTable();
            } else {
                showError("Update failed — program not found.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }


    @FXML
    void onTableRowClick(MouseEvent event) {
        TherapyProgramDTO selected = tblPrograms.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        txtProgramId.setText(selected.getProgramId());
        txtProgramId.setEditable(false);          // ID must not change on update
        txtProgramName.setText(selected.getProgramName());
        txtDuration.setText(selected.getDuration() != null ? selected.getDuration() : "");
        txtFee.setText(String.valueOf(selected.getFee()));
        txtDescription.setText(selected.getDescription() != null ? selected.getDescription() : "");

        // Match therapist in ComboBox
        String match = cmbTherapist.getItems().stream()
                .filter(s -> s.startsWith(selected.getTherapistName()))
                .findFirst()
                .orElse("-- None --");
        cmbTherapist.setValue(match);

        showInfo("Editing: " + selected.getProgramId());
    }


    @FXML
    void clearForm(ActionEvent event) {
        txtProgramId.clear();
        txtProgramId.setEditable(true);
        txtProgramName.clear();
        txtDuration.clear();
        txtFee.clear();
        txtDescription.clear();
        cmbTherapist.getSelectionModel().selectFirst();
        clearErrors();
        lblStatus.setText("");
        tblPrograms.getSelectionModel().clearSelection();
    }


    @FXML
    void goBack(ActionEvent event) {
        Launcher.setRoot("admindashboard");   // Admin only
    }


    private void refreshTable() {
        try {
            List<TherapyProgramDTO> list = therapyProgramBO.getAllPrograms();
            tblPrograms.setItems(FXCollections.observableArrayList(list));
        } catch (Exception ex) {
            showError("Failed to load programs: " + ex.getMessage());
        }
    }


    private TherapyProgramDTO buildDTO() {
        TherapyProgramDTO dto = new TherapyProgramDTO();
        dto.setProgramId(txtProgramId.getText().trim().toUpperCase());
        dto.setProgramName(txtProgramName.getText().trim());
        dto.setDuration(txtDuration.getText().trim());
        dto.setFee(Double.parseDouble(txtFee.getText().trim()));
        dto.setDescription(txtDescription.getText().trim());

        String therapistDisplay = cmbTherapist.getValue();
        int therapistId = therapistMap.getOrDefault(therapistDisplay, 0);
        dto.setTherapistId(therapistId);
        dto.setTherapistName(therapistDisplay == null || therapistDisplay.equals("-- None --")
                ? "Not Assigned" : therapistDisplay);
        return dto;
    }


    private boolean validateForm() {
        boolean ok = true;

        if (txtProgramId.getText().isBlank()) {
            lblIdError.setText("Program ID is required (e.g. MT1006).");
            ok = false;
        }
        if (txtProgramName.getText().isBlank()) {
            lblNameError.setText("Program Name is required.");
            ok = false;
        }
        if (txtFee.getText().isBlank()) {
            lblFeeError.setText("Fee is required.");
            ok = false;
        } else {
            try {
                double fee = Double.parseDouble(txtFee.getText().trim());
                if (fee <= 0) {
                    lblFeeError.setText("Fee must be greater than 0.");
                    ok = false;
                }
            } catch (NumberFormatException e) {
                lblFeeError.setText("Enter a valid numeric fee.");
                ok = false;
            }
        }
        return ok;
    }

    private void clearErrors() {
        lblIdError.setText("");
        lblNameError.setText("");
        lblFeeError.setText("");
    }

    private void showSuccess(String msg) {
        lblStatus.setStyle("-fx-text-fill: #16A34A; -fx-font-size: 12px;");
        lblStatus.setText(msg);
    }

    private void showError(String msg) {
        lblStatus.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        lblStatus.setText(msg);
    }

    private void showInfo(String msg) {
        lblStatus.setStyle("-fx-text-fill: #2563B0; -fx-font-size: 12px;");
        lblStatus.setText(msg);
    }
}
